package tr.com.srdc.epsos.ws.server.xca.impl;

import epsos.ccd.gnomon.auditmanager.*;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.posam.tm.exception.TMError;
import epsos.ccd.posam.tm.response.TMResponseStructure;
import epsos.ccd.posam.tm.service.ITransformationService;
import epsos.ccd.posam.tsam.exception.ITMTSAMEror;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.xca.DocumentSearchInterface;
import eu.epsos.protocolterminators.ws.server.xca.XCAServiceInterface;
import eu.epsos.util.EvidenceUtils;
import eu.epsos.util.IheConstants;
import eu.epsos.util.xca.XCAConstants;
import eu.epsos.validation.datamodel.cda.CdaModel;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.services.CdaValidationService;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.AdhocQueryResponseStatus;
import fi.kela.se.epsos.data.model.*;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.*;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.data.model.xds.DocumentType;
import tr.com.srdc.epsos.securityman.SAML2Validator;
import tr.com.srdc.epsos.securityman.exceptions.AssertionValidationException;
import tr.com.srdc.epsos.securityman.exceptions.InsufficientRightsException;
import tr.com.srdc.epsos.securityman.helper.Helper;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.DateUtil;
import tr.com.srdc.epsos.util.XMLUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class XCAServiceImpl implements XCAServiceInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(XCAServiceImpl.class);
    private ITransformationService transformationService;
    private OMFactory factory;
    private oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory ofQuery;
    private oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory ofRim;
    private oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory ofRs;
    private DocumentSearchInterface documentSearchService;

    public XCAServiceImpl() {

        ServiceLoader<DocumentSearchInterface> serviceLoader = ServiceLoader.load(DocumentSearchInterface.class);
        try {
            LOGGER.info("Loading National implementation of DocumentSearchInterface...");
            documentSearchService = serviceLoader.iterator().next();
            LOGGER.info("Successfully loaded documentSearchService");
        } catch (Exception e) {
            LOGGER.error("Failed to load implementation of DocumentSearchInterface: " + e.getMessage(), e);
            throw e;
        }

        ofQuery = new oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory();
        ofRim = new oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory();
        ofRs = new oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory();

        factory = OMAbstractFactory.getOMFactory();

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
        transformationService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());
    }

    private void prepareEventLogForQuery(EventLog eventLog, AdhocQueryRequest request, AdhocQueryResponse response, Element sh, String classCode) {

        switch (classCode) {
            case Constants.EP_CLASSCODE:
                eventLog.setEventType(EventType.epsosOrderServiceList);
                eventLog.setEI_TransactionName(TransactionName.epsosOrderServiceList);
                break;
            case Constants.PS_CLASSCODE:
                eventLog.setEventType(EventType.epsosPatientServiceList);
                eventLog.setEI_TransactionName(TransactionName.epsosPatientServiceList);
                break;
            case Constants.MRO_CLASSCODE:
                eventLog.setEventType(EventType.epsosMroList);
                eventLog.setEI_TransactionName(TransactionName.epsosMroServiceList);
                break;
        }
        eventLog.setEI_EventActionCode(EventActionCode.READ);
        try {
            eventLog.setEI_EventDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("DatatypeConfigurationException: {}", e.getMessage(), e);
        }
        eventLog.setPS_PatricipantObjectID(getDocumentEntryPatientId(request));

        if (response.getRegistryObjectList() != null) {
            for (int i = 0; i < response.getRegistryObjectList().getIdentifiable().size(); i++) {
                if (!(response.getRegistryObjectList().getIdentifiable().get(i).getValue() instanceof ExtrinsicObjectType)) {
                    continue;
                }
                ExtrinsicObjectType eot = (ExtrinsicObjectType) response.getRegistryObjectList().getIdentifiable().get(i).getValue();
                String documentId = "";
                for (ExternalIdentifierType eit : eot.getExternalIdentifier()) {
                    if (eit.getIdentificationScheme().equals("urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab")) {
                        documentId = eit.getValue();
                    }
                }
                // PT-237 fix. This method should be somewhere centrally
                if (!StringUtils.startsWith(documentId, "urn:uuid:")) {
                    documentId = "urn:uuid:" + documentId;
                }
                eventLog.setET_ObjectID(documentId);
                break;
            }
        }

        if (response.getRegistryObjectList() == null) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
            eventLog.setET_ObjectID(Constants.UUID_PREFIX);
        } else if (response.getRegistryErrorList() == null) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        } else {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.TEMPORAL_FAILURE);
            eventLog.setET_ObjectID(Constants.UUID_PREFIX);
        }

        eventLog.setHR_UserID(Constants.HR_ID_PREFIX + "<" + Helper.getUserID(sh) + "@" + Helper.getOrganization(sh) + ">");
        eventLog.setHR_AlternativeUserID(Helper.getAlternateUserID(sh));
        eventLog.setHR_RoleID(Helper.getRoleID(sh));
        eventLog.setSP_UserID(HTTPUtil.getSubjectDN(true));
        eventLog.setPT_PatricipantObjectID(getDocumentEntryPatientId(request));
        eventLog.setAS_AuditSourceId(Constants.COUNTRY_PRINCIPAL_SUBDIVISION);

        if (response.getRegistryErrorList() != null) {
            RegistryError re = response.getRegistryErrorList().getRegistryError().get(0);
            eventLog.setEM_PatricipantObjectID(re.getErrorCode());
            eventLog.setEM_PatricipantObjectDetail(re.getCodeContext().getBytes());
        }
    }

    private void prepareEventLogForRetrieve(EventLog eventLog, RetrieveDocumentSetRequestType request, boolean errorsDiscovered,
                                            boolean documentReturned, OMElement registryErrorList, Element sh, String classCode) {

        LOGGER.info("method prepareEventLogForRetrieve({})", classCode);
        if (classCode == null || classCode.equals(Constants.EP_CLASSCODE)) {
            // In case the document is not found, audit log cannot be properly filled, as we don't know the event type
            // Log this under Order Service
            eventLog.setEventType(EventType.epsosOrderServiceRetrieve);
            eventLog.setEI_TransactionName(TransactionName.epsosOrderServiceRetrieve);
        } else if (classCode.equals(Constants.PS_CLASSCODE)) {
            eventLog.setEventType(EventType.epsosPatientServiceRetrieve);
            eventLog.setEI_TransactionName(TransactionName.epsosPatientServiceRetrieve);
        } else if (classCode.equals(Constants.MRO_CLASSCODE)) {
            eventLog.setEventType(EventType.epsosMroRetrieve);
            eventLog.setEI_TransactionName(TransactionName.epsosMroServiceRetrieve);
        }
        eventLog.setEI_EventActionCode(EventActionCode.READ);
        try {
            eventLog.setEI_EventDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("DatatypeConfigurationException: {}", e.getMessage(), e);
        }

        eventLog.setET_ObjectID(Constants.UUID_PREFIX + request.getDocumentRequest().get(0).getDocumentUniqueId());

        if (!documentReturned) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
        } else if (!errorsDiscovered) {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        } else {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.TEMPORAL_FAILURE);
        }

        eventLog.setHR_UserID(Constants.HR_ID_PREFIX + "<" + Helper.getUserID(sh) + "@" + Helper.getOrganization(sh) + ">");
        eventLog.setHR_AlternativeUserID(Helper.getAlternateUserID(sh));
        eventLog.setHR_RoleID(Helper.getRoleID(sh));
        eventLog.setSP_UserID(HTTPUtil.getSubjectDN(true));
        eventLog.setPT_PatricipantObjectID(Helper.getDocumentEntryPatientIdFromTRCAssertion(sh));
        eventLog.setAS_AuditSourceId(Constants.COUNTRY_PRINCIPAL_SUBDIVISION);

        if (errorsDiscovered) {
            Iterator<OMElement> re = registryErrorList.getChildElements();
            //Include only the first error in the audit log.
            if (re.hasNext()) {
                OMElement error = re.next();
                try {
                    LOGGER.debug("Error to be included in audit: '{}'", XMLUtil.prettyPrint(XMLUtils.toDOM(error)));
                } catch (Exception e) {
                    LOGGER.debug("Exception: '{}'", e.getMessage(), e);
                }
                eventLog.setEM_PatricipantObjectID(error.getAttributeValue(new QName("", "errorCode")));
                eventLog.setEM_PatricipantObjectDetail(error.getAttributeValue(new QName("", "codeContext")).getBytes());
            }
        }
    }

    private String getDocumentEntryClassCode(AdhocQueryRequest request) {

        for (SlotType1 sl : request.getAdhocQuery().getSlot()) {
            if (sl.getName().equals("$XDSDocumentEntryClassCode")) {
                String classCode = sl.getValueList().getValue().get(0);
                String pattern = "\\(?\\)?\\'?";
                classCode = classCode.replaceAll(pattern, "");
                classCode = classCode.substring(0, classCode.indexOf("^^"));
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

    private SlotType1 makeSlot(String name, String value) {

        SlotType1 sl = ofRim.createSlotType1();
        sl.setName(name);
        sl.setValueList(ofRim.createValueListType());
        sl.getValueList().getValue().add(value);
        return sl;
    }

    private ClassificationType makeClassification(String classificationScheme, String classifiedObject, String nodeRepresentation, String value, String name) {

        String uuid = "urn:uuid:" + UUID.randomUUID().toString();
        ClassificationType cl = ofRim.createClassificationType();
        cl.setId(uuid);
        cl.setNodeRepresentation(nodeRepresentation);
        cl.setClassificationScheme(classificationScheme);
        cl.setClassifiedObject(classifiedObject);
        cl.getSlot().add(makeSlot("codingScheme", value));

        cl.setName(ofRim.createInternationalStringType());
        cl.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        cl.getName().getLocalizedString().get(0).setValue(name);
        return cl;
    }

    private ClassificationType makeClassification(String classificationScheme, String classifiedObject, String nodeRepresentation) {

        String uuid = "urn:uuid:" + UUID.randomUUID().toString();
        ClassificationType cl = ofRim.createClassificationType();
        cl.setId(uuid);
        cl.setNodeRepresentation(nodeRepresentation);
        cl.setClassificationScheme(classificationScheme);
        cl.setClassifiedObject(classifiedObject);
        return cl;
    }

    private ExternalIdentifierType makeExternalIdentifier(String identificationScheme, String registryObject, String value, String name) {

        String uuid = "urn:uuid:" + UUID.randomUUID().toString();
        ExternalIdentifierType ex = ofRim.createExternalIdentifierType();
        ex.setId(uuid);
        ex.setIdentificationScheme(identificationScheme);
        ex.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
        ex.setRegistryObject(registryObject);
        ex.setValue(value);

        ex.setName(ofRim.createInternationalStringType());
        ex.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        ex.getName().getLocalizedString().get(0).setValue(name);
        return ex;
    }

    private String prepareExtrinsicObjectEpsosDoc(DocumentType docType, Date effectiveTime, String repositoryId,
                                                  AdhocQueryRequest request, ExtrinsicObjectType eot, boolean isPDF,
                                                  String documentId) {

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
                LOGGER.error("Unsupported document for query in OpenNCP. Requested document type: {}", docType.name());
                return "";
        }

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID().toString();
        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType("urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1");

        // Status
        eot.setMimeType("text/xml");

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
        eot.getSlot().add(makeSlot("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", effectiveTime)));

        // Source Patient Id
        eot.getSlot().add(makeSlot("sourcePatientId", getDocumentEntryPatientId(request)));

        // LanguageCode (optional)
        eot.getSlot().add(makeSlot("languageCode", Constants.LANGUAGE_CODE));

        // repositoryUniqueId (optional)
        eot.getSlot().add(makeSlot("repositoryUniqueId", repositoryId));

        eot.getClassification().add(makeClassification("urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a",
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(makeClassification("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid, classCode, "2.16.840.1.113883.6.1", title));
        // Confidentiality Code
        eot.getClassification().add(makeClassification("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid, "N", "2.16.840.1.113883.5.25", "Normal"));
        // FormatCode
        if (isPDF) {
            eot.getClassification().add(makeClassification("urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
                    uuid, "urn:ihe:iti:xds-sd:pdf:2008", "IHE PCC", "PDF/A coded document"));
        } else {
            eot.getClassification().add(makeClassification("urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
                    uuid, nodeRepresentation, "epSOS formatCodes", displayName));
        }

        /*
         * Healthcare facility code
         * TODO: Get healthcare facility info from national implementation
         */
        eot.getClassification().add(makeClassification("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid, Constants.COUNTRY_CODE, "1.0.3166.1", Constants.COUNTRY_NAME));

        // Practice Setting code
        eot.getClassification().add(makeClassification("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid, "Not Used", "epSOS Practice Setting Codes-Not Used", "Not Used"));

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid, getDocumentEntryPatientId(request), "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab",
                uuid, documentId, "XDSDocumentEntry.uniqueId"));

        return uuid;
    }

    private String prepareExtrinsicObjectEP(AdhocQueryRequest request, ExtrinsicObjectType eot, EPDocumentMetaData document) {

        String name = "ePrescription";
        String uuid = "urn:uuid:" + UUID.randomUUID().toString();
        boolean isPDF = document.getFormat() == EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF;

        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType("urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1");

        // Status
        eot.setMimeType("text/xml");

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(name);

        // Description (optional)
        eot.setDescription(ofRim.createInternationalStringType());
        eot.getDescription().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getDescription().getLocalizedString().get(0).setValue(document.getTitle());

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1");

        // Creation Date (optional)
        eot.getSlot().add(makeSlot("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", document.getEffectiveTime())));

        // Source Patient Id
        eot.getSlot().add(makeSlot("sourcePatientId", document.getPatientId() + "^^^&" + Constants.HOME_COMM_ID + "&ISO"));

        // LanguageCode (optional)
        eot.getSlot().add(makeSlot("languageCode", Constants.LANGUAGE_CODE));

        // repositoryUniqueId (optional)
        eot.getSlot().add(makeSlot("repositoryUniqueId", document.getRepositoryId()));

        eot.getClassification().add(
                makeClassification(
                        "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a", uuid,
                        Constants.EP_CLASSCODE, "2.16.840.1.113883.6.1", name));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(makeClassification(
                "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid,
                Constants.EP_CLASSCODE,
                "2.16.840.1.113883.6.1",
                name));
        // Confidentiality Code
        eot.getClassification().add(makeClassification(
                "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid,
                "N",
                "2.16.840.1.113883.5.25",
                "Normal"));
        // FormatCode
        if (isPDF) {
            eot.getClassification().add(makeClassification(
                    "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
                    uuid,
                    "urn:ihe:iti:xds-sd:pdf:2008",
                    "IHE PCC",
                    "PDF/A coded document"));
        } else {
            eot.getClassification().add(makeClassification(
                    "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
                    uuid,
                    "urn:epSOS:ep:pre:2010",
                    "epSOS formatCodes",
                    "epSOS coded ePrescription"));
        }
        // Healthcare facility code
        // TODO: Get healthcare facility info from national implementaition

        eot.getClassification().add(makeClassification(
                "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid,
                Constants.COUNTRY_CODE,
                "1.0.3166.1",
                Constants.COUNTRY_NAME));
        // Practice Setting code
        eot.getClassification().add(makeClassification(
                "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid,
                "Not Used",
                "epSOS Practice Setting Codes-Not Used",
                "Not Used"));

        // Author Person
        ClassificationType authorClassification = makeClassification(
                "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d",
                uuid,
                "");
        authorClassification.getSlot().add(makeSlot("authorPerson", document.getAuthor()));
        eot.getClassification().add(authorClassification);

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier(
                "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid,
                document.getPatientId() + "^^^&" + Constants.HOME_COMM_ID + "&ISO",
                "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier(
                "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab",
                uuid,
                document.getId(),
                "XDSDocumentEntry.uniqueId"));

        return uuid;
    }

    private AssociationType1 makeAssociation(String source, String target) {

        String uuid = "urn:uuid:" + UUID.randomUUID().toString();
        AssociationType1 association = ofRim.createAssociationType1();
        association.setId(uuid);
        association.setAssociationType("urn:ihe:iti:2007:AssociationType:XFRM");
        association.setSourceObject(source);
        association.setTargetObject(target);
        /*
         * Gazelle does not like this information when validating. Uncomment if
         * really needed.
         */
        //        association.getClassification().add(makeClassification(
        //                "urn:uuid:abd807a3-4432-4053-87b4-fd82c643d1f3",
        //                uuid,
        //                "epSOS pivot",
        //                "epSOS translation types",
        //                "Translation into epSOS pivot format"));
        return association;
    }

    private String getLocation() {

        //String location = ConfigurationManagerFactory.getConfigurationManager().getEndpointUrl(Constants.COUNTRY_CODE.toLowerCase(Locale.ENGLISH),
        //              RegisteredService.PATIENT_SERVICE);
        // EHNCP-1131
        return "urn:oid:" + Constants.HOME_COMM_ID;
    }

    private RegistryError createErrorMessage(String errorCode, String codeContext, String value, boolean isWarning) {

        RegistryError re = ofRs.createRegistryError();
        re.setErrorCode(errorCode);
        re.setLocation(getLocation());
        re.setSeverity("urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:" + (isWarning ? "Warning" : "Error"));
        re.setCodeContext(codeContext);
        re.setValue(value);
        return re;
    }

    private OMElement createErrorOMMessage(OMNamespace ons, String errorCode, String codeContext, String value, boolean isWarning) {

        OMElement re = factory.createOMElement("RegistryError", ons);
        re.addAttribute(factory.createOMAttribute("codeContext", null, codeContext));
        re.addAttribute(factory.createOMAttribute("errorCode", null, errorCode));
        String aux = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:" + (isWarning ? "Warning" : "Error");
        re.addAttribute(factory.createOMAttribute("severity", null, aux));
        // EHNCP-1131
        re.addAttribute(factory.createOMAttribute("location", null, getLocation()));
        re.setText(value);

        return re;
    }

    /**
     * Main part of the XCA query operation implementation, fills the AdhocQueryResponse with details
     */
    private void AdhocQueryResponseBuilder(AdhocQueryRequest request, AdhocQueryResponse response, SOAPHeader sh,
                                           EventLog eventLog) throws Exception {

        String sigCountryCode = null;
        Element shElement = null;
        // What's being requested: eP or PS?
        String classCodeValue = getDocumentEntryClassCode(request);
        RegistryErrorList rel = ofRs.createRegistryErrorList();
        // Create Registry Object List
        response.setRegistryObjectList(ofRim.createRegistryObjectListType());

        try {
            shElement = XMLUtils.toDOM(sh);
            documentSearchService.setSOAPHeader(shElement);
            sigCountryCode = SAML2Validator.validateXCAHeader(shElement, classCodeValue);
        } catch (InsufficientRightsException e) {
            LOGGER.debug(e.getMessage(), e);
            rel.getRegistryError().add(createErrorMessage(e.getCode(), e.getMessage(), "", false));
        } catch (AssertionValidationException e) {
            LOGGER.debug(e.getMessage(), e);
            rel.getRegistryError().add(createErrorMessage(e.getCode(), e.getMessage(), "", false));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rel.getRegistryError().add(createErrorMessage("", e.getMessage(), "", false));
            throw e;
        }

        String fullPatientId = trimDocumentEntryPatientId(Helper.getDocumentEntryPatientIdFromTRCAssertion(shElement));
        if (!getDocumentEntryPatientId(request).contains(fullPatientId)) {
            // Patient ID in TRC assertion does not match the one given in the request. Return "No documents found".
            if (StringUtils.contains(classCodeValue, Constants.EP_CLASSCODE)) {
                rel.getRegistryError().add(createErrorMessage("1101", "No ePrescriptions are registered for the given patient.", "", true));
            } else if (StringUtils.contains(classCodeValue, Constants.PS_CLASSCODE)) {
                rel.getRegistryError().add(createErrorMessage("1102", "No patient summary is registered for the given patient.", "", true));
            } else {
                rel.getRegistryError().add(createErrorMessage("1100", "No documents are registered for the given patient.", "", true));
            }
        }
        String patientId = trimDocumentEntryPatientId(fullPatientId);
        String countryCode = "";
        String DN = eventLog.getSC_UserID();
        int cIndex = DN.indexOf("C=");

        if (cIndex > 0) {
            countryCode = DN.substring(cIndex + 2, cIndex + 4);
        } // Mustafa: This part is added for handling consents when the call is not https
        // In this case, we check the country code of the signature certificate that
        // ships within the HCP assertion
        // TODO: Might be necessary to remove later, although it does no harm in reality!
        else {
            LOGGER.info("Could not get client country code from the service consumer certificate. " +
                    "The reason can be that the call was not via HTTPS. " +
                    "Will check the country code from the signature certificate now.");
            if (sigCountryCode != null) {
                LOGGER.info("Found the client country code via the signature certificate.");
                countryCode = sigCountryCode;
            }
        }
        LOGGER.info("The client country code to be used by the PDP: '{}'", countryCode);

        // Then, it is the Policy Decision Point (PDP) that decides according to the consent of the patient
        if (!SAML2Validator.isConsentGiven(patientId, countryCode)) {
            InsufficientRightsException e = new InsufficientRightsException(4701);
            rel.getRegistryError().add(createErrorMessage(e.getCode(), e.getMessage(), "", false));
        }

        if (classCodeValue == null) {
            rel.getRegistryError().add(createErrorMessage("4202", "Class code missing in XCA query request.", "", false));
        }

        // Evidence for call to NI for XCA List
        /* Joao: we MUST generate NRO when NCPA sends to NI. This was throwing errors because we were not passing a XML document.
        We're passing data like:
        "SearchCriteria: {patientId = 12445ASD}"
        So we provided a XML representation of such data */
        try {
            EvidenceUtils.createEvidenceREMNRO(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                    tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PATH,
                    tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PASSWORD,
                    tr.com.srdc.epsos.util.Constants.SP_PRIVATEKEY_ALIAS,
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                    tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                    IHEEventType.epsosPatientServiceList.getCode(),
                    new DateTime(),
                    EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                    "NI_XCA_LIST_REQ",
                    Helper.getTRCAssertion(shElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        if (!rel.getRegistryError().isEmpty()) {
            response.setRegistryErrorList(rel);
            response.setStatus(AdhocQueryResponseStatus.FAILURE);

        } else {

            if (StringUtils.equals(classCodeValue, Constants.EP_CLASSCODE) || StringUtils.equals(classCodeValue,
                    Constants.PS_CLASSCODE) || StringUtils.equals(classCodeValue, Constants.MRO_CLASSCODE)) {

                LOGGER.info("XCA Query Request for '{}' is valid.", classCodeValue);
                // Document search for ePrescription service.
                if (StringUtils.contains(classCodeValue, Constants.EP_CLASSCODE)) {

                    List<DocumentAssociation<EPDocumentMetaData>> prescriptions = documentSearchService.getEPDocumentList(
                            DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                    if (prescriptions == null) {

                        rel.getRegistryError().add(createErrorMessage("4103", "ePrescription registry could not be accessed.", "", true));
                        response.setRegistryErrorList(rel);
                        response.setStatus(AdhocQueryResponseStatus.FAILURE);
                    } else if (prescriptions.isEmpty()) {

                        rel.getRegistryError().add(createErrorMessage("1101", "No ePrescriptions are registered for the given patient.", "", true));
                        response.setRegistryErrorList(rel);
                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);
                    } else {

                        // Multiple prescriptions mean multiple PDF and XML files, multiple ExtrinsicObjects and associations
                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);
                        for (DocumentAssociation<EPDocumentMetaData> prescription : prescriptions) {

                            LOGGER.debug("Prescription Repository ID: '{}'", prescription.getXMLDocumentMetaData().getRepositoryId());
                            String xmlUUID;
                            ExtrinsicObjectType eotXML = ofRim.createExtrinsicObjectType();
                            xmlUUID = prepareExtrinsicObjectEP(request, eotXML, prescription.getXMLDocumentMetaData());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));

                            String pdfUUID;
                            ExtrinsicObjectType eotPDF = ofRim.createExtrinsicObjectType();
                            pdfUUID = prepareExtrinsicObjectEP(request, eotPDF, prescription.getPDFDocumentMetaData());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));

                            if (StringUtils.isNotBlank(xmlUUID) && StringUtils.isNotBlank(pdfUUID)) {

                                response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                            }
                        }
                    }
                } else if (StringUtils.contains(classCodeValue, Constants.PS_CLASSCODE)) {

                    // Document search for Patient service.
                    DocumentAssociation<PSDocumentMetaData> psDoc = documentSearchService.getPSDocumentList(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                    if (psDoc == null || (psDoc.getPDFDocumentMetaData() == null && psDoc.getXMLDocumentMetaData() == null)) {

                        rel.getRegistryError().add(createErrorMessage("1102", "No patient summary is registered for the given patient.", "", true));
                        response.setRegistryErrorList(rel);
                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);
                    } else {

                        PSDocumentMetaData docPdf = psDoc.getPDFDocumentMetaData();
                        PSDocumentMetaData docXml = psDoc.getXMLDocumentMetaData();
                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);

                        String xmlUUID = "";
                        if (docXml != null) {
                            ExtrinsicObjectType eotXML = ofRim.createExtrinsicObjectType();
                            xmlUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.PATIENT_SUMMARY, psDoc.getXMLDocumentMetaData().getEffectiveTime(), psDoc.getXMLDocumentMetaData().getRepositoryId(), request, eotXML, false, docXml.getId());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));
                        }
                        String pdfUUID = "";
                        if (docPdf != null) {
                            ExtrinsicObjectType eotPDF = ofRim.createExtrinsicObjectType();
                            pdfUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.PATIENT_SUMMARY, psDoc.getPDFDocumentMetaData().getEffectiveTime(), psDoc.getPDFDocumentMetaData().getRepositoryId(), request, eotPDF, true, docPdf.getId());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));
                        }
                        if (!xmlUUID.equals("") && !pdfUUID.equals("")) {
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                        }
                    }
                } else if (StringUtils.contains(classCodeValue, Constants.MRO_CLASSCODE)) {

                    // Document search for MRO service.
                    DocumentAssociation<MroDocumentMetaData> mro = documentSearchService.getMroDocumentList(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId));

                    if (mro == null || (mro.getPDFDocumentMetaData() == null && mro.getXMLDocumentMetaData() == null)) {

                        rel.getRegistryError().add(createErrorMessage("1100", "No MRO summary is registered for the given patient.", "", true));
                        response.setRegistryErrorList(rel);
                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);
                    } else {

                        MroDocumentMetaData docPdf = mro.getPDFDocumentMetaData();
                        MroDocumentMetaData docXml = mro.getXMLDocumentMetaData();

                        response.setStatus(AdhocQueryResponseStatus.SUCCESS);

                        String xmlUUID = "";
                        if (docXml != null) {
                            ExtrinsicObjectType eotXML = ofRim.createExtrinsicObjectType();
                            xmlUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.MRO, mro.getXMLDocumentMetaData().getEffectiveTime(), mro.getXMLDocumentMetaData().getRepositoryId(), request, eotXML, false, docXml.getId());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotXML));
                        }
                        String pdfUUID = "";
                        if (docPdf != null) {
                            ExtrinsicObjectType eotPDF = ofRim.createExtrinsicObjectType();
                            pdfUUID = prepareExtrinsicObjectEpsosDoc(DocumentType.MRO, mro.getPDFDocumentMetaData().getEffectiveTime(), mro.getPDFDocumentMetaData().getRepositoryId(), request, eotPDF, true, docPdf.getId());
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createExtrinsicObject(eotPDF));
                        }
                        if (!xmlUUID.equals("") && !pdfUUID.equals("")) {
                            response.getRegistryObjectList().getIdentifiable().add(ofRim.createAssociation(makeAssociation(pdfUUID, xmlUUID)));
                        }
                    }
                }
