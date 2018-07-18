package epsos.ccd.posam.tm.service.impl;

import epsos.ccd.gnomon.auditmanager.*;
import epsos.ccd.posam.tm.exception.TMError;
import epsos.ccd.posam.tm.exception.TMException;
import epsos.ccd.posam.tm.exception.TmErrorCtx;
import epsos.ccd.posam.tm.response.TMResponseStructure;
import epsos.ccd.posam.tm.service.ITransformationService;
import epsos.ccd.posam.tm.util.*;
import epsos.ccd.posam.tsam.exception.ITMTSAMEror;
import epsos.ccd.posam.tsam.response.TSAMResponseStructure;
import epsos.ccd.posam.tsam.service.ITerminologyService;
import epsos.ccd.posam.tsam.util.CodedElement;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.NamespaceSupport;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.xml.datatype.DatatypeFactory;
import java.util.*;

/**
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 */
public class TransformationService implements ITransformationService, TMConstants, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TransformationService.class);
    private final Logger logger_clinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private ITerminologyService tsamApi = null;
    private HashMap<String, String> level1Type;
    private HashMap<String, String> level3Type;
    private TMConfiguration config;

    public TMResponseStructure toEpSOSPivot(Document epSOSOriginalData) {

        logger.info("Transforming OpenNCP CDA Document toEpsosPivot [START]");
        TMResponseStructure responseStructure = process(epSOSOriginalData, null, true);
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            try {
                logger_clinical.debug("PIVOT CDA: \n'{}'", XMLUtil.prettyPrint(responseStructure.getDocument()));
            } catch (Exception e) {
                logger.error("Exception: '{}'", e.getMessage(), e);
            }
        }
        logger.info("Transforming OpenNCP CDA Document toEpsosPivot [END]");
        return responseStructure;
    }

    public TMResponseStructure translate(Document epSosCDA, String targetLanguageCode) {

        logger.info("Translating OpenNCP CDA Document [START]");
        TMResponseStructure responseStructure = process(epSosCDA, targetLanguageCode, false);
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            try {
                logger_clinical.debug("Translate CDA: \n'{}'", XMLUtil.prettyPrint(responseStructure.getDocument()));
            } catch (Exception e) {
                logger.error("Exception: '{}'", e.getMessage(), e);
            }
        }
        logger.info("Translating OpenNCP CDA Document [END]");
        return responseStructure;
    }

    /**
     * Method checks for CDA document code and body and returns constant determining Document type
     * (PatientSummary, ePrescription, eDispensation) and level of CDA document (1 - unstructured, or 3 - structured)
     *
     * @param document input CDA document
     * @return Constant which determines one of six Document types
     * @throws Exception
     */
    public String getCDADocumentType(Document document) throws Exception {

        List<Node> nodeList = XmlUtil.getNodeList(document, XPATH_CLINICALDOCUMENT_CODE);

        // Document type code
        String docTypeCode;
        // exactly 1 document type element should exist
        if (nodeList.size() == 1) {
            Element docTypeCodeElement = (Element) nodeList.get(0);
            docTypeCode = docTypeCodeElement.getAttribute(CODE);
            logger.debug("docTypeCode: '{}'", docTypeCode);
            if (docTypeCode == null || docTypeCode.length() == 0) {
                throw new TMException(TMError.ERROR_DOCUMENT_CODE_NOT_EXIST);
            }
        } else {
            logger.error("Problem obtaining document type code ! found /ClinicalDocument/code elements: '{}'", nodeList.size());
            throw new TMException(TMError.ERROR_DOCUMENT_CODE_NOT_EXIST);
        }

        // Document level (1 - unstructured or 3 - structured)
        boolean level3Doc;
        // check if structuredDocument
        Node nodeStructuredBody = XmlUtil.getNode(document, XPATH_STRUCTUREDBODY);
        if (nodeStructuredBody != null) {
            logger.debug("Found - '{}'", XPATH_STRUCTUREDBODY);
            // LEVEL 3 document
            level3Doc = true;
        } else {
            // check if unstructured document
            logger.debug("Not Found - '{}'", XPATH_STRUCTUREDBODY);
            Node nodeNonXMLBody = XmlUtil.getNode(document, XPATH_NONXMLBODY);
            if (nodeNonXMLBody != null) {
                logger.debug("Found - '{}'", NON_XML_BODY);
                // LEVEL 1 document
                level3Doc = false;
            } else {
                // NO BODY - Document will be processed as LEVEL 1
                level3Doc = false;
            }
        }

        String docTypeConstant;

        // find constant for Document type
        if (level3Doc) {
            docTypeConstant = level3Type.get(docTypeCode);
        } else {
            docTypeConstant = level1Type.get(docTypeCode);
        }

        if (docTypeConstant == null) {
            throw new TMException(new TmErrorCtx(TMError.ERROR_DOCUMENT_CODE_UNKNOWN, docTypeCode));
        }

        logger.debug("docTypeConstant is: '{}'", docTypeConstant);
        return docTypeConstant;
    }


    private TMResponseStructure process(Document inputDocument, String targetLanguageCode, boolean isTranscode) {

        logger.info("Processing CDA Document: '{}', Target Language: '{}', Transcoding: '{}'", inputDocument.toString(), targetLanguageCode, isTranscode);
        TMResponseStructure responseStructure;
        String status;
        List<ITMTSAMEror> errors = new ArrayList<>();
        List<ITMTSAMEror> warnings = new ArrayList<>();
        byte[] inputDocbytes;

        try {
            if (inputDocument == null) {
                status = STATUS_FAILURE;
                errors.add(TMError.ERROR_NULL_INPUT_DOCUMENT);
                responseStructure = new TMResponseStructure(null, status, errors, warnings);
                logger.error("Error, null input document!");
                return responseStructure;
            } else {
                // validate schema
                inputDocbytes = XmlUtil.doc2bytes(inputDocument);
                Document namespaceAwareDoc = XmlUtil.getNamespaceAwareDocument(inputDocbytes);

                // check document type
                // check if document is structured or unstructured
                Document namespaceNotAwareDoc = inputDocument;
                String cdaDocumentType = getCDADocumentType(namespaceNotAwareDoc);

                //boolean schemaValid = Validator.validateToSchema(namespaceAwareDoc);

                // MDA validation
                if (config.isModelValidationEnabled()) {
                    ModelValidatorResult validateMDA = Validator.validateMDA(new String(inputDocbytes), cdaDocumentType, isTranscode);

                    if (!validateMDA.isSchemaValid()) {
                        warnings.add(TMError.WARNING_INPUT_XSD_VALIDATION_FAILED);
                    }
                    if (!validateMDA.isModelValid()) {
                        warnings.add(TMError.WARNING_OUTPUT_MDA_VALIDATION_FAILED);
                    }

                }

                //XSD Schema Validation
                if (config.isSchemaValidationEnabled()) {

                    boolean schemaValid = Validator.validateToSchema(namespaceAwareDoc);
                    // if model validation is enabled schema validation is done as part of it so there is no point doing it again
                    if (!schemaValid) {
                        status = STATUS_FAILURE;
                        warnings.add(TMError.WARNING_INPUT_XSD_VALIDATION_FAILED);
                    }
                }

                // Schematron Validation
                if (config.isSchematronValidationEnabled()) {
                    // if transcoding, validate against friendly scheme,
                    // else against pivot scheme
                    boolean validateFriendly = isTranscode;
                    SchematronResult result = Validator.validateSchematron(inputDocument, cdaDocumentType, validateFriendly);
                    if (result == null || !result.isValid()) {
                        status = STATUS_FAILURE;
                        warnings.add(TMError.WARNING_INPUT_SCHEMATRON_VALIDATION_FAILED);
                        logger.error("Schematron validation error, input document is invalid!");
                        if (result != null) {
                            logger.error(result.toString());
                        }
                    }
                } else {
                    logger.info("Schematron validation disabled");
                }

                // transcode/translate document
                status = isTranscode ? transcodeDocument(namespaceNotAwareDoc, errors, warnings,
                        cdaDocumentType) : translateDocument(namespaceNotAwareDoc, targetLanguageCode,
                        errors, warnings, cdaDocumentType);

                Document finalDoc = XmlUtil.removeEmptyXmlns(namespaceNotAwareDoc);

                if (config.isModelValidationEnabled()) {
                    ModelValidatorResult validateMDA = Validator.validateMDA(XmlUtil.xmlToString(finalDoc),
                            cdaDocumentType, !isTranscode);
                    if (!validateMDA.isSchemaValid()) {
                        warnings.add(TMError.WARNING_OUTPUT_XSD_VALIDATION_FAILED);
                    }
                    if (!validateMDA.isModelValid()) {
                        warnings.add(TMError.WARNING_OUTPUT_MDA_VALIDATION_FAILED);
                    }

                }
                // validate RESULT (schematron)
                if (config.isSchematronValidationEnabled()) {

                    SchematronResult result = Validator.validateSchematron(finalDoc, cdaDocumentType, !isTranscode);
                    if (result == null || !result.isValid()) {
                        status = STATUS_FAILURE;
                        warnings.add(TMError.WARNING_OUTPUT_SCHEMATRON_VALIDATION_FAILED);
                        responseStructure = new TMResponseStructure(finalDoc, status, errors, warnings);
                        logger.error("Schematron validation error, result document is invalid!");
                        if (logger.isErrorEnabled() && result != null) {
                            logger.error(result.toString());
                        }
                        return responseStructure;
                    }
                } else {
                    logger.debug("Schematron validation disabled");
                }

                // create & fill TMResponseStructure
                responseStructure = new TMResponseStructure(finalDoc, status, errors, warnings);
                if (logger.isInfoEnabled()) {
                    logger.info("TM result:\n{}", responseStructure.toString());
                }
            }
        } catch (TMException e) {
            status = STATUS_FAILURE;
            errors.add(e.getReason());
            responseStructure = new TMResponseStructure(inputDocument, status, errors, warnings);
            // log ERROR
            logger.error(e.getReason().toString(), e);
        } catch (Exception e) {
            // write ERROR to ResponseStructure
            status = STATUS_FAILURE;
            errors.add(TMError.ERROR_PROCESSING_ERROR);
            responseStructure = new TMResponseStructure(inputDocument, status, errors, warnings);
            // log ERROR
            logger.error(TMError.ERROR_PROCESSING_ERROR.toString(), e);
        }

        // audit log
        writeAuditTrail(responseStructure);

        return responseStructure;
    }

    public List<String> getLtrLanguages() {
        return tsamApi.getLtrLanguages();
    }

    /**
     * @param responseStructure
     */
    private void writeAuditTrail(TMResponseStructure responseStructure) {

        logger.debug("[Transformation Service] Audit trail BEGIN");

        if (responseStructure != null) {
            if (config.isAuditTrailEnabled()) {

                try {
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTime(new Date());
                    // TODO this is not final/correct version
                    EventLog eventLog = EventLog.createEventLogPivotTranslation(
                            TransactionName.epsosPivotTranslation, // Possible values according to D4.5.6 are E,R,U,D
                            EventActionCode.EXECUTE,
                            DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar),
                            responseStructure.getStatus().equals(STATUS_SUCCESS) ? EventOutcomeIndicator.FULL_SUCCESS : EventOutcomeIndicator.PERMANENT_FAILURE,
                            // The string encoded CN of the TLS certificate of the NCP triggered the epsos operation
                            HTTPUtil.getSubjectDN(false),
                            // Identifier that allows to unequivocally identify the SOURCE document or source data entries. (UUID Format)
                            getOIDFromDocument(responseStructure.getDocument()),
                            getOIDFromDocument(responseStructure.getResponseCDA()), // Identifier that allows to univocally identify the TARGET document. (UUID Format)
                            Constants.UUID_PREFIX + "", // The value MUST contain the base64 encoded security header
                            new byte[0], // ReqM_PatricipantObjectDetail - The value MUST contain the base64 encoded security header
                            Constants.UUID_PREFIX + "", // String-encoded UUID of the response message
                            new byte[0], // ResM_PatricipantObjectDetail - The value MUST contain the base64 encoded security header
                            ConfigurationManagerFactory.getConfigurationManager().getProperty("SERVER_IP") // The IP Address of the target Gateway
                    );
                    eventLog.setEventType(EventType.epsosPivotTranslation);
                    eventLog.setNcpSide(NcpSide.valueOf(config.getNcpSide()));

                    logger.info("Write AuditTrail: '{}'", eventLog.getEventType());
                    AuditServiceFactory.getInstance().write(eventLog, config.getAuditTrailFacility(), config.getAuditTrailSeverity());
                } catch (Exception e) {
                    logger.error("Audit trail ERROR! ", e);
                }
                logger.debug("[Transformation Service] Audit trail END");
            } else {
                logger.debug("[Transformation Service] Audit trail DISABLED");
            }
        } else {
            logger.error("Write AuditTrail Error: Cannot process Transformation Manager response");
        }

    }

    /**
     * Method iterates document for coded elements, calls for each TSAM.getEpSOSConceptByCode method,
     * Input document is enriched with translation elements (transcoded Concept), list of errors & warnings is filled,
     * finally status of operation is returned
     *
     * @param document        original CDA document
     * @param errors          empty list for TMErrors
     * @param warnings        empty list for TMWarnings
     * @param cdaDocumentType
     * @return String - final status of transcoding
     */
    private String transcodeDocument(Document document, List<ITMTSAMEror> errors, List<ITMTSAMEror> warnings, String cdaDocumentType) {

        logger.info("Transcoding Document '{}'", cdaDocumentType);
        return processDocument(document, null, errors, warnings, cdaDocumentType, true);
    }

    /**
     * Search textContent for referenced values and replace it with transcoded/translated displayName for each key (reference id)
     *
     * @param document
     * @param hmReffIdDisplayName
     * @param process
     * @deprecated
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void replaceReferencedValues(Document document, HashMap<String, String> hmReffIdDisplayName, String process) {

        // just log referenced keys and displayNames
        logReferences(hmReffIdDisplayName, process);
        logger.debug("replaceReferencedValues BEGIN");
        try {
            // find elements with ID attribute which id=remembered reference key
            // and
            // replace their textContent
            List<Node> idElements = XmlUtil.getNodeList(document, XPATH_ALL_ELEMENTS_WITH_ID_ATTR);
            String id;
            for (Node idElement1 : idElements) {
                Element idElement = (Element) idElement1;
                id = idElement.getAttribute(ID);
                if (notEmpty(id) && hmReffIdDisplayName != null && hmReffIdDisplayName.containsKey(id) && idElement.getChildNodes().getLength() == 1) {

                    logger.debug("replaced id: '{}' '{}' --> '{}'", id, idElement.getTextContent(), hmReffIdDisplayName.get(id));
                    idElement.setTextContent(hmReffIdDisplayName.get(id));
                }
            }
            logger.debug("replaceReferencedValues END");
        } catch (Exception e) {
            logger.error("replaceReferencedValues error: ", e);
        }
    }

    /**
     * Just logs referenced keys and displayNames
     *
     * @param hmReffIdDisplayName
     * @param process
     */
    private void logReferences(HashMap<String, String> hmReffIdDisplayName, String process) {

        if (hmReffIdDisplayName != null && !hmReffIdDisplayName.isEmpty()) {
            Iterator<String> iKeys = hmReffIdDisplayName.keySet().iterator();
            logger.debug("List ('{}') - Reference id : displayName - ", process);
            while (iKeys.hasNext()) {
                String key = iKeys.next();
                logger.debug("'{}':'{}'", key, hmReffIdDisplayName.get(key));
            }
        }
    }

    /**
     * Calls TSAM.getEpSOSConceptByCode method, if transcoding is successful, constructs translation element
     * for original data, new/transcoded data are placed in original element.
     *
     * @param originalElement     - transcoded Coded Element
     * @param document            - input CDA document
     * @param hmReffIdDisplayName hashMap for ID of referencedValues and
     *                            transcoded DisplayNames
     * @param warnings
     * @param errors
     * @return boolean - true if SUCCES otherwise false
     */
    private boolean transcodeElement(Element originalElement,
                                     Document document, HashMap<String, String> hmReffIdDisplayName,
                                     String valueSet, String valueSetVersion, List<ITMTSAMEror> errors,
                                     List<ITMTSAMEror> warnings) {
        return processElement(originalElement, document, null,
                hmReffIdDisplayName, valueSet, valueSetVersion, true, errors,
                warnings);
    }

    /**
     * Checks if element contains originalText with reference value, if yes, remember displayName for this reference id.
     *
     * @param hmReffIdDisplayName
     * @param displayName
     * @param codedElement
     */
    @SuppressWarnings("unused")
    private void checkRememberReference(
            HashMap<String, String> hmReffIdDisplayName, String displayName,
            Element codedElement) {
        try {
            // find Elements with tagname "originalText"
            NodeList texts = codedElement.getElementsByTagName(TMConstants.ORIGINAL_TEXT);
            for (int i = 0; i < texts.getLength(); i++) {
                Element text = (Element) texts.item(i);
                // find Element with tagname "reference"
                NodeList references = text.getElementsByTagName(TMConstants.REFERENCE);
                if (references.getLength() > 0) {
                    Element reference = (Element) references.item(0);
                    String key = reference.getAttribute(VALUE);
                    if (notEmpty(key)) {
                        key = key.replace(HASH, EMPTY_STRING);
                        if (!hmReffIdDisplayName.containsKey(key)) {
                            hmReffIdDisplayName.put(key, displayName);
                            logger.debug("remembered @reference= '{}' | '{}'", key, displayName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("checkRememberReference error: ", e);
        }
    }

    /**
     * Method iterates document for translated coded elements, calls for each
     * TSAM.getDesignationByEpSOSConcept method, Input document is enriched with
     * translation elements (translated Concept), list of errors & warnings is
     * filled, finally status of operation is returned
     *
     * @param document           - translated CDA document
     * @param targetLanguageCode - language Code
     * @param errors             empty list for TMErrors
     * @param warnings           empty list for TMWarnings
     * @return String - final status of transcoding
     */
    private String translateDocument(Document document, String targetLanguageCode, List<ITMTSAMEror> errors,
                                     List<ITMTSAMEror> warnings, String cdaDocumentType) {

        logger.info("Translating Document '{}' to target Language: '{}'", cdaDocumentType, targetLanguageCode);
        return processDocument(document, targetLanguageCode, errors, warnings,
                cdaDocumentType, false);
    }

    private String processDocument(Document document, String targetLanguageCode, List<ITMTSAMEror> errors,
                                   List<ITMTSAMEror> warnings, String cdaDocumentType, boolean isTranscode) {

        logger.info("Processing Document '{}' to target Language: '{}' Transcoding: '{}", cdaDocumentType, targetLanguageCode, isTranscode);
        boolean processingOK = true;
        // hashMap for ID of referencedValues and transcoded/translated
        // DisplayNames
        HashMap<String, String> hmReffId_DisplayName = new HashMap<>();
        boolean isProcessingSuccesful;

        if (CodedElementList.getInstance().isConfigurableElementIdentification()) {

            Collection<CodedElementListItem> ceList = CodedElementList.getInstance().getList(cdaDocumentType);
            logger.info("Configurable Element Identification is set, CodedElementList for '{}' contains elements: '{}'",
                    cdaDocumentType, ceList.size());
            if (ceList == null || ceList.isEmpty()) {
                warnings.add(TMError.WARNING_CODED_ELEMENT_LIST_EMPTY);
                return STATUS_SUCCESS;
            }
            Iterator<CodedElementListItem> iProcessed = ceList.iterator();
            CodedElementListItem codedElementListItem;
            String xPathExpression;
            List<Node> nodeList;
            boolean isRequired;
            String celTargetLanguageCode;
            boolean useCELTargetLanguageCode = false;

            while (iProcessed.hasNext()) {

                codedElementListItem = iProcessed.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Looking for: '{}'", codedElementListItem.toString());
                }
                xPathExpression = codedElementListItem.getxPath();
                isRequired = codedElementListItem.isRequired();

                if (!isTranscode) {
                    celTargetLanguageCode = codedElementListItem.getTargetLanguageCode();

                    // if targetLanguageCode is specified in CodedElementList, this is used for translation
                    useCELTargetLanguageCode = celTargetLanguageCode != null && celTargetLanguageCode.length() > 1;
                }

                nodeList = XmlUtil.getNodeList(document, xPathExpression);
                logger.debug("Found: '{}' elements", (nodeList == null ? "NULL" : nodeList.size()));

                if (isRequired && (nodeList == null || nodeList.isEmpty())) {

                    if (logger.isErrorEnabled()) {
                        logger.error("Required element is missing: '{}'", codedElementListItem.toString());
                    }
                    processingOK = false;
                    errors.add(new TmErrorCtx(TMError.ERROR_REQUIRED_CODED_ELEMENT_MISSING, codedElementListItem.toString()));
                } else {

                    Element originalElement;
                    if (nodeList != null) {

                        for (Node aNodeList : nodeList) {
                            // iterate elements for processing
                            originalElement = (Element) aNodeList;

                            // check if xsi:type is "CE" or "CD"
                            checkType(originalElement, warnings);

                            // call tsam transcode/translate method for each coded
                            // element
                            isProcessingSuccesful = (isTranscode ? transcodeElement(
                                    originalElement, document, hmReffId_DisplayName, null,
                                    null, errors, warnings) : translateElement(
                                    originalElement, document, targetLanguageCode,
                                    hmReffId_DisplayName, null, null, errors, warnings));

                            // if is required & processing is unsuccessful,
                            // report ERROR
                            if (isRequired && !isProcessingSuccesful) {
                                processingOK = false;
                                String ctx = XmlUtil.getElementPath(originalElement);
                                errors.add(isTranscode ? new TmErrorCtx(TMError.ERROR_REQUIRED_CODED_ELEMENT_NOT_TRANSCODED, ctx)
                                        : new TmErrorCtx(TMError.ERROR_REQUIRED_CODED_ELEMENT_NOT_TRANSLATED, ctx));
                                logger.error("Required coded element was not translated");
                            }
                        }
                    }
                }
            }
        } else {

            logger.info("Configurable Element Identification is NOT set - looking for //*[@code] elements");
            List<Node> nodeList = XmlUtil.getNodeList(document, XPATH_ALL_ELEMENTS_WITH_CODE_ATTR);
            logger.info("Found '{}' elements to translate/transcode", nodeList.size());
            Element originalElement;
            for (Node aNodeList : nodeList) {
                // iterate elements for translation
                originalElement = (Element) aNodeList;
                // if element name is translation, don't do anything
                if (TRANSLATION.equals(originalElement.getLocalName())) {

                    CodedElement ce = new CodedElement(originalElement);
                    if (logger.isDebugEnabled()) {
                        logger.debug("translation element - skipping: '{}'", ce.toString());
                    }
                    continue;
                }
                // check if xsi:type is "CE" or "CD"
                checkType(originalElement, warnings);

                // call TSAM transcode/translate method for each coded element
                isProcessingSuccesful = (isTranscode ? transcodeElement(
                        originalElement, document, hmReffId_DisplayName, null,
                        null, errors, warnings) : translateElement(
                        originalElement, document, targetLanguageCode,
                        hmReffId_DisplayName, null, null, errors, warnings));
            }
        }
        return (processingOK ? STATUS_SUCCESS : STATUS_FAILURE);
    }

    /**
     * @param originalElement
     * @param warnings
     */
    private void checkType(Element originalElement, List<ITMTSAMEror> warnings) {

        if (originalElement != null && StringUtils.isNotBlank(originalElement.getAttribute(XSI_TYPE))) {

            String[] parts = new String[3];
            NamespaceSupport support = new NamespaceSupport();
            support.pushContext();
            // TODO: this method has to be optimized and managed carefully the parser to manage the namespaces used.
            support.declarePrefix("urn", "urn:hl7-org:v3");
            parts = support.processName(originalElement.getAttributeNode(XSI_TYPE).getValue(), parts, false);

            if (logger.isDebugEnabled()) {
                logger.debug("Namespace URI: '{}' - Local Name: '{}' - Raw Name: '{}'", parts[0], parts[1], parts[2]);
            }
            if (!StringUtils.equals(parts[1], CE) && !StringUtils.equals(parts[1], CD)) {
                warnings.add(TMError.WARNING_CODED_ELEMENT_NOT_PROPER_TYPE);
            }
        }
    }

    private boolean translateElement(Element originalElement,
                                     Document document, String targetLanguageCode,
                                     HashMap<String, String> hmReffIdDisplayName, String valueSet,
                                     String valueSetVersion, List<ITMTSAMEror> errors,
                                     List<ITMTSAMEror> warnings) {

        return processElement(originalElement, document, targetLanguageCode,
                hmReffIdDisplayName, valueSet, valueSetVersion, false, errors,
                warnings);
    }

    private boolean processElement(Element originalElement, Document document,
                                   String targetLanguageCode,
                                   HashMap<String, String> hmReffIdDisplayName, String valueSet,
                                   String valueSetVersion, boolean isTranscode,
                                   List<ITMTSAMEror> errors, List<ITMTSAMEror> warnings) {

        //TODO: Update the translation Node while the translation/transcoding process
        try {
            // kontrola na povinne atributy
            Boolean checkAttributes = checkAttributes(originalElement, warnings);
            if (checkAttributes != null) {
                return checkAttributes.booleanValue();
            }

            CodedElement codedElement = new CodedElement(originalElement);
            codedElement.setVsOid(valueSet);
            codedElement.setValueSetVersion(valueSetVersion);

            // hladanie vnoreneho elementu translation
            Node oldTranslationElement = findOldTranslation(originalElement);

            TSAMResponseStructure tsamResponse = isTranscode ? tsamApi
                    .getEpSOSConceptByCode(codedElement)
                    : tsamApi.getDesignationByEpSOSConcept(
                    codedElement, targetLanguageCode);

            if (tsamResponse.isStatusSuccess()) {
                logger.debug("processing successful '{}'", codedElement.toString());
                // +++++ Element editing BEGIN +++++

                // NEW TRANSLATION element
                Element newTranslation = document.createElementNS(originalElement.getNamespaceURI(), TRANSLATION);
                if (originalElement.getPrefix() != null) {
                    newTranslation.setPrefix(originalElement.getPrefix());
                }
                boolean attributesFilled = false;
                // check - no repeated attributed in translation element by
                // transcoding
                // if codeSystem && code for source and target are same
                if (notEmpty(tsamResponse.getCodeSystem()) && notEmpty(codedElement.getOid())
                        && !codedElement.getOid().equalsIgnoreCase(tsamResponse.getCodeSystem())
                        || (codedElement.getOid().equalsIgnoreCase(tsamResponse.getCodeSystem())
                        && !codedElement.getCode().equals(tsamResponse.getCode()))) {
                    // code
                    if (notEmpty(codedElement.getCode())) {
                        newTranslation.setAttribute(CODE, codedElement.getCode());
                    }
                    // codeSystem
                    if (notEmpty(codedElement.getOid())) {
                        newTranslation.setAttribute(CODE_SYSTEM, codedElement.getOid());
                    }
                    // codeSystemName
                    if (notEmpty(codedElement.getCodeSystem())) {
                        newTranslation.setAttribute(CODE_SYSTEM_NAME, codedElement.getCodeSystem());
                    }
                    // codeSystemVersion
                    if (notEmpty(codedElement.getVersion())) {
                        newTranslation.setAttribute(CODE_SYSTEM_VERSION, codedElement.getVersion());
                    }
                    attributesFilled = true;
                }
                // designation (only if source and target differs)
                if (!tsamResponse.getDesignation().equals(codedElement.getDisplayName())) {
                    if (StringUtils.isNotBlank(codedElement.getDisplayName())) {
                        newTranslation.setAttribute(DISPLAY_NAME, codedElement.getDisplayName());
                    }
                    if (notEmpty(codedElement.getCode())) {
                        newTranslation.setAttribute(CODE, codedElement.getCode());
                    }
                    // codeSystem
                    if (notEmpty(codedElement.getOid())) {
                        newTranslation.setAttribute(CODE_SYSTEM, codedElement.getOid());
                    }
                    // codeSystemName
                    if (notEmpty(codedElement.getCodeSystem())) {
                        newTranslation.setAttribute(CODE_SYSTEM_NAME, codedElement.getCodeSystem());
                    }
                    attributesFilled = true;
                } else {
                    logger.debug("Translation is same as original: '{}'", tsamResponse.getDesignation());
                }
                if (attributesFilled) {
                    if (oldTranslationElement != null) {
                        oldTranslationElement = originalElement.removeChild(oldTranslationElement);
                        newTranslation.appendChild(oldTranslationElement);
                    }
                    originalElement.appendChild(newTranslation);
                }

                // CHANGE original attributes code
                if (notEmpty(tsamResponse.getCode())) {
                    originalElement.setAttribute(CODE, tsamResponse.getCode());
                }
                // codeSystem
                if (notEmpty(tsamResponse.getCodeSystem())) {
                    originalElement.setAttribute(CODE_SYSTEM, tsamResponse.getCodeSystem());
                }
                // codeSystemName
                if (notEmpty(tsamResponse.getCodeSystemName())) {
                    originalElement.setAttribute(CODE_SYSTEM_NAME, tsamResponse.getCodeSystemName());
                }
                // codeSystemVersion
                if (notEmpty(tsamResponse.getCodeSystemVersion())) {
                    originalElement.setAttribute(CODE_SYSTEM_VERSION, tsamResponse.getCodeSystemVersion());
                }
                // designation
                if (notEmpty(tsamResponse.getDesignation())) {
                    originalElement.setAttribute(DISPLAY_NAME, tsamResponse.getDesignation());
                }
                // +++++ Element editing END +++++
                errors.addAll(tsamResponse.getErrors());
                warnings.addAll(tsamResponse.getWarnings());
                return true;
            } else {
                logger.error("processing failure! for Code: '{}'", codedElement.toString());
                errors.addAll(tsamResponse.getErrors());
                warnings.addAll(tsamResponse.getWarnings());
                return false;
            }
        } catch (Exception e) {
            // system error
            logger.error("processing failure! ", e);
            return false;
        }
    }

    /**
     * Najde a vrati vnoreny translation element ak existuje
     *
     * @param originalElement
     * @return
     */
    private Node findOldTranslation(Element originalElement) {

        Node oldTranslationElement = null;
        NodeList nodeList = originalElement.getChildNodes();
        if (nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (TMConstants.TRANSLATION.equals(node.getLocalName())) {
                        oldTranslationElement = node;
                        logger.debug("Old translation found");
                        break;
                    }
                }
            }
        }
        return oldTranslationElement;
    }

    /**
     * Kontrola na povinne atributy.
     *
     * @param originalElement
     * @param warnings
     * @return Vrati true ak je povolene nemat povinne atributy, false ak nie,
     * null ak je vsetko ok
     */
    private Boolean checkAttributes(Element originalElement, List<ITMTSAMEror> warnings) {

        String elName = XmlUtil.getElementPath(originalElement);
        // ak je nullFlavor, neprekladat, nevyhadzovat chybu
        if (originalElement.hasAttribute("nullFlavor")) {
            logger.debug("nullFlavor, skippink: '{}'", elName);
            return true;
        } else {
            // ak chyba code alebo codeSystem vyhodit warning
            boolean noCode = false;
            boolean noCodeSystem = false;
            if (!originalElement.hasAttribute("code")) {
                noCode = true;
            }

            if (!originalElement.hasAttribute("codeSystem")) {
                noCodeSystem = true;
            }
            if (noCode || noCodeSystem) {
                NodeList origText = originalElement.getElementsByTagName("originalText");
                if (origText.getLength() > 0) {
                    // ak element obsahuje originalText, preskocit, nevyhazovat warning
                    logger.debug("Element without required attributes, but has originalText, ignoring: '{}'", elName);
                    return true;
                } else {
                    logger.warn("Element has no \"code or \"codeSystem\" attribute: '{}'", elName);
                    warnings.add(new TmErrorCtx(TMError.WARNING_MANDATORY_ATTRIBUTES_MISSING, "Element " + elName));
                    return false;
                }
            }
            return null;
        }
    }

    public void setTsamApi(ITerminologyService tsamApi) {
        this.tsamApi = tsamApi;
    }

    public boolean notEmpty(String string) {
        return (string != null && string.length() > 0);
    }

    public void setConfig(TMConfiguration config) {
        this.config = config;
    }

    public void afterPropertiesSet() {

        level1Type = new HashMap<>();
        level1Type.put(config.getPatientSummaryCode(), PATIENT_SUMMARY1);
        level1Type.put(config.geteDispensationCode(), EDISPENSATION1);
        level1Type.put(config.getePrescriptionCode(), EPRESCRIPTION1);
        level1Type.put(config.getHcerCode(), HCER1);
        level1Type.put(config.getMroCode(), MRO1);

        level3Type = new HashMap<>();
        level3Type.put(config.getPatientSummaryCode(), PATIENT_SUMMARY3);
        level3Type.put(config.geteDispensationCode(), EDISPENSATION3);
        level3Type.put(config.getePrescriptionCode(), EPRESCRIPTION3);
        level3Type.put(config.getHcerCode(), HCER3);
        level3Type.put(config.getMroCode(), MRO3);
    }

    /**
     * Obtains the unique identifier of the document
     *
     * @param doc
     * @return
     */
    private String getOIDFromDocument(Document doc) {

        String oid = "";
        if (doc.getElementsByTagName("id").getLength() > 0) {
            Node id = doc.getElementsByTagName("id").item(0);
            if (id.getAttributes().getNamedItem("root") != null) {
                oid = oid + id.getAttributes().getNamedItem("root").getTextContent();
            }
            if (id.getAttributes().getNamedItem("extension") != null) {
                oid = oid + "^" + id.getAttributes().getNamedItem("extension").getTextContent();
            }
        }
        return oid;
    }
}
