package tr.com.srdc.epsos.ws.server.xca.impl;

import epsos.ccd.gnomon.auditmanager.*;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.TMError;
import epsos.ccd.posam.tm.response.TMResponseStructure;
import epsos.ccd.posam.tm.service.ITransformationService;
import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMEror;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.xca.DocumentSearchInterface;
import eu.epsos.protocolterminators.ws.server.xca.XCAServiceInterface;
import eu.epsos.util.EvidenceUtils;
import eu.epsos.util.IheConstants;
import eu.epsos.util.xca.XCAConstants;
import eu.epsos.util.xdr.XDRConstants;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.Helper;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.OpenncpErrorCodeException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML2Validator;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.AdhocQueryResponseStatus;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.RegistryErrorSeverity;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import eu.europa.ec.sante.ehdsi.openncp.util.UUIDHelper;
import eu.europa.ec.sante.ehdsi.constant.error.IheErrorCode;
import fi.kela.se.epsos.data.model.*;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.*;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.data.model.FilterParams;
import tr.com.srdc.epsos.data.model.SimpleConfidentialityEnum;
import tr.com.srdc.epsos.data.model.SubstitutionCodeEnum;
import tr.com.srdc.epsos.data.model.xds.DocumentType;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.DateUtil;
import tr.com.srdc.epsos.util.XMLUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;
import tr.com.srdc.epsos.ws.server.xca.impl.eP.EPExtrinsicObjectBuilder;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import java.time.Instant;
import java.util.*;

public class XCAServiceImpl implements XCAServiceInterface {

    private static final DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(XCAServiceImpl.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");
    private final ITransformationService transformationService;
    private final OMFactory omFactory;
    private final oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory ofQuery;
    private final oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory ofRim;
    private final oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory ofRs;
    private final DocumentSearchInterface documentSearchService;

    /**
     * Public Constructor for IHE XCA Profile implementation, the default constructor will handle the loading of
     * the National Connector implementation by using the <class>ServiceLoader</class>
     *
     * @see ServiceLoader
     */
    public XCAServiceImpl() {

        ServiceLoader<DocumentSearchInterface> serviceLoader = ServiceLoader.load(DocumentSearchInterface.class);
        try {
            logger.info("Loading National implementation of DocumentSearchInterface...");
            documentSearchService = serviceLoader.iterator().next();
            logger.info("Successfully loaded documentSearchService");
        } catch (Exception e) {
            logger.error("Failed to load implementation of DocumentSearchInterface: " + e.getMessage(), e);
            throw e;
        }

        ofQuery = new oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory();
        ofRim = new oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory();
        ofRs = new oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory();

        omFactory = OMAbstractFactory.getOMFactory();

        var applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
        transformationService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());
    }

    private boolean isUUIDValid(String message) {
        try {
            var uuid = UUID.fromString(message);
            logger.debug("Valid UUID: '{}'", uuid);
            return true;
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: '{}'", e.getMessage());
            return false;
        }
    }

    private void prepareEventLogForQuery(EventLog eventLog, AdhocQueryRequest request, AdhocQueryResponse response,
                                         Element sh, String classCode) {

        logger.info("method prepareEventLogForQuery(Request: '{}', ClassCode: '{}')", request.getId(), classCode);

        switch (classCode) {
            case Constants.EP_CLASSCODE:
                eventLog.setEventType(EventType.ORDER_SERVICE_LIST);
                eventLog.setEI_TransactionName(TransactionName.ORDER_SERVICE_LIST);
                break;
            case Constants.PS_CLASSCODE:
                eventLog.setEventType(EventType.PATIENT_SERVICE_LIST);
                eventLog.setEI_TransactionName(TransactionName.PATIENT_SERVICE_LIST);
                break;
            case Constants.MRO_CLASSCODE:
                eventLog.setEventType(EventType.MRO_LIST);
                eventLog.setEI_TransactionName(TransactionName.MRO_SERVICE_LIST);
                break;
            case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_TITLE:
            case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
            case Constants.ORCD_MEDICAL_IMAGING_REPORTS_TITLE:
            case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                eventLog.setEventType(EventType.ORCD_SERVICE_LIST);
                eventLog.setEI_TransactionName(TransactionName.ORCD_SERVICE_LIST);
                break;
        }
        eventLog.setEI_EventActionCode(EventActionCode.READ);
        eventLog.setEI_EventDateTime(DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar()));
        eventLog.setPS_ParticipantObjectID(getDocumentEntryPatientId(request));

        if (response.getRegistryObjectList() != null) {
            List<String> documentIds = new ArrayList<>();
            for (var i = 0; i < response.getRegistryObjectList().getIdentifiable().size(); i++) {
                if (!(response.getRegistryObjectList().getIdentifiable().get(i).getValue() instanceof ExtrinsicObjectType)) {
                    continue;
                }
                ExtrinsicObjectType eot = (ExtrinsicObjectType) response.getRegistryObjectList().getIdentifiable().get(i).getValue();
                for (ExternalIdentifierType externalIdentifierType : eot.getExternalIdentifier()) {
                    if (externalIdentifierType.getIdentificationScheme().equals(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_SCHEME)) {
                        documentIds.add(externalIdentifierType.getValue());
                    }
                }
            }
            eventLog.setEventTargetParticipantObjectIds(documentIds);
        }

        // Set the operation status to the response
        handleEventLogStatus(eventLog, response, request);

        String userIdAlias = Helper.getAssertionsSPProvidedId(sh);
        eventLog.setHR_UserID(StringUtils.isNotBlank(userIdAlias) ? userIdAlias : "" + "<" + Helper.getUserID(sh)
                + "@" + Helper.getAssertionsIssuer(sh) + ">");
        eventLog.setHR_AlternativeUserID(Helper.getAlternateUserID(sh));
        eventLog.setHR_RoleID(Helper.getFunctionalRoleID(sh));
        eventLog.setSP_UserID(HTTPUtil.getSubjectDN(true));
        eventLog.setPT_ParticipantObjectID(getDocumentEntryPatientId(request));
        eventLog.setAS_AuditSourceId(Constants.COUNTRY_PRINCIPAL_SUBDIVISION);