//                // Evidence for response from NI for XCA List in case of success
                /* Joao: This should be NRR of NCPA receiving from NI.
                    This was throwing errors because we were not passing a XML document.
                    We're passing data like:
                    "SearchCriteria: {patientId = 12445ASD}"
                    So we provided a XML representation of such data. Still, evidence is generated based on request data, not response.
                    This NRR is optional as per the CP. So we leave this commented*/
//                try {
//                    EvidenceUtils.createEvidenceREMNRR(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                            IHEEventType.epsosPatientServiceList.getCode(),
//                            new DateTime(),
//                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                            "NI_XCA_LIST_RES",
//                            Helper.getTRCAssertion(shElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
//                } catch (Exception e) {
//                    LOGGER.error(ExceptionUtils.getStackTrace(e));
//                }
            } else {
                // Evidence for response from NI for XCA List in case of failure
                /* Joao: This should be NRR of NCPA receiving from NI.
                    This was throwing errors because we were not passing a XML document.
                    We're passing data like:
                    "SearchCriteria: {patientId = 12445ASD}"
                    So we provided a XML representation of such data. Still, evidence is generated based on request data, not response.
                    This NRR is optional as per the CP. So we leave this commented
                */
//                try {
//                    EvidenceUtils.createEvidenceREMNRR(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                            IHEEventType.epsosPatientServiceList.getCode(),
//                            new DateTime(),
//                            EventOutcomeIndicator.TEMPORAL_FAILURE.getCode().toString(),
//                            "NI_XCA_LIST_RES_FAIL",
//                            Helper.getTRCAssertion(shElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
//                } catch (Exception e) {
//                    LOGGER.error(ExceptionUtils.getStackTrace(e));
//                }

                rel.getRegistryError().add(createErrorMessage("4202", "Class code not supported for XCA query(" + classCodeValue + ").", "", false));
                response.setRegistryErrorList(rel);
                response.setStatus(AdhocQueryResponseStatus.FAILURE);
            }
        }

        try {
            prepareEventLogForQuery(eventLog, request, response, shElement, classCodeValue);
        } catch (Exception e) {
            LOGGER.error("Prepare Audit log failed: '{}'", e.getMessage(), e);
            // Is this fatal?
        }
    }

    /**
     * @param doc
     * @param registryErrorList
     * @param registryResponseElement
     * @param isTranscode
     * @param eventLog
     * @return
     * @throws Exception
     */
    private Document transformDocument(Document doc, OMElement registryErrorList, OMElement registryResponseElement,
                                       boolean isTranscode, EventLog eventLog) {

        LOGGER.debug("Transforming document, isTranscode: '{}'", isTranscode);

        Document returnDoc;
        try {
            TMResponseStructure tmResponse;
            String operationType;
            if (isTranscode) {
                operationType = "toEpSOSPivot";
                LOGGER.debug("Transforming document to epSOS pivot...");
                tmResponse = transformationService.toEpSOSPivot(doc);
            } else {
                operationType = "translate";
                LOGGER.debug("Translating document to '{}'", Constants.LANGUAGE_CODE);
                tmResponse = transformationService.translate(doc, Constants.LANGUAGE_CODE);
            }

            OMNamespace ns = registryResponseElement.getNamespace();
            OMNamespace ons = factory.createOMNamespace(ns.getNamespaceURI(), "a");

            for (int i = 0; i < tmResponse.getErrors().size(); i++) {
                ITMTSAMEror error = tmResponse.getErrors().get(i);

                registryErrorList.addChild(createErrorOMMessage(
                        ons,
                        error.getCode(),
                        error.getDescription(),
                        "ECDATransformationHandler.Error." + operationType + "(" + error.getCode() + " / " + error.getDescription() + ")",
                        false));
            }

            for (int i = 0; i < tmResponse.getWarnings().size(); i++) {
                ITMTSAMEror error = tmResponse.getWarnings().get(i);

                registryErrorList.addChild(createErrorOMMessage(
                        ons,
                        error.getCode(),
                        error.getDescription(),
                        "ECDATransformationHandler.Error." + operationType + "(" + error.getCode() + " / " + error.getDescription() + ")",
                        true));
            }

            returnDoc = tmResponse.getResponseCDA();
            if (registryErrorList.getChildElements().hasNext()) {
                registryResponseElement.addChild(registryErrorList);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return returnDoc;
    }

    private void RetrieveDocumentSetBuilder(RetrieveDocumentSetRequestType request, SOAPHeader soapHeader,
                                            EventLog eventLog, OMElement omElement) throws Exception {

        OMNamespace ns = factory.createOMNamespace("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "");
        OMElement registryResponse = factory.createOMElement("RegistryResponse", ns);
        OMElement registryErrorList = factory.createOMElement("RegistryErrorList", ns);
        OMNamespace ns2 = omElement.getNamespace();
        OMElement documentResponse = factory.createOMElement("DocumentResponse", ns2);

        boolean documentReturned = false;
        boolean failure = false;

        Element soapHeaderElement;
        String classCodeValue = null;

        // Start processing within a labeled block, break on certain errors
        processLabel:
        {
            try {
                soapHeaderElement = XMLUtils.toDOM(soapHeader);
            } catch (Exception e) {
                LOGGER.error(null, e);
                throw e;
            }

            documentSearchService.setSOAPHeader(soapHeaderElement);

            String documentId = request.getDocumentRequest().get(0).getDocumentUniqueId();
            String patientId = trimDocumentEntryPatientId(Helper.getDocumentEntryPatientIdFromTRCAssertion(soapHeaderElement));
            String repositoryId = getRepositoryUniqueId(request);

            //try getting country code from the certificate
            String countryCode = null;
            String DN = eventLog.getSC_UserID();
            int cIndex = DN.indexOf("C=");

            if (cIndex > 0) {
                countryCode = DN.substring(cIndex + 2, cIndex + 4);
            }
            // Mustafa: This part is added for handling consents when the call is not https
            // In this case, we check the country code of the signature certificate that
            // ships within the HCP assertion
            // TODO: Might be necessary to remove later, although it does no harm in reality!
            if (countryCode == null) {
                LOGGER.info("Could not get client country code from the service consumer certificate. " +
                        "The reason can be that the call was not via HTTPS. " +
                        "Will check the country code from the signature certificate now.");
                countryCode = SAML2Validator.getCountryCodeFromHCPAssertion(soapHeaderElement);
                if (countryCode != null) {
                    LOGGER.info("Found the client country code via the signature certificate.");
                } else {
                    InsufficientRightsException e = new InsufficientRightsException();
                    registryErrorList.addChild(createErrorOMMessage(ns, e.getCode(), e.getMessage(), "", false));
                    break processLabel;
                }
            }

            LOGGER.info("The client country code to be used by the PDP '{}' ", countryCode);

            // Then, it is the Policy Decision Point (PDP) that decides according to the consent of the patient
            if (!SAML2Validator.isConsentGiven(patientId, countryCode)) {
                InsufficientRightsException e = new InsufficientRightsException(4701);
                registryErrorList.addChild(createErrorOMMessage(ns, e.getCode(), e.getMessage(), "", false));
                break processLabel;
            }

            // Evidence for call to NI for XCA Retrieve
            /* Joao: we MUST generate NRO when NCPA sends to NI.This was throwing errors because we were not passing a XML document.
                We're passing data like:
                "SearchCriteria: {patientId = 12445ASD}"
                So we provided a XML representation of such data */
            try {
                EvidenceUtils.createEvidenceREMNRO(DocumentFactory.createSearchCriteria().add(Criteria.PatientId, patientId).asXml(),
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                        tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PATH,
                        tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PASSWORD,
                        tr.com.srdc.epsos.util.Constants.SP_PRIVATEKEY_ALIAS,
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                        tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                        IHEEventType.epsosPatientServiceRetrieve.getCode(),
                        new DateTime(),
                        EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(), "NI_XCA_RETRIEVE_REQ",
                        Helper.getTRCAssertion(soapHeaderElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
            } catch (Exception e) {
                LOGGER.error("createEvidenceREMNRO: " + ExceptionUtils.getStackTrace(e));
            }

            //TODO: EHNCP-1271 - Shall we indicate a specific ERROR Code???
            EPSOSDocument epsosDoc;
            try {
                epsosDoc = documentSearchService.getDocument(DocumentFactory.createSearchCriteria()
                        .add(Criteria.DocumentId, documentId)
                        .add(Criteria.PatientId, patientId)
                        .add(Criteria.RepositoryId, repositoryId));
            } catch (NIException e) {
                LOGGER.error("NIException: '{}'", e.getMessage(), e);
                registryErrorList.addChild(createErrorOMMessage(ns, e.getCode(), e.getMessage(), "", false));
                break processLabel;
            }

            if (epsosDoc == null) {
//                // Evidence for response from NI in case of failure
                /* Joao: This should be NRR of NCPA receiving from NI.
                    This was throwing errors because we were not passing a XML document.
                    We're passing data like:
                    "SearchCriteria: {patientId = 12445ASD}"
                    So we provided a XML representation of such data. Still, evidence is generated based on request data, not response.
                    This NRR is optional as per the CP. So we leave this commented */
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
//                    LOGGER.error(ExceptionUtils.getStackTrace(e));
//                }
                registryErrorList.addChild(createErrorOMMessage(ns, "XDSMissingDocument", "Requested document not found.", "", false));
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
//                LOGGER.error(ExceptionUtils.getStackTrace(e));
//            }

            classCodeValue = epsosDoc.getClassCode();

            try {
                SAML2Validator.validateXCAHeader(soapHeaderElement, classCodeValue);
            } catch (InsufficientRightsException e) {
                LOGGER.error("InsufficientRightsException: '{}'", e.getMessage(), e);
                registryErrorList.addChild(createErrorOMMessage(ns, e.getCode(), e.getMessage(), "", false));
                break processLabel;
            } catch (AssertionValidationException e) {
                LOGGER.error("AssertionValidationException: '{}'", e.getMessage(), e);
                registryErrorList.addChild(createErrorOMMessage(ns, e.getCode(), e.getMessage(), "", false));
                break processLabel;
            } catch (SMgrException e) {
                LOGGER.error("SMgrException: '{}'", e.getMessage(), e);
                registryErrorList.addChild(createErrorOMMessage(ns, "", e.getMessage(), "", false));
                break processLabel;
            }

            LOGGER.info("XCA Retrieve Request is valid.");
            OMElement homeCommunityId = factory.createOMElement("HomeCommunityId", ns2);
            homeCommunityId.setText(request.getDocumentRequest().get(0).getHomeCommunityId());
            documentResponse.addChild(homeCommunityId);

            OMElement repositoryUniqueId = factory.createOMElement("RepositoryUniqueId", ns2);
            repositoryUniqueId.setText(request.getDocumentRequest().get(0).getRepositoryUniqueId());
            documentResponse.addChild(repositoryUniqueId);

            OMElement documentUniqueId = factory.createOMElement("DocumentUniqueId", ns2);
            documentUniqueId.setText(documentId);
            documentResponse.addChild(documentUniqueId);

            OMElement mimeType = factory.createOMElement("mimeType", ns2);
            mimeType.setText("text/xml");
            documentResponse.addChild(mimeType);

            OMElement document = factory.createOMElement("Document", factory.createOMNamespace("urn:ihe:iti:xds-b:2007", ""));

            try {
                Document doc = epsosDoc.getDocument();
                LOGGER.debug("Client userID: '{}'", eventLog.getSC_UserID());

                if (doc != null) {

                    CdaValidationService cdaValidationService = CdaValidationService.getInstance();

                    /* Validate CDA epSOS Friendly */
                    cdaValidationService.validateModel(XMLUtils.toOM(doc.getDocumentElement()).toString(),
                            CdaModel.obtainCdaModel(epsosDoc.getClassCode(), false), NcpSide.NCP_A);

                    // Transcode to Epsos Pivot
                    doc = transformDocument(doc, registryErrorList, registryResponse, true, eventLog);
                    if (!checkIfOnlyWarnings(registryErrorList)) {

                        // If the transformation process has raised at least one FATAL Error, we should determine which
                        // XCAError code has to be provided according the corresponding TM Error Code
                        Iterator<OMElement> errors = registryErrorList.getChildElements();
                        while (errors.hasNext()) {

                            OMElement errorCode = errors.next();
                            LOGGER.error("Error: '{}'-'{}'", errorCode.getText(), errorCode.getAttributeValue(QName.valueOf("errorCode")));
                            if (StringUtils.equals(TMError.ERROR_REQUIRED_CODED_ELEMENT_NOT_TRANSCODED.getCode(), errorCode.getAttributeValue(QName.valueOf("errorCode")))) {
                                //throw new TranscodingErrorException("The requested encoding cannot be provided due to a transcoding error.");
                                registryErrorList.addChild(createErrorOMMessage(ns, "4203", "The requested encoding cannot be provided due to a transcoding error.", "", false));
                                // If the error is FATAL flag failure has been set to true
                                failure = true;
                                break;
                            }
                        }
                    }
                    /* Validate CDA epSOS Pivot */
                    cdaValidationService.validateModel(XMLUtils.toOM(doc.getDocumentElement()).toString(),
                            CdaModel.obtainCdaModel(epsosDoc.getClassCode(), true), NcpSide.NCP_A);
                }

                // If the registryErrorList is empty or contains only Warning, the status of the request is SUCCESS
                if (!registryErrorList.getChildElements().hasNext()) {
                    registryResponse.addAttribute(factory.createOMAttribute("status", null,
                            AdhocQueryResponseStatus.SUCCESS));
                } else {
                    if (checkIfOnlyWarnings(registryErrorList)) {
                        registryResponse.addAttribute(factory.createOMAttribute("status", null,
                                AdhocQueryResponseStatus.SUCCESS));
                    } else if (failure) {
                        // If there is a failure during the request process, the status is FAILURE
                        registryResponse.addAttribute(factory.createOMAttribute("status", null,
                                AdhocQueryResponseStatus.FAILURE));
                    } else {
                        //Otherwise the status is PARTIALSUCCESS
                        registryResponse.addAttribute(factory.createOMAttribute("status", null,
                                IheConstants.REGREP_RESPONSE_PARTIALSUCCESS));
                    }
                }

                // If there is no failure during the process, the CDA document has been attached to the response
                if (!failure) {
                    ByteArrayDataSource dataSource = null;
                    if (doc != null) {
                        dataSource = new ByteArrayDataSource(XMLUtils.toOM(doc.getDocumentElement())
                                .toString().getBytes(), "text/xml;charset=UTF-8");
                    }
                    DataHandler dataHandler = new DataHandler(dataSource);
                    OMText textData = factory.createOMText(dataHandler, true);
                    textData.setOptimize(true);
                    document.addChild(textData);

                    LOGGER.debug("Returning document '{}'", documentId);
                    documentResponse.addChild(document);
                    documentReturned = true;
                }
            } catch (Exception e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
                registryResponse.addAttribute(factory.createOMAttribute("status", null, AdhocQueryResponseStatus.FAILURE));
                registryErrorList.addChild(createErrorOMMessage(ns, "", e.getMessage(), "", false));
            }
        }
        LOGGER.info("Preparing Event Log of the Response:");
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
            LOGGER.error("Prepare Audit log failed. '{}'", ex.getMessage(), ex);
            // Is this fatal?
        }

        //Once the response and the audit message have been prepared, the registryErrorList might be cleaned depending of the errors status.
        if (failure) {
            //Only XCA Error Code defined into the XCA Profile might be attached to the response in case of FAILURE.
            Iterator<OMElement> errors = registryErrorList.getChildElements();
            while (errors.hasNext()) {

                OMElement errorCode = errors.next();
                LOGGER.error("Error: '{}'-'{}'", errorCode.getText(), errorCode.getAttributeValue(QName.valueOf("errorCode")));
                if (!StringUtils.equals(XCAError.ERROR_4203.getCode(), errorCode.getAttributeValue(QName.valueOf("errorCode")))) {
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


        boolean onlyWarnings = true;
        OMElement element;
        Iterator it = registryErrorList.getChildElements();
        while (it.hasNext()) {
            element = (OMElement) it.next();
            LOGGER.info("checkIfOnlyWarnings() - Element: '{}'", element.getText());
            if (StringUtils.equals(element.getAttribute(QName.valueOf("severity")).getAttributeValue(),
                    "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error")) {
                onlyWarnings = false;
            }
        }
        return onlyWarnings;
    }

    private AdhocQueryResponse handleUnsupportedOpertationException(AdhocQueryRequest request, UnsupportedOperationException e) {

        AdhocQueryResponse response = ofQuery.createAdhocQueryResponse();

        RegistryErrorList rel = ofRs.createRegistryErrorList();
        RegistryError re = ofRs.createRegistryError();

        // Create Registry Object List
        response.setRegistryObjectList(ofRim.createRegistryObjectListType());

        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        //TODO: Jerome: review this value during Exception handling
        re.setLocation("");
        //re.setLocation(result.toString());

        String classCode = getDocumentEntryClassCode(request);
        if (Constants.EP_CLASSCODE.equals(classCode)) {
            re.setSeverity("urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Info");
            re.setErrorCode("1101");
            re.setValue("No ePrescriptions are registered for the given patient.");
            re.setCodeContext("The XDS repository does not contain any ePrescription related to the current patient");
        } else if (Constants.PS_CLASSCODE.equals(classCode)) {
            re.setSeverity("urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning");
            re.setErrorCode("1102");
            re.setValue("No patient summary is registered for the given patient.");
            re.setCodeContext("The XDS repository does not contain any Patient Summary related to the current patient");
        } else {
            // What should be in the error, if the classCode is not EP_CLASSCODE nor PS_CLASSCODE
            re.setSeverity("urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning");
            re.setErrorCode("1100");
            re.setValue("No documents are registered for the given patient.");
            re.setCodeContext("The XDS repository does not contain any documents related to the current patient");
        }

        rel.getRegistryError().add(re);
        response.setRegistryErrorList(rel);
        response.setStatus(AdhocQueryResponseStatus.SUCCESS);

        return response;
    }

    /**
     * XCA list operation implementation, returns the list of patient summaries or ePrescriptions,
     * depending on the query.
     */
    @Override
    public AdhocQueryResponse queryDocument(AdhocQueryRequest adhocQueryRequest, SOAPHeader sh, EventLog eventLog)
            throws Exception {

        AdhocQueryResponse result = ofQuery.createAdhocQueryResponse();
        try {
            AdhocQueryResponseBuilder(adhocQueryRequest, result, sh, eventLog);
        } catch (UnsupportedOperationException uoe) {
            result = handleUnsupportedOpertationException(adhocQueryRequest, uoe);
        }
        return result;
    }

    /**
     * XCA retrieve operation implementation, returns the particular document requested by the caller.
     * The response is placed in the OMElement
     */
    @Override
    public void retrieveDocument(RetrieveDocumentSetRequestType request, SOAPHeader sh, EventLog eventLog,
                                 OMElement response) throws Exception {

        RetrieveDocumentSetBuilder(request, sh, eventLog, response);
    }

    /**
     * This auxiliary service returns the service name, based on a provided class code.
     */
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