        if (response.getRegistryErrorList() != null) {
            RegistryError re = response.getRegistryErrorList().getRegistryError().get(0);
            eventLog.setEM_ParticipantObjectID(re.getErrorCode());
            eventLog.setEM_ParticipantObjectDetail(re.getCodeContext().getBytes());
        }
    }

    private void handleEventLogStatus(EventLog eventLog, AdhocQueryResponse queryResponse, AdhocQueryRequest queryRequest) {

        if (queryResponse.getRegistryObjectList() == null) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
            // In case of failure, the document class code has been provided to the event log as event target as there is no
            // reference available as resources (document ID etc.).
            addDocumentClassCodeToEventLog(eventLog, queryRequest);
        } else if (queryResponse.getRegistryErrorList() == null) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        } else {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.TEMPORAL_FAILURE);
            // In case of failure, the document class code has been provided to the event log as event target as there is no
            // reference available as resources (document ID etc.).
            addDocumentClassCodeToEventLog(eventLog, queryRequest);
        }
    }

    private void addDocumentClassCodeToEventLog(EventLog eventLog, AdhocQueryRequest queryRequest) {

        for (SlotType1 slotType1 : queryRequest.getAdhocQuery().getSlot()) {
            if (StringUtils.equals(slotType1.getName(), "$XDSDocumentEntryClassCode")) {
                String documentType = slotType1.getValueList().getValue().get(0);
                documentType = org.apache.commons.lang3.StringUtils.remove(documentType, "('");
                documentType = org.apache.commons.lang3.StringUtils.remove(documentType, "')");
                eventLog.getEventTargetParticipantObjectIds().add(documentType);
            }
        }
    }

    private void prepareEventLogForRetrieve(EventLog eventLog, RetrieveDocumentSetRequestType request, boolean errorsDiscovered,
                                            boolean documentReturned, OMElement registryErrorList, Element sh, String classCode) {

        logger.info("method prepareEventLogForRetrieve({})", classCode);
        if (classCode == null) {
            // In case the document is not found, audit log cannot be properly filled, as we don't know the event type
            // Log this under Order Service
            eventLog.setEventType(EventType.ORDER_SERVICE_RETRIEVE);
            eventLog.setEI_TransactionName(TransactionName.ORDER_SERVICE_RETRIEVE);
        } else {
            switch (classCode) {
                case Constants.EP_CLASSCODE:
                    eventLog.setEventType(EventType.ORDER_SERVICE_RETRIEVE);
                    eventLog.setEI_TransactionName(TransactionName.ORDER_SERVICE_RETRIEVE);
                    break;
                case Constants.PS_CLASSCODE:
                    eventLog.setEventType(EventType.PATIENT_SERVICE_RETRIEVE);
                    eventLog.setEI_TransactionName(TransactionName.PATIENT_SERVICE_RETRIEVE);
                    break;
                case Constants.MRO_CLASSCODE:
                    eventLog.setEventType(EventType.MRO_RETRIEVE);
                    eventLog.setEI_TransactionName(TransactionName.MRO_SERVICE_RETRIEVE);
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    eventLog.setEventType(EventType.ORCD_SERVICE_RETRIEVE);
                    eventLog.setEI_TransactionName(TransactionName.ORCD_SERVICE_RETRIEVE);
                    break;
                default:
                    break;
            }
        }

        eventLog.setEI_EventActionCode(EventActionCode.READ);
        eventLog.setEI_EventDateTime(DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar()));
        eventLog.getEventTargetParticipantObjectIds().add(request.getDocumentRequest().get(0).getDocumentUniqueId());

        if (!documentReturned) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
        } else if (!errorsDiscovered) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        } else {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.TEMPORAL_FAILURE);
        }

        String userIdAlias = Helper.getAssertionsSPProvidedId(sh);
        eventLog.setHR_UserID(StringUtils.isNotBlank(userIdAlias) ? userIdAlias : "" + "<" + Helper.getUserID(sh)
                + "@" + Helper.getAssertionsIssuer(sh) + ">");
        eventLog.setHR_AlternativeUserID(Helper.getAlternateUserID(sh));
        eventLog.setHR_RoleID(Helper.getFunctionalRoleID(sh));
        eventLog.setSP_UserID(HTTPUtil.getSubjectDN(true));
        eventLog.setPT_ParticipantObjectID(Helper.getDocumentEntryPatientIdFromTRCAssertion(sh));
        eventLog.setAS_AuditSourceId(Constants.COUNTRY_PRINCIPAL_SUBDIVISION);

        if (errorsDiscovered) {
            Iterator<OMElement> re = registryErrorList.getChildElements();
            //Include only the first error in the audit log.
            if (re.hasNext()) {
                OMElement error = re.next();
                if (logger.isDebugEnabled()) {
                    try {
                        logger.debug("Error to be included in audit: '{}'", XMLUtil.prettyPrint(XMLUtils.toDOM(error)));
                    } catch (Exception e) {
                        logger.debug("Exception: '{}'", e.getMessage(), e);
                    }
                }
                eventLog.setEM_ParticipantObjectID(error.getAttributeValue(new QName("", "errorCode")));
                eventLog.setEM_ParticipantObjectDetail(error.getAttributeValue(new QName("", "codeContext")).getBytes());
            }
        }
    }

    private List<String> getDocumentEntryClassCodes(AdhocQueryRequest request) {
        List<String> classCodes = new ArrayList<>();
        for (SlotType1 slotType1 : request.getAdhocQuery().getSlot()) {
            if (slotType1.getName().equals("$XDSDocumentEntryClassCode")) {
                var fullClassCodeString = slotType1.getValueList().getValue().get(0);
                var pattern = "\\(?\\)?\\'?";
                fullClassCodeString = fullClassCodeString.replaceAll(pattern, "");
                String[] classCodeString = fullClassCodeString.split(",");
                for (String classCode : classCodeString) {
                    classCode = classCode.substring(0, classCode.indexOf("^^"));
                    classCodes.add(classCode);
                }
            }
        }
        return classCodes;
    }

    private String getClassCode(List<String> classCodeList){

        List<String> list = Arrays.asList(
                Constants.EP_CLASSCODE,
                Constants.PS_CLASSCODE,
                Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE,
                Constants.ORCD_LABORATORY_RESULTS_CLASSCODE,
                Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE,
                Constants.ORCD_MEDICAL_IMAGES_CLASSCODE);

        for (String classCode : classCodeList) {
            if (list.contains(classCode)) {
                return classCode;
            }
        }

        return null;
    }

    /**
     * Extracts the XDS patient ID from the XCA query
     */
    private String getDocumentEntryPatientId(AdhocQueryRequest request) {

        for (SlotType1 sl : request.getAdhocQuery().getSlot()) {
            if (sl.getName().equals("$XDSDocumentEntryPatientId")) {
                String patientId = sl.getValueList().getValue().get(0);
                patientId = patientId.substring(1, patientId.length() - 1);
                return patientId;
            }
        }
        return "$XDSDocumentEntryPatientId Not Found!";
    }

    private FilterParams getFilterParams(AdhocQueryRequest request) {

        var filterParams = new FilterParams();

        for (SlotType1 sl : request.getAdhocQuery().getSlot()) {
            switch (sl.getName()) {
                case XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERCREATEDAFTER_SLOT_NAME:
                    filterParams.setCreatedAfter(Instant.parse(sl.getValueList().getValue().get(0)));
                    break;
                case XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERCREATEDBEFORE_SLOT_NAME:
                    filterParams.setCreatedBefore(Instant.parse(sl.getValueList().getValue().get(0)));
                    break;
                case XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERMAXIMUMSIZE_SLOT_NAME:
                    filterParams.setMaximumSize(Long.parseLong(sl.getValueList().getValue().get(0)));
                    break;
                default:
                    break;
            }
        }
        return filterParams;
    }

    /**
     * Extracts the patient ID used in epSOS transactions from the XCA query
     */
    private String getEpSOSPatientId(AdhocQueryRequest request) {

        String docPatientId = getDocumentEntryPatientId(request);
        return trimDocumentEntryPatientId(docPatientId);
    }

    /**
     * Extracts repositoryUniqueId from request
     *
     * @return repositoryUniqueId
     */
    private String getRepositoryUniqueId(RetrieveDocumentSetRequestType request) {

        return request.getDocumentRequest().get(0).getRepositoryUniqueId();
    }

    private String trimDocumentEntryPatientId(String patientId) {

        if (patientId.contains("^^^")) {
            return patientId.substring(0, patientId.indexOf("^^^"));
        }
        return patientId;
    }

    private ExternalIdentifierType makeExternalIdentifier(String identificationScheme, String registryObject, String value, String name) {

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        var externalIdentifierType = ofRim.createExternalIdentifierType();
        externalIdentifierType.setId(uuid);
        externalIdentifierType.setIdentificationScheme(identificationScheme);
        externalIdentifierType.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
        externalIdentifierType.setRegistryObject(registryObject);
        externalIdentifierType.setValue(value);

        externalIdentifierType.setName(ofRim.createInternationalStringType());
        externalIdentifierType.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        externalIdentifierType.getName().getLocalizedString().get(0).setValue(name);
        return externalIdentifierType;
    }

    private String prepareExtrinsicObjectEpsosDoc(DocumentType docType, Date effectiveTime, String repositoryId,
                                                  AdhocQueryRequest request, ExtrinsicObjectType eot, boolean isPDF,
                                                  String documentId, String confidentialityCode,
                                                  String confidentialityDisplay, String languageCode) {

        final String title;
        final String classCode;
        final String nodeRepresentation;
        final String displayName;

        switch (docType) {

            case PATIENT_SUMMARY:
                title = Constants.PS_TITLE;
                classCode = Constants.PS_CLASSCODE;
                nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.PatientSummary.EpsosPivotCoded.NODE_REPRESENTATION;
                displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.PatientSummary.EpsosPivotCoded.DISPLAY_NAME;
                break;
            case MRO:
                title = Constants.MRO_TITLE;
                classCode = Constants.MRO_CLASSCODE;
                nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.Mro.EpsosPivotCoded.NODE_REPRESENTATION;
                displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.Mro.EpsosPivotCoded.DISPLAY_NAME;
                break;
            default:
                logger.error("Unsupported document for query in OpenNCP. Requested document type: {}", docType.name());
                return "";
        }

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType(XCAConstants.XDS_DOC_ENTRY_CLASSIFICATION_NODE);

        // Status
        eot.setMimeType(MediaType.TEXT_XML_VALUE);

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(title);

        // Description (optional)
        eot.setDescription(ofRim.createInternationalStringType());
        eot.getDescription().getLocalizedString().add(ofRim.createLocalizedStringType());
        if (isPDF) {
            eot.getDescription().getLocalizedString().get(0)
                    .setValue("The " + title + " document (CDA L1 / PDF body) for patient " + getEpSOSPatientId(request));
        } else {
            eot.getDescription().getLocalizedString().get(0)
                    .setValue("The " + title + " document (CDA L3 / Structured body) for patient " + getEpSOSPatientId(request));
        }

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1.1");

        // Creation Date (optional)
        eot.getSlot().add(SlotBuilder.build("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", effectiveTime)));

        // Source Patient Id
        eot.getSlot().add(SlotBuilder.build("sourcePatientId", getDocumentEntryPatientId(request)));

        // LanguageCode (optional)
        eot.getSlot().add(SlotBuilder.build("languageCode", languageCode == null ? Constants.LANGUAGE_CODE : languageCode));

        // repositoryUniqueId (optional)
        eot.getSlot().add(SlotBuilder.build("repositoryUniqueId", repositoryId));

        eot.getClassification().add(ClassificationBuilder.build(XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME,
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Confidentiality Code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid, confidentialityCode, "2.16.840.1.113883.5.25", confidentialityDisplay));
        // FormatCode
        if (isPDF) {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.NODE_REPRESENTATION,
                    "IHE PCC", XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.DISPLAY_NAME));
        } else {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, nodeRepresentation, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.CODING_SCHEME, displayName));
        }

        /*
         * Healthcare facility code
         * TODO: Get healthcare facility info from national implementation
         */
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid, Constants.COUNTRY_CODE, "1.0.3166.1", Constants.COUNTRY_NAME));

        // Practice Setting code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid, "Not Used", "eHDSI Practice Setting Codes-Not Used", "Not Used"));

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid, getDocumentEntryPatientId(request), "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_SCHEME,
                uuid, documentId, XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR));

        return uuid;
    }

    /**
     * Method to build the Extrinsic object to be used for the XCA Query service for OrCD documents.
     *
     * @param docType
     * @param effectiveTime
     * @param serviceStartTime
     * @param repositoryId
     * @param request
     * @param eot
     * @param documentId
     * @param confidentialityCode
     * @param confidentialityDisplay
     * @param languageCode
     * @param classCode
     * @param documentFileType
     * @param size
     * @param authors
     * @param reasonOfHospitalisation
     * @return
     */
    private String prepareExtrinsicObjectOrCD(DocumentType docType, Date effectiveTime, Date serviceStartTime,
                                              String repositoryId, AdhocQueryRequest request, ExtrinsicObjectType eot,
                                              String documentId, String confidentialityCode, String confidentialityDisplay,
                                              String languageCode, String classCode,
                                              OrCDDocumentMetaData.DocumentFileType documentFileType, long size,
                                              List<OrCDDocumentMetaData.Author> authors,
                                              OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        final String title;
        final String nodeRepresentation;
        final String displayName;

        switch (classCode) {
            case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                title = Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_TITLE;
                nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.NODE_REPRESENTATION;
                displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.DISPLAY_NAME;
                break;
            case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                title = Constants.ORCD_LABORATORY_RESULTS_TITLE;
                nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.NODE_REPRESENTATION;
                displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.DISPLAY_NAME;
                break;
            case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                title = Constants.ORCD_MEDICAL_IMAGING_REPORTS_TITLE;
                nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.NODE_REPRESENTATION;
                displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PdfSourceCoded.DISPLAY_NAME;
                break;
            case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                title = Constants.ORCD_MEDICAL_IMAGES_TITLE;
                switch (documentFileType) {
                    case PNG:
                        nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PngSourceCoded.NODE_REPRESENTATION;
                        displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.PngSourceCoded.DISPLAY_NAME;
                        break;
                    case JPEG:
                        nodeRepresentation = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.JpegSourceCoded.NODE_REPRESENTATION;
                        displayName = XCAConstants.EXTRINSIC_OBJECT.FormatCode.OrCD.JpegSourceCoded.DISPLAY_NAME;
                        break;
                    default:
                        logger.error("Unsupported document file type '{}' for OrCD Medical Images", documentFileType);
                        return "";
                }
                break;
            default:
                logger.error("Unsupported classCode for OrCD query in OpenNCP. Requested classCode: {}", classCode);
                return "";
        }

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType(XCAConstants.XDS_DOC_ENTRY_CLASSIFICATION_NODE);

        // MimeType
        eot.setMimeType(MediaType.TEXT_XML_VALUE);

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(title);

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1.1");

        // Creation Date (optional)
        eot.getSlot().add(SlotBuilder.build("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", effectiveTime)));

        // Service Start time (optional)
        eot.getSlot().add(SlotBuilder.build("serviceStartTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", serviceStartTime)));

        // Source Patient Id
        eot.getSlot().add(SlotBuilder.build("sourcePatientId", getDocumentEntryPatientId(request)));

        // Size
        eot.getSlot().add(SlotBuilder.build("size", String.valueOf(size)));

        // LanguageCode (optional)
        eot.getSlot().add(SlotBuilder.build("languageCode", languageCode == null ? Constants.LANGUAGE_CODE : languageCode));

        // repositoryUniqueId (optional)
        eot.getSlot().add(SlotBuilder.build("repositoryUniqueId", repositoryId));

        eot.getClassification().add(ClassificationBuilder.build(XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME,
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Confidentiality Code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid, confidentialityCode, "2.16.840.1.113883.5.25", confidentialityDisplay));
        // FormatCode
        eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                uuid, nodeRepresentation, "eHDSI formatCodes", displayName));

        /*
         * Healthcare facility code
         * TODO: Get healthcare facility info from national implementation
         */
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid, Constants.COUNTRY_CODE, "1.0.3166.1", Constants.COUNTRY_NAME));

        // Reason of hospitalisation
        if (reasonOfHospitalisation != null) {
            eot.getClassification().add(ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, reasonOfHospitalisation.getCode(), reasonOfHospitalisation.getCodingScheme(), reasonOfHospitalisation.getText()));
        }

        // Practice Setting code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid, "Not Used", "eHDSI Practice Setting Codes-Not Used", "Not Used"));

        for (OrCDDocumentMetaData.Author author : authors) {
            ClassificationType classificationAuthor = ClassificationBuilder.build(IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID,
                    uuid, "");

            if (author.getAuthorPerson() != null) {
                SlotType1 authorPersonSlot = SlotBuilder.build(IheConstants.AUTHOR_PERSON_STR, author.getAuthorPerson());
                classificationAuthor.getSlot().add(authorPersonSlot);
            }

            if (author.getAuthorSpeciality() != null && !author.getAuthorSpeciality().isEmpty()) {
                SlotType1 authorSpecialtySlot = SlotBuilder.build(IheConstants.AUTHOR_SPECIALITY_STR, author.getAuthorSpeciality());
                classificationAuthor.getSlot().add(authorSpecialtySlot);
            }
            eot.getClassification().add(classificationAuthor);
        }

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid, getDocumentEntryPatientId(request), "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_SCHEME,
                uuid, documentId, XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR));

        return uuid;
    }

    private String prepareExtrinsicObjectEP(AdhocQueryRequest request, ExtrinsicObjectType eot, EPDocumentMetaData document) {

        var name = "eHDSI - ePrescription";
        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        boolean isPDF = document.getFormat() == EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF;

        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType(XCAConstants.XDS_DOC_ENTRY_CLASSIFICATION_NODE);

        // Status
        eot.setMimeType(MediaType.TEXT_XML_VALUE);

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(name);

        // Description
        eot.setDescription(ofRim.createInternationalStringType());
        eot.getDescription().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getDescription().getLocalizedString().get(0).setValue(document.getDescription());

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1");

        // Creation Date (optional)
        eot.getSlot().add(SlotBuilder.build("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", document.getEffectiveTime())));

        // Source Patient Id
        eot.getSlot().add(SlotBuilder.build("sourcePatientId", getDocumentEntryPatientId(request)));

        // LanguageCode (optional)
        String languageCode = document.getLanguage() == null ? Constants.LANGUAGE_CODE : document.getLanguage();
        eot.getSlot().add(SlotBuilder.build("languageCode", languageCode));

        // repositoryUniqueId (optional)
        eot.getSlot().add(SlotBuilder.build("repositoryUniqueId", document.getRepositoryId()));

        eot.getClassification().add(
                ClassificationBuilder.build(
                        XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME, uuid,
                        Constants.EP_CLASSCODE, "2.16.840.1.113883.6.1", name));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid, Constants.EP_CLASSCODE, "2.16.840.1.113883.6.1", name));

        // Dispensable
        if (document.isDispensable()) {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:open", "1.3.6.1.4.1.19376.1.2.3", "Open");
            dispensableClassification.getSlot().add(SlotBuilder.build("dispensable", "Open"));
            eot.getClassification().add(dispensableClassification);
        } else {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:closed", "1.3.6.1.4.1.19376.1.2.3", "Closed");
            dispensableClassification.getSlot().add(SlotBuilder.build("dispensable", "Closed"));
            eot.getClassification().add(dispensableClassification);
        }

        // ATC code (former Product element)
        ClassificationType atcCodeClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getAtcCode(), "2.16.840.1.113883.6.73", document.getAtcName());
        eot.getClassification().add(atcCodeClassification);

        // Dose Form Code
        ClassificationType doseFormClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getDoseFormCode(), "0.4.0.127.0.16.1.1.2.1", document.getDoseFormName());
        eot.getClassification().add(doseFormClassification);

        // Strength
        ClassificationType strengthClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getStrength(), "eHDSI_Strength_CodeSystem", "Strength of medication");
        eot.getClassification().add(strengthClassification);

        // Substitution
        String substitutionCode = document.getSubstitution() != null
                ? document.getSubstitution().getSubstitutionCode()
                : SubstitutionCodeEnum.G.name();
        String substitutionDisplay = document.getSubstitution() != null
                ? document.getSubstitution().getSubstitutionDisplayName()
                : SubstitutionCodeEnum.G.getDisplayName();
        ClassificationType substitutionClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                substitutionCode, "2.16.840.1.113883.5.1070", substitutionDisplay);
        eot.getClassification().add(substitutionClassification);

        // Confidentiality Code
        String confidentialityCode = document.getConfidentiality() != null
                && document.getConfidentiality().getConfidentialityCode() != null
                ? document.getConfidentiality().getConfidentialityCode()
                : SimpleConfidentialityEnum.N.name();
        String confidentialityDisplay = document.getConfidentiality() != null
                && document.getConfidentiality().getConfidentialityDisplay() != null
                ? document.getConfidentiality().getConfidentialityDisplay()
                : SimpleConfidentialityEnum.N.getDisplayName();
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid, confidentialityCode, "2.16.840.1.113883.5.25", confidentialityDisplay));
        // FormatCode
        if (isPDF) {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.NODE_REPRESENTATION, "IHE PCC",
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.DISPLAY_NAME));
        } else {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.NODE_REPRESENTATION,
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.CODING_SCHEME,
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.DISPLAY_NAME));
        }
        // Healthcare facility code
        // TODO: Get healthcare facility info from national implementation
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid, Constants.COUNTRY_CODE, "1.0.3166.1", Constants.COUNTRY_NAME));

        // Practice Setting code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid, "Not Used", "eHDSI Practice Setting Codes-Not Used", "Not Used"));

        // Author Person
        ClassificationType authorClassification = ClassificationBuilder.build(
                IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID, uuid, "");
        authorClassification.getSlot().add(SlotBuilder.build(IheConstants.AUTHOR_PERSON_STR, document.getAuthor()));
        eot.getClassification().add(authorClassification);

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid, getDocumentEntryPatientId(request), "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_SCHEME,
                uuid, document.getId(), XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR));

        return uuid;
    }

    private AssociationType1 makeAssociation(String source, String target) {

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        var association = ofRim.createAssociationType1();
        association.setId(uuid);
        association.setAssociationType("urn:ihe:iti:2007:AssociationType:XFRM");
        association.setSourceObject(source);
        association.setTargetObject(target);
        //  Gazelle does not like this information when validating. Uncomment if really needed.
        //        association.getClassification().add(ClassificationBuilder.build(
        //                "urn:uuid:abd807a3-4432-4053-87b4-fd82c643d1f3",
        //                uuid,
        //                "epSOS pivot",
        //                "epSOS translation types",
        //                "Translation into epSOS pivot format"));
        return association;
    }

    private String getLocation(String location) {

        //TODO: to be reviewed in the future linked with JIRA EHNCP-1131.
        //  String location = ConfigurationManagerFactory.getConfigurationManager()
        //      .getEndpointUrl(Constants.COUNTRY_CODE.toLowerCase(Locale.ENGLISH), RegisteredService.PATIENT_SERVICE);
        return StringUtils.isBlank(location)?  Constants.OID_PREFIX + Constants.HOME_COMM_ID : location;
    }

    private void addErrorMessage(RegistryErrorList registryErrorList, OpenncpErrorCode openncpErrorCode, String codeContext, String value, RegistryErrorSeverity severity) {
        registryErrorList.getRegistryError().add(createErrorMessage(openncpErrorCode.getCode(), codeContext, value, null, severity));
    }

    private void addErrorMessage(RegistryErrorList registryErrorList, OpenncpErrorCode openncpErrorCode, String codeContext, String value, String location , RegistryErrorSeverity severity) {
        registryErrorList.getRegistryError().add(createErrorMessage(openncpErrorCode.getCode(), codeContext, value, location, severity));
    }


    private void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, OpenncpErrorCode openncpErrorCode, String codeContext, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, openncpErrorCode.getCode(), codeContext, "", null, severity));
    }

    private void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, IheErrorCode iheErrorCode, String codeContext, String value, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, iheErrorCode.getCode(), codeContext, value, null, severity));
    }

    private void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, OpenncpErrorCode openncpErrorCode, String codeContext, String location, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, openncpErrorCode.getCode(), codeContext, "", location, severity));
    }

    private void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, ITMTSAMEror error, String operationType, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons,
                error.getCode(),
                error.getDescription(),
                "ECDATransformationHandler.Error." + operationType + "(" + error.getCode() + " / " + error.getDescription() + ")",
                null,
                severity));
    }

    private RegistryError createErrorMessage(String errorCode, String codeContext, String value, String location, RegistryErrorSeverity severity) {

        var registryError = ofRs.createRegistryError();
        registryError.setErrorCode(errorCode);
        registryError.setLocation(getLocation(location));
        registryError.setSeverity(severity.getText());
        registryError.setCodeContext(codeContext);
        registryError.setValue(value);
        return registryError;
    }

    private OMElement createErrorOMMessage(OMNamespace ons, String errorCode, String codeContext, String value, String location,  RegistryErrorSeverity severity) {

        var registryError = omFactory.createOMElement("RegistryError", ons);
        registryError.addAttribute(omFactory.createOMAttribute("codeContext", null, codeContext));
        registryError.addAttribute(omFactory.createOMAttribute("errorCode", null, errorCode));
        String aux = severity != null? severity.getText() : null ;
        registryError.addAttribute(omFactory.createOMAttribute("severity", null, aux));
        // EHNCP-1131
        registryError.addAttribute(omFactory.createOMAttribute("location", null, getLocation(location)));
        registryError.setText(value);

        return registryError;
    }

    /**
     * Main part of the XCA query operation implementation, fills the AdhocQueryResponse with details
     */
    private void adhocQueryResponseBuilder(AdhocQueryRequest request, AdhocQueryResponse response, SOAPHeader soapHeader,
                                           EventLog eventLog) throws Exception {

        String sigCountryCode = null;
        Element shElement = null;
        String responseStatus = AdhocQueryResponseStatus.FAILURE;
        // What's being requested: eP or PS?
        List<String> classCodeValues = getDocumentEntryClassCodes(request);
        var registryErrorList = ofRs.createRegistryErrorList();
        // Create Registry Object List
        response.setRegistryObjectList(ofRim.createRegistryObjectListType());

        try {
            shElement = XMLUtils.toDOM(soapHeader);
            documentSearchService.setSOAPHeader(shElement);
            sigCountryCode = SAML2Validator.validateXCAHeader(shElement, getClassCode(classCodeValues));
        } catch (OpenncpErrorCodeException e) {
            logger.debug(e.getMessage(), e);
            addErrorMessage(registryErrorList, e.getOpenncpErrorCode(),  e.getMessage(), "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
        } catch (Exception e) {
            OpenncpErrorCode code = OpenncpErrorCode.ERROR_GENERIC;
            switch (getClassCode(classCodeValues)){
                case Constants.EP_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_EP_GENERIC;
                    break;
                case Constants.PS_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_PS_GENERIC;
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_ORCD_GENERIC;
                    break;
            }
            addErrorMessage(registryErrorList, code, e.getMessage(), "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
            throw e;
        }

        String fullPatientId = trimDocumentEntryPatientId(Helper.getDocumentEntryPatientIdFromTRCAssertion(shElement));
        if (!getDocumentEntryPatientId(request).contains(fullPatientId)) {
            // Patient ID in TRC assertion does not match the one given in the request. Return "No documents found".
            OpenncpErrorCode code = OpenncpErrorCode.ERROR_DOCUMENT_NOT_FOUND;
            switch (getClassCode(classCodeValues)) {
                case Constants.EP_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_EP_NOT_FOUND;
                    break;
                case Constants.PS_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_PS_NOT_FOUND;
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    code = OpenncpErrorCode.ERROR_ORCD_NOT_FOUND;
                    break;
            }
            addErrorMessage(registryErrorList, code, code.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);

        }
        String patientId = trimDocumentEntryPatientId(fullPatientId);
        var countryCode = "";
        String distinguishedName = eventLog.getSC_UserID();
        int cIndex = distinguishedName.indexOf("C=");

        if (cIndex > 0) {
            countryCode = distinguishedName.substring(cIndex + 2, cIndex + 4);
        } // Mustafa: This part is added for handling consents when the call is not https
        // In this case, we check the country code of the signature certificate that
        // ships within the HCP assertion
        // TODO: Might be necessary to remove later, although it does no harm in reality!
        else {
            logger.info("Could not get client country code from the service consumer certificate. " +
                    "The reason can be that the call was not via HTTPS. " +
                    "Will check the country code from the signature certificate now.");
            if (sigCountryCode != null) {
                logger.info("Found the client country code via the signature certificate.");
                countryCode = sigCountryCode;
            }
        }
        logger.info("The client country code to be used by the PDP: '{}'", countryCode);

        // Then, it is the Policy Decision Point (PDP) that decides according to the consent of the patient
        if (!SAML2Validator.isConsentGiven(patientId, countryCode)) {
            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_NO_CONSENT, OpenncpErrorCode.ERROR_NO_CONSENT.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
        }

        if (classCodeValues.isEmpty()) {
            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_GENERIC_SERVICE_SIGNIFIER_UNKNOWN, OpenncpErrorCode.ERROR_GENERIC_SERVICE_SIGNIFIER_UNKNOWN.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
        }

        // Evidence for call to NI for XCA List
        try {
            //  e-Sens: we MUST generate NRO when NCPA sends to NI. This was throwing errors because we were not
            //  passing an XML document. We're passing data like:"SearchCriteria: {patientId = 12445ASD}".
            //  So we provided a XML representation of such data.
            Assertion assertionTRC = Helper.getTRCAssertion(shElement);
            String messageUUID = UUIDHelper.encodeAsURN(assertionTRC.getID()) + "_" + assertionTRC.getIssueInstant();

            EvidenceUtils.createEvidenceREMNRO(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
                    Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD, Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                    Constants.SP_KEYSTORE_PATH, Constants.SP_KEYSTORE_PASSWORD, Constants.SP_PRIVATEKEY_ALIAS,
                    Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD, Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                    IHEEventType.PATIENT_SERVICE_LIST.getCode(), new DateTime(), EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                    "NI_XCA_LIST_REQ", messageUUID);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_SEC_GENERIC, OpenncpErrorCode.ERROR_SEC_GENERIC.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
        }

        for (String classCodeValue : classCodeValues) {
            try {
                switch (classCodeValue) {
                    case Constants.EP_CLASSCODE:
                        List<DocumentAssociation<EPDocumentMetaData>> prescriptions = documentSearchService.getEPDocumentList(
                                DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                        if (prescriptions == null) {

                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_EP_REGISTRY_NOT_ACCESSIBLE, OpenncpErrorCode.ERROR_EP_REGISTRY_NOT_ACCESSIBLE.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.FAILURE;
                        } else if (prescriptions.isEmpty()) {

                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_EP_NOT_FOUND, OpenncpErrorCode.ERROR_EP_NOT_FOUND.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.FAILURE;
                        } else {

                            // Multiple prescriptions mean multiple PDF and XML files, multiple ExtrinsicObjects and associations
                            responseStatus = AdhocQueryResponseStatus.SUCCESS;
                            for (DocumentAssociation<EPDocumentMetaData> prescription : prescriptions) {

                                logger.debug("Prescription Repository ID: '{}'", prescription.getXMLDocumentMetaData().getRepositoryId());
                                String xmlUUID;
                                var eotXML = ofRim.createExtrinsicObjectType();
                                xmlUUID = EPExtrinsicObjectBuilder.build(request, eotXML, prescription.getXMLDocumentMetaData());
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));

                                String pdfUUID;
                                var eotPDF = ofRim.createExtrinsicObjectType();
                                pdfUUID = EPExtrinsicObjectBuilder.build(request, eotPDF, prescription.getPDFDocumentMetaData());
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));

                                if (StringUtils.isNotBlank(xmlUUID) && StringUtils.isNotBlank(pdfUUID)) {

                                    response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                                }
                            }
                        }
                        break;
                    case Constants.PS_CLASSCODE:
                        DocumentAssociation<PSDocumentMetaData> psDoc = documentSearchService.getPSDocumentList(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                        if (psDoc == null || (psDoc.getPDFDocumentMetaData() == null && psDoc.getXMLDocumentMetaData() == null)) {

                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_PS_NOT_FOUND, "No patient summary is registered for the given patient.", "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.SUCCESS;
                        } else {

                            PSDocumentMetaData docPdf = psDoc.getPDFDocumentMetaData();
                            PSDocumentMetaData docXml = psDoc.getXMLDocumentMetaData();
                            responseStatus = AdhocQueryResponseStatus.SUCCESS;

                            var xmlUUID = "";
                            if (docXml != null) {
                                var eotXML = ofRim.createExtrinsicObjectType();
                                String confidentialityCode = docXml.getConfidentiality() == null
                                        || docXml.getConfidentiality().getConfidentialityCode() == null ? "N"
                                        : docXml.getConfidentiality().getConfidentialityCode();
                                String confidentialityDisplay = docXml.getConfidentiality() == null
                                        || docXml.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                                        : docXml.getConfidentiality().getConfidentialityDisplay();
                                String languageCode = docXml.getLanguage();
                                xmlUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.PATIENT_SUMMARY,
                                        docXml.getEffectiveTime(), docXml.getRepositoryId(), request, eotXML, false,
                                        docXml.getId(), confidentialityCode, confidentialityDisplay, languageCode);
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));
                            }
                            var pdfUUID = "";
                            if (docPdf != null) {
                                var eotPDF = ofRim.createExtrinsicObjectType();
                                String confidentialityCode = docPdf.getConfidentiality() == null
                                        || docPdf.getConfidentiality().getConfidentialityCode() == null ? "N"
                                        : docPdf.getConfidentiality().getConfidentialityCode();
                                String confidentialityDisplay = docPdf.getConfidentiality() == null
                                        || docPdf.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                                        : docPdf.getConfidentiality().getConfidentialityDisplay();
                                String languageCode = docPdf.getLanguage();
                                pdfUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.PATIENT_SUMMARY, docPdf.getEffectiveTime(), docPdf.getRepositoryId(), request, eotPDF, true,
                                        docPdf.getId(), confidentialityCode, confidentialityDisplay, languageCode);
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));
                            }
                            if (!xmlUUID.equals("") && !pdfUUID.equals("")) {
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                            }
                        }
                        break;
                    case Constants.MRO_CLASSCODE:
                        DocumentAssociation<MroDocumentMetaData> mro = documentSearchService.getMroDocumentList(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                        if (mro == null || (mro.getPDFDocumentMetaData() == null && mro.getXMLDocumentMetaData() == null)) {

                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_MRO_NO_DATA, OpenncpErrorCode.ERROR_MRO_NO_DATA.getDescription(), "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.SUCCESS;
                        } else {

                            MroDocumentMetaData docPdf = mro.getPDFDocumentMetaData();
                            MroDocumentMetaData docXml = mro.getXMLDocumentMetaData();

                            responseStatus = AdhocQueryResponseStatus.SUCCESS;

                            var xmlUUID = "";
                            if (docXml != null) {
                                var eotXML = ofRim.createExtrinsicObjectType();
                                String confidentialityCode = docXml.getConfidentiality() == null
                                        || docXml.getConfidentiality().getConfidentialityCode() == null ? "N"
                                        : docXml.getConfidentiality().getConfidentialityCode();
                                String confidentialityDisplay = docXml.getConfidentiality() == null
                                        || docXml.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                                        : docXml.getConfidentiality().getConfidentialityDisplay();
                                String languageCode = docXml.getLanguage();
                                xmlUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.MRO, docXml.getEffectiveTime(),
                                        docXml.getRepositoryId(), request, eotXML, false, docXml.getId(), confidentialityCode, confidentialityDisplay, languageCode);
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));
                            }
                            var pdfUUID = "";
                            if (docPdf != null) {
                                var eotPDF = ofRim.createExtrinsicObjectType();
                                String confidentialityCode = docPdf.getConfidentiality() == null
                                        || docPdf.getConfidentiality().getConfidentialityCode() == null ? "N"
                                        : docPdf.getConfidentiality().getConfidentialityCode();
                                String confidentialityDisplay = docPdf.getConfidentiality() == null
                                        || docPdf.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                                        : docPdf.getConfidentiality().getConfidentialityDisplay();
                                String languageCode = docPdf.getLanguage();
                                pdfUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.MRO, docPdf.getEffectiveTime(),
                                        docPdf.getRepositoryId(), request, eotPDF, true, docPdf.getId(), confidentialityCode, confidentialityDisplay, languageCode);
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));
                            }
                            if (!xmlUUID.equals("") && !pdfUUID.equals("")) {
                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                            }
                        }
                        break;
                    case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                    case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                        var searchCriteria = DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId);
                        var filterParams = getFilterParams(request);
                        if (filterParams.getMaximumSize() != null) {
                            searchCriteria.add(Criteria.MaximumSize, filterParams.getMaximumSize().toString());
                        }
                        if (filterParams.getCreatedBefore() != null) {
                            searchCriteria.add(Criteria.CreatedBefore, filterParams.getCreatedBefore().toString());
                        }
                        if (filterParams.getCreatedAfter() != null) {
                            searchCriteria.add(Criteria.CreatedAfter, filterParams.getCreatedAfter().toString());
                        }

                        List<OrCDDocumentMetaData> orCDDocumentMetaDataList = getOrCDDocumentMetaDataList(classCodeValue, searchCriteria);

                        if (orCDDocumentMetaDataList == null) {
                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_ORCD_GENERIC, "orCD registry could not be accessed.", "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.FAILURE;
                        } else if (orCDDocumentMetaDataList.isEmpty()) {
                            addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_ORCD_NOT_FOUND, "There is no original clinical data of the requested type registered for the given patient.", "", RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                            responseStatus = AdhocQueryResponseStatus.SUCCESS;
                        } else {

                            responseStatus = AdhocQueryResponseStatus.SUCCESS;
                            for (OrCDDocumentMetaData orCDDocumentMetaData : orCDDocumentMetaDataList) {
                                logger.debug("OrCD Document Repository ID: '{}'", orCDDocumentMetaData.getRepositoryId());
                                buildOrCDExtrinsicObject(request, response, orCDDocumentMetaData);
                            }
                        }
                        break;

                    default:
                        addErrorMessage(registryErrorList, OpenncpErrorCode.ERROR_GENERIC_SERVICE_SIGNIFIER_UNKNOWN, "Class code not supported for XCA query(" + classCodeValue + ").", "", RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                        responseStatus = AdhocQueryResponseStatus.SUCCESS;
                        break;
                }

                try {
                    prepareEventLogForQuery(eventLog, request, response, shElement, classCodeValue);
                } catch (Exception e) {
                    logger.error("Prepare Audit log failed: '{}'", e.getMessage(), e);
                    // Is this fatal?
                }
            } catch (NIException e){
                addErrorMessage(registryErrorList, e.getOpenncpErrorCode(), e.getOpenncpErrorCode().getDescription(), "", e.getMessage(), RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                responseStatus = AdhocQueryResponseStatus.FAILURE;
            }
        }

        if(!registryErrorList.getRegistryError().isEmpty()) {
            response.setRegistryErrorList(registryErrorList);
        }
        response.setStatus(responseStatus);
    }

    private List<OrCDDocumentMetaData> getOrCDDocumentMetaDataList(String classCode, SearchCriteria searchCriteria)
            throws NIException, InsufficientRightsException {

        List<OrCDDocumentMetaData> orCDDocumentMetaDataList = new ArrayList<>();
        switch (classCode) {
            case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                orCDDocumentMetaDataList = documentSearchService.getOrCDHospitalDischargeReportsDocumentList(searchCriteria);
                break;
            case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                orCDDocumentMetaDataList = documentSearchService.getOrCDLaboratoryResultsDocumentList(searchCriteria);
                break;
            case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                orCDDocumentMetaDataList = documentSearchService.getOrCDMedicalImagingReportsDocumentList(searchCriteria);
                break;
            case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                orCDDocumentMetaDataList = documentSearchService.getOrCDMedicalImagesDocumentList(searchCriteria);
                break;
            default:
                // eHDSI supports only 4 types of document.
                break;
        }

        return orCDDocumentMetaDataList;
    }

    private void buildOrCDExtrinsicObject(AdhocQueryRequest request, AdhocQueryResponse response, OrCDDocumentMetaData orCDDocumentMetaData) {

        var eotXML = ofRim.createExtrinsicObjectType();
        final String confidentialityCode = orCDDocumentMetaData.getConfidentiality() == null
                || orCDDocumentMetaData.getConfidentiality().getConfidentialityCode() == null ? "N"
                : orCDDocumentMetaData.getConfidentiality().getConfidentialityCode();
        final String confidentialityDisplay = orCDDocumentMetaData.getConfidentiality() == null
                || orCDDocumentMetaData.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                : orCDDocumentMetaData.getConfidentiality().getConfidentialityDisplay();
        final String languageCode = orCDDocumentMetaData.getLanguage();
        String xmlUUID = prepareExtrinsicObjectOrCD(DocumentType.ORCD, orCDDocumentMetaData.getEffectiveTime(),
                orCDDocumentMetaData.getServiceStartTime(), orCDDocumentMetaData.getRepositoryId(), request, eotXML,
                orCDDocumentMetaData.getId(), confidentialityCode, confidentialityDisplay, languageCode,
                orCDDocumentMetaData.getClassCode(), orCDDocumentMetaData.getDocumentFileType(),
                orCDDocumentMetaData.getSize(), orCDDocumentMetaData.getAuthors(),
                orCDDocumentMetaData.getReasonOfHospitalisation());
        response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));
        if (!StringUtils.isEmpty(xmlUUID)) {
            response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(xmlUUID, xmlUUID)));
        }
    }

    private Document transformDocument(Document doc, OMElement registryErrorList, OMElement registryResponseElement,
                                       boolean isTranscode, EventLog eventLog) {

        logger.debug("Transforming document, isTranscode: '{}' - Event Type: '{}'", isTranscode, eventLog.getEventType());
        if (eventLog.getReqM_ParticipantObjectDetail() != null) {
            var requester = new String(eventLog.getReqM_ParticipantObjectDetail());
            if (loggerClinical.isDebugEnabled() && !org.apache.commons.lang3.StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
                loggerClinical.debug("Participant Requester: '{}'", requester);
            }
        }
        if (eventLog.getResM_ParticipantObjectDetail() != null) {
            var responder = new String(eventLog.getResM_ParticipantObjectDetail());
            if (loggerClinical.isDebugEnabled() && !org.apache.commons.lang3.StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
                loggerClinical.debug("Participant Responder: '{}'", responder);
            }
        }

        Document returnDoc;
        try {
            TMResponseStructure tmResponse;
            String operationType;
            if (isTranscode) {
                operationType = "toEpSOSPivot";
                logger.debug("Transforming document to epSOS pivot...");
                tmResponse = transformationService.toEpSOSPivot(doc);
            } else {
                operationType = "translate";
                logger.debug("Translating document to '{}'", Constants.LANGUAGE_CODE);
                tmResponse = transformationService.translate(doc, Constants.LANGUAGE_CODE);
            }

            OMNamespace ns = registryResponseElement.getNamespace();
            var ons = omFactory.createOMNamespace(ns.getNamespaceURI(), "a");

            for (var i = 0; i < tmResponse.getErrors().size(); i++) {
                ITMTSAMEror error = tmResponse.getErrors().get(i);
                addErrorOMMessage(ons, registryErrorList,
                        error, operationType,
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
            }

            for (var i = 0; i < tmResponse.getWarnings().size(); i++) {
                ITMTSAMEror error = tmResponse.getWarnings().get(i);
                addErrorOMMessage(ons, registryErrorList,
                        error, operationType,
                        RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
            }

            returnDoc = tmResponse.getResponseCDA();
            if (registryErrorList.getChildElements().hasNext()) {
                registryResponseElement.addChild(registryErrorList);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return returnDoc;
    }

    private void retrieveDocumentSetBuilder(RetrieveDocumentSetRequestType request, SOAPHeader soapHeader,
                                            EventLog eventLog, OMElement omElement) throws Exception {

        var omNamespace = omFactory.createOMNamespace("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "");
        var registryResponse = omFactory.createOMElement("RegistryResponse", omNamespace);
        var registryErrorList = omFactory.createOMElement("RegistryErrorList", omNamespace);
        OMNamespace ns2 = omElement.getNamespace();
        var documentResponse = omFactory.createOMElement("DocumentResponse", ns2);

        var documentReturned = false;
        var failure = false;

        Element soapHeaderElement;
        String classCodeValue = null;

        // Start processing within a labeled block, break on certain errors
        processLabel:
        {
            try {
                soapHeaderElement = XMLUtils.toDOM(soapHeader);
            } catch (Exception e) {
                logger.error(null, e);
                throw e;
            }

            documentSearchService.setSOAPHeader(soapHeaderElement);

            String documentId = request.getDocumentRequest().get(0).getDocumentUniqueId();
            String patientId = trimDocumentEntryPatientId(Helper.getDocumentEntryPatientIdFromTRCAssertion(soapHeaderElement));
            String repositoryId = getRepositoryUniqueId(request);
            if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                loggerClinical.debug("Retrieving clinical document by criteria:\nPatient ID: '{}'\nDocument ID: '{}'\nRepository ID: '{}'",
                        patientId, documentId, repositoryId);
            }
            //try getting country code from the certificate
            String countryCode = null;
            String distinguishedName = eventLog.getSC_UserID();
            logger.info("[Certificate] Distinguished Name: '{}'", distinguishedName);
            int cIndex = distinguishedName.indexOf("C=");
            if (cIndex > 0) {
                countryCode = distinguishedName.substring(cIndex + 2, cIndex + 4);
            }
            // Mustafa: This part is added for handling consents when the call is not https. In this case, we check
            // the country code of the signature certificate that ships within the HCP assertion
            // TODO: Might be necessary to remove later, although it does no harm in reality!
            if (countryCode == null) {
                logger.info("Could not get client country code from the service consumer certificate. " +
                        "The reason can be that the call was not via HTTPS. " +
                        "Will check the country code from the signature certificate now.");
                countryCode = SAML2Validator.getCountryCodeFromHCPAssertion(soapHeaderElement);
                if (countryCode != null) {
                    logger.info("Found the client country code via the signature certificate.");
                } else {
                    addErrorOMMessage(omNamespace, registryErrorList,
                            OpenncpErrorCode.ERROR_INSUFFICIENT_RIGHTS,
                            OpenncpErrorCode.ERROR_INSUFFICIENT_RIGHTS.getDescription(),
                            RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                    break processLabel;
                }
            }

            logger.info("The client country code to be used by the PDP '{}' ", countryCode);

            // Then, it is the Policy Decision Point (PDP) that decides according to the consent of the patient
            if (!SAML2Validator.isConsentGiven(patientId, countryCode)) {
                addErrorOMMessage(omNamespace, registryErrorList,
                        OpenncpErrorCode.ERROR_NO_CONSENT,
                        OpenncpErrorCode.ERROR_NO_CONSENT.getDescription(),
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                break processLabel;
            }

            // Evidence for call to NI for XCA Retrieve
            /* Joao: we MUST generate NRO when NCPA sends to NI.This was throwing errors because we were not passing a XML document.
                We're passing data like:
                "SearchCriteria: {patientId = 12445ASD}"
                So we provided a XML representation of such data */
            try {
                EvidenceUtils.createEvidenceREMNRO(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
                        Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD, Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                        Constants.SP_KEYSTORE_PATH, Constants.SP_KEYSTORE_PASSWORD, Constants.SP_PRIVATEKEY_ALIAS,
                        Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD, Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                        IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode(), new DateTime(),
                        EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(), "NI_XCA_RETRIEVE_REQ",
                        Helper.getTRCAssertion(soapHeaderElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
            } catch (Exception e) {
                logger.error("createEvidenceREMNRO: '{}'", ExceptionUtils.getStackTrace(e), e);
            }

            //TODO: EHNCP-1271 - Shall we indicate a specific ERROR Code???
            EPSOSDocument epsosDoc;
            try {
                epsosDoc = documentSearchService.getDocument(DocumentFactory.createSearchCriteria()
                        .add(Criteria.DocumentId, documentId)
                        .add(Criteria.PatientId, patientId)
                        .add(Criteria.RepositoryId, repositoryId));
                //  TODO: EHNCP-2055 Inconsistency in handling patient id
            } catch (NIException e) {
                logger.error("NIException: '{}'", e.getMessage(), e);
                 addErrorOMMessage(omNamespace, registryErrorList,
                        e.getOpenncpErrorCode(),
                        e.getOpenncpErrorCode().getDescription(),
                         e.getMessage(), RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                break processLabel;
            }

            if (epsosDoc == null) {

                //  Evidence for response from NI in case of failure
                //  This should be NRR of NCPA receiving from NI. This was throwing errors because we were not passing a XML document.
                //  We're passing data like: "SearchCriteria: {patientId = 12445ASD}"
                //  So we provided a XML representation of such data. Still, evidence is generated based on request data, not response.
                //  This NRR is optional as per the CP. So we leave this commented.
//                try {
//                    EvidenceUtils.createEvidenceREMNRR(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                            IHEEventType.epsosPatientServiceRetrieve.getCode(),
//                            new DateTime(),
//                            EventOutcomeIndicator.TEMPORAL_FAILURE.getCode().toString(),
//                            "NI_XCA_RETRIEVE_RES_FAIL",
//                            Helper.getTRCAssertion(soapHeaderElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
//                } catch (Exception e) {
//                    logger.error(ExceptionUtils.getStackTrace(e));
//                }
                logger.error("[National Connector] No document returned by the National Infrastructure");
                addErrorOMMessage(omNamespace, registryErrorList,
                        IheErrorCode.XDSMissingDocument,
                        OpenncpErrorCode.ERROR_GENERIC_DOCUMENT_MISSING.getCode() + " : " + OpenncpErrorCode.ERROR_GENERIC_DOCUMENT_MISSING.getDescription(),
                        "Requested document not found.",
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                break processLabel;
            }

            // Evidence for response from NI in case of success
            /* Joao: This should be NRR of NCPA receiving from NI.
                    This was throwing errors because we were not passing a XML document.
                    We're passing data like:
                    "SearchCriteria: {patientId = 12445ASD}"
                    So we provided a XML representation of such data. Still, evidence is generated based on request data, not response.
                    This NRR is optional as per the CP. So we leave this commented */
//            try {
//                EvidenceUtils.createEvidenceREMNRR(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
//                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                        tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                        IHEEventType.epsosPatientServiceRetrieve.getCode(),
//                        new DateTime(),
//                        EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                        "NI_XCA_RETRIEVE_RES_SUCC",
//                        DateUtil.getCurrentTimeGMT());
//            } catch (Exception e) {
//                logger.error(ExceptionUtils.getStackTrace(e));
//            }

            classCodeValue = epsosDoc.getClassCode();

            try {
                SAML2Validator.validateXCAHeader(soapHeaderElement, classCodeValue);
            } catch (OpenncpErrorCodeException e) {
                logger.error("OpenncpErrorCodeException: '{}'", e.getMessage(), e);
                addErrorOMMessage(omNamespace, registryErrorList,
                        e.getOpenncpErrorCode(),
                        e.getMessage(),
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                break processLabel;
            } catch (SMgrException e) {
                logger.error("SMgrException: '{}'", e.getMessage(), e);
                addErrorOMMessage(omNamespace, registryErrorList,
                        OpenncpErrorCode.ERROR_SEC_GENERIC,
                        e.getMessage(),
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                break processLabel;
            }

            logger.info("XCA Retrieve Request is valid.");
            var homeCommunityId = omFactory.createOMElement("HomeCommunityId", ns2);
            homeCommunityId.setText(request.getDocumentRequest().get(0).getHomeCommunityId());
            documentResponse.addChild(homeCommunityId);

            var repositoryUniqueId = omFactory.createOMElement("RepositoryUniqueId", ns2);
            repositoryUniqueId.setText(request.getDocumentRequest().get(0).getRepositoryUniqueId());
            documentResponse.addChild(repositoryUniqueId);

            var documentUniqueId = omFactory.createOMElement("DocumentUniqueId", ns2);
            documentUniqueId.setText(documentId);
            documentResponse.addChild(documentUniqueId);

            var mimeType = omFactory.createOMElement("mimeType", ns2);
            mimeType.setText(MediaType.TEXT_XML_VALUE);
            documentResponse.addChild(mimeType);

            var document = omFactory.createOMElement("Document", omFactory.createOMNamespace("urn:ihe:iti:xds-b:2007", ""));
            logger.info("XCA Retrieve Response has been created.");
            try {
                Document doc = epsosDoc.getDocument();
                logger.info("Client userID: '{}'", eventLog.getSC_UserID());

                if (doc != null) {
                    logger.info("[National Infrastructure] CDA Document:\n'{}'", epsosDoc.getClassCode());
                    /* Validate CDA eHDSI Friendly */
                    if (OpenNCPValidation.isValidationEnable()) {

                        OpenNCPValidation.validateCdaDocument(XMLUtil.documentToString(epsosDoc.getDocument()),
                                NcpSide.NCP_A, epsosDoc.getClassCode(), false);
                    }
                    // Transcode to eHDSI Pivot
                    if (!Constants.getClassCodesOrCD().contains(classCodeValue)) {
                        doc = transformDocument(doc, registryErrorList, registryResponse, true, eventLog);
                    }
                    if (!checkIfOnlyWarnings(registryErrorList)) {

                        // If the transformation process has raised at least one FATAL Error, we should determine which
                        // XCAError code has to be provided according the corresponding TM Error Code
                        Iterator<OMElement> errors = registryErrorList.getChildElements();
                        while (errors.hasNext()) {

                            OMElement errorCode = errors.next();
                            logger.error("Error: '{}'-'{}'", errorCode.getText(), errorCode.getAttributeValue(QName.valueOf("errorCode")));
                            logger.error("TRANSCODING ERROR: '{}'-'{}'", TMError.ERROR_REQUIRED_CODED_ELEMENT_NOT_TRANSCODED.getCode(),
                                    errorCode.getAttributeValue(QName.valueOf("errorCode")));

                            if (StringUtils.startsWith(errorCode.getAttributeValue(QName.valueOf("errorCode")), "45")) {

                                OpenncpErrorCode openncpErrorCode = OpenncpErrorCode.ERROR_TRANSCODING_ERROR;

                                switch (classCodeValue){
                                    case Constants.EP_CLASSCODE:
                                        openncpErrorCode = OpenncpErrorCode.WARNING_EP_MISSING_EXPECTED_MAPPING;
                                        break;
                                    case Constants.PS_CLASSCODE:
                                        openncpErrorCode = OpenncpErrorCode.WARNING_PS_MISSING_EXPECTED_MAPPING;
                                        break;
                                }

                                addErrorOMMessage(omNamespace, registryErrorList,
                                        openncpErrorCode,
                                        openncpErrorCode.getDescription(),
                                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
                                // If the error is FATAL flag failure has been set to true
                                failure = true;
                                break;
                            }
                        }
                    }
                    /* Validate CDA eHDSI Pivot */
                    if (OpenNCPValidation.isValidationEnable()) {
                        OpenNCPValidation.validateCdaDocument(XMLUtils.toOM(doc.getDocumentElement()).toString(),
                                NcpSide.NCP_A, epsosDoc.getClassCode(), true);
                    }
                }

                // If there is no failure during the process, the CDA document has been attached to the response
                logger.info("Error Registry: Failure '{}'", failure);
                if (!failure) {
                    ByteArrayDataSource dataSource = null;
                    if (doc != null) {
                        dataSource = new ByteArrayDataSource(XMLUtils.toOM(doc.getDocumentElement()).toString().getBytes(),
                                "text/xml;charset=UTF-8");
                    }
                    var dataHandler = new DataHandler(dataSource);
                    var textData = omFactory.createOMText(dataHandler, true);
                    textData.setOptimize(true);
                    document.addChild(textData);

                    logger.debug("Returning document '{}'", documentId);
                    documentResponse.addChild(document);
                    documentReturned = true;
                }
            } catch (Exception e) {
                OpenncpErrorCode code = OpenncpErrorCode.ERROR_GENERIC;

                switch (classCodeValue) {
                    case Constants.EP_CLASSCODE:
                        code = OpenncpErrorCode.ERROR_EP_GENERIC;
                        break;
                    case Constants.PS_CLASSCODE:
                        code = OpenncpErrorCode.ERROR_PS_GENERIC;
                        break;
                    case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                    case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                        code = OpenncpErrorCode.ERROR_ORCD_GENERIC;
                        break;
                }

                failure = true;
                logger.error("Exception: '{}'", e.getMessage(), e);
                addErrorOMMessage(omNamespace, registryErrorList,
                        code,
                        e.getMessage(),
                        RegistryErrorSeverity.ERROR_SEVERITY_ERROR);
            }
        }

        // If the registryErrorList is empty or contains only Warning, the status of the request is SUCCESS
        if (!registryErrorList.getChildElements().hasNext()) {
            logger.info("XCA Retrieve Document - Transformation Status: '{}'\nDefault Case", AdhocQueryResponseStatus.SUCCESS);
            registryResponse.addAttribute(omFactory.createOMAttribute("status", null,
                    AdhocQueryResponseStatus.SUCCESS));
        } else {
            if (checkIfOnlyWarnings(registryErrorList)) {
                logger.info("XCA Retrieve Document - Transformation Status: '{}'\nCheck Warning", AdhocQueryResponseStatus.SUCCESS);
                registryResponse.addAttribute(omFactory.createOMAttribute("status", null,
                        AdhocQueryResponseStatus.SUCCESS));
            } else if (failure) {
                // If there is a failure during the request process, the status is FAILURE
                logger.info("XCA Retrieve Document - Transformation Status: '{}'\nCheck Warning Failure: '{}'", AdhocQueryResponseStatus.FAILURE, failure);
                registryResponse.addAttribute(omFactory.createOMAttribute("status", null,
                        AdhocQueryResponseStatus.FAILURE));
            } else {
                //  Otherwise the status is PARTIAL SUCCESS
                logger.info("XCA Retrieve Document - Transformation Status: '{}'\nOtherwise...", AdhocQueryResponseStatus.PARTIAL_SUCCESS);
                registryResponse.addAttribute(omFactory.createOMAttribute("status", null,
                        AdhocQueryResponseStatus.PARTIAL_SUCCESS));
            }
        }

        logger.info("Preparing Event Log of the Response:");
        try {
            boolean errorsDiscovered = registryErrorList.getChildElements().hasNext();
            if (errorsDiscovered) {
                registryResponse.addChild(registryErrorList);
            }
            omElement.addChild(registryResponse);
            if (documentReturned) {
                omElement.addChild(documentResponse);
            }
            prepareEventLogForRetrieve(eventLog, request, errorsDiscovered, documentReturned, registryErrorList, soapHeaderElement, classCodeValue);
        } catch (Exception ex) {
            logger.error("Prepare Audit log failed. '{}'", ex.getMessage(), ex);
            // Is this fatal?
        }

        //Once the response and the audit message have been prepared, the registryErrorList might be cleaned depending of the errors status.
        if (failure) {
            //Only XCA Error Code defined into the XCA Profile might be attached to the response in case of FAILURE.
            Iterator<OMElement> errors = registryErrorList.getChildElements();
            while (errors.hasNext()) {

                OMElement errorCode = errors.next();
                logger.error("Error: '{}'-'{}'", errorCode.getText(), errorCode.getAttributeValue(QName.valueOf("errorCode")));
                List<String> list = Arrays.asList(OpenncpErrorCode.ERROR_TRANSCODING_ERROR.getCode(),
                        OpenncpErrorCode.WARNING_EP_MISSING_EXPECTED_MAPPING.getCode(),
                        OpenncpErrorCode.WARNING_PS_MISSING_EXPECTED_MAPPING.getCode(),
                        OpenncpErrorCode.WARNING_ED_MISSING_EXPECTED_MAPPING.getCode());
                if(!list.contains(errorCode.getAttributeValue(QName.valueOf("errorCode")))){
                    errors.remove();
                }
            }
        }
    }

    /**
     * This method will check if the Registry error list only contains Warnings.
     *
     * @return boolean value, indicating if the list only contains warnings.
     */
    private boolean checkIfOnlyWarnings(OMElement registryErrorList) {

        var onlyWarnings = true;
        OMElement element;
        Iterator it = registryErrorList.getChildElements();

        while (it.hasNext()) {

            element = (OMElement) it.next();
            if (StringUtils.equals(element.getAttribute(QName.valueOf("severity")).getAttributeValue(),
                    RegistryErrorSeverity.ERROR_SEVERITY_ERROR.getText())) {
                logger.debug("Error has been detected for Element: '{}'", element.getText());
                onlyWarnings = false;
            }
        }
        return onlyWarnings;
    }

    /**
     * Method responsible of the AdhocQueryResponse message if the operation requested is not supported by the server.
     * RegistryError shall contain:
     * errorCode: required.
     * codeContext: required - Supplies additional detail for the errorCode.
     * severity: required - Indicates the severity of the error.
     * Shall be one of:
     * urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error
     * urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning
     * location: optional - Supplies the location of the error module name and line number or stack trace if appropriate.
     *
     * @param request - original AdhocQueryRequest
     * @param e       - Exception thrown by the system
     * @return response - populated with te specific Error Code according the document Class Code.
     */
    private AdhocQueryResponse handleUnsupportedOperationException(AdhocQueryRequest request, UnsupportedOperationException e) {

        var adhocQueryResponse = ofQuery.createAdhocQueryResponse();
        var registryErrorList = ofRs.createRegistryErrorList();

        // Create Registry Object List
        adhocQueryResponse.setRegistryObjectList(ofRim.createRegistryObjectListType());

        List<String> classCodeValues = getDocumentEntryClassCodes(request);

        for (String classCodeValue : classCodeValues) {
            switch (classCodeValue) {
                case Constants.EP_CLASSCODE:
                    addErrorMessage(registryErrorList,
                            OpenncpErrorCode.ERROR_EP_NOT_FOUND,
                            "The XDS repository does not contain any ePrescription related to the current patient",
                            "No ePrescriptions are registered for the given patient.",
                            RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                    break;
                case Constants.PS_CLASSCODE:
                    addErrorMessage(registryErrorList,
                            OpenncpErrorCode.ERROR_PS_NOT_FOUND,
                            "The XDS repository does not contain any Patient Summary related to the current patient",
                            "No patient summary is registered for the given patient.",
                            RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    addErrorMessage(registryErrorList,
                            OpenncpErrorCode.ERROR_ORCD_NOT_FOUND,
                            "The XDS repository does not contain any OrCD of the requested type related to the current patient",
                            "No original clinical document of the requested type is registered for the given patient.",
                            RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                    break;
                default:
                    addErrorMessage(registryErrorList,
                            OpenncpErrorCode.ERROR_DOCUMENT_NOT_FOUND,
                            "The XDS repository does not contain any documents related to the current patient",
                            "No documents are registered for the given patient.",
                            RegistryErrorSeverity.ERROR_SEVERITY_WARNING);
                    break;
            }
        }

        adhocQueryResponse.setRegistryErrorList(registryErrorList);
        // Errors managed are only WARNING so the AdhocQueryResponse is considered as successful.
        adhocQueryResponse.setStatus(AdhocQueryResponseStatus.SUCCESS);

        return adhocQueryResponse;
    }

    /**
     * XCA list operation implementation, returns the list of patient summaries or ePrescriptions, depending on the query.
     */
    @Override
    public AdhocQueryResponse queryDocument(AdhocQueryRequest adhocQueryRequest, SOAPHeader sh, EventLog eventLog)
            throws Exception {

        var adhocQueryResponse = ofQuery.createAdhocQueryResponse();
        try {
            adhocQueryResponseBuilder(adhocQueryRequest, adhocQueryResponse, sh, eventLog);
        } catch (UnsupportedOperationException uoe) {
            adhocQueryResponse = handleUnsupportedOperationException(adhocQueryRequest, uoe);
        }
        return adhocQueryResponse;
    }

    /**
     * XCA retrieve operation implementation, returns the particular document requested by the caller.
     * The response is placed in the OMElement
     */
    @Override
    public void retrieveDocument(RetrieveDocumentSetRequestType request, SOAPHeader sh, EventLog eventLog,
                                 OMElement response) throws Exception {

        retrieveDocumentSetBuilder(request, sh, eventLog, response);
    }

    /**
     * This auxiliary service returns the service name, based on a provided class code.
     *
     * @deprecated
     */
    @Deprecated(since = "5.2.0", forRemoval = true)
    private String getDocumentName(final String classCodeValue) {

        if (classCodeValue.contains(Constants.PS_CLASSCODE)) {
            return Constants.PS_TITLE;
        } else if (classCodeValue.contains(Constants.ED_CLASSCODE)) {
            return Constants.ED_TITLE;
        } else if (classCodeValue.contains(Constants.MRO_CLASSCODE)) {
            return Constants.MRO_TITLE;
        } else {
            return null;
        }
    }
}
