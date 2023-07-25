package epsos.ccd.gnomon.auditmanager;

import epsos.ccd.gnomon.utils.SecurityMgr;
import epsos.ccd.gnomon.utils.Utils;
import eu.epsos.util.audit.AuditLogSerializer;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditConstant;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import net.RFC3881.*;
import net.RFC3881.ActiveParticipantContents;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This class provides methods for constructing the audit message and for sending the syslog message to the repository.
 * Instances of the spSOS defined services, as for constructing and sending the syslog message to the repository.
 *
 * @author Kostas Karkaletsis
 */
public enum AuditTrailUtils {

    INSTANCE;
    private static final JAXBContext jaxbContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditTrailUtils.class);

    static {
        try {
            jaxbContext = JAXBContext.newInstance(AuditMessage.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return
     */
    public static AuditTrailUtils getInstance() {
        return INSTANCE;
    }

    /**
     * @param auditmessage
     * @param sign
     * @return
     */
    public static synchronized String constructMessage(AuditMessage auditmessage, boolean sign) {

        String auditMessage = "";
        String eventTypeCode = "EventTypeCode(N/A)";
        try {
            eventTypeCode = auditmessage.getEventIdentification() != null ?
                    auditmessage.getEventIdentification().getEventTypeCode().get(0).getCsdCode() : "EventTypeCode(N/A)";
            //eventTypeCode = auditmessage.getEventIdentification() != null ?
            //      auditmessage.getEventIdentification().getEventTypeCode().get(0).getCode() : "EventTypeCode(N/A)";
            LOGGER.debug("'{}' try to convert the message to xml using JAXB", eventTypeCode);
        } catch (NullPointerException e) {
            LOGGER.warn("Unable to log AuditMessageEventTypeCode.", e);
        }

        try {
            auditMessage = AuditTrailUtils.convertAuditObjectToXML(auditmessage);
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
        INSTANCE.writeTestAudits(auditmessage, auditMessage);
        LOGGER.info("Message constructed: '{}'", eventTypeCode);

        boolean validated = false;

        try {
            LOGGER.debug("'{}' Validating Schema", auditmessage.getEventIdentification() != null ?
                    auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
            //LOGGER.debug("'{}' Validating Schema", auditmessage.getEventIdentification() != null ?
              //      auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
            validated = Utils.validateSchema(auditMessage);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        boolean forceWrite = Boolean.parseBoolean(Utils.getProperty("auditrep.forcewrite", "true", true));
        if (!validated) {
            LOGGER.debug("'{}' Message not validated", auditmessage.getEventIdentification() != null ?
                    auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                    //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
            if (!forceWrite) {
                auditMessage = "";
            }
        }
        if (validated || forceWrite) {

            if (validated) {
                LOGGER.debug("'{}' Audit Message validated", auditmessage.getEventIdentification() != null ?
                        auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                        //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
            } else {
                LOGGER.debug("'{}' Audit Message not validated", auditmessage.getEventIdentification() != null ?
                        auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                        //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
            }

            if (forceWrite && !validated) {
                LOGGER.debug("'{}' AuditManager is force to send the message. So trying ...",
                        auditmessage.getEventIdentification() != null ?
                                auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                                //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
            }

            try {
                // Validating XML according to XSD
                LOGGER.debug("'{}' XML stuff: Create Dom From String",
                        auditmessage.getEventIdentification() != null ?
                                auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                                //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
                Document doc = Utils.createDomFromString(auditMessage);
                if (sign) {

                    auditMessage = SecurityMgr.getSignedDocumentAsString(SecurityMgr.signDocumentEnveloped(doc));
                    LOGGER.debug("'{}' Audit Message signed",
                            auditmessage.getEventIdentification() != null ?
                                    auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)");
                                    //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)");
                }
            } catch (Exception e) {
                auditMessage = "";
                LOGGER.error("'{}' Error signing doc: '{}'",
                        auditmessage.getEventIdentification() != null ?
                                auditmessage.getEventIdentification().getEventID().getCsdCode() : "EventTypeCode(N/A)",
                                //auditmessage.getEventIdentification().getEventID().getCode() : "EventTypeCode(N/A)",
                        e.getMessage(), e);
            }
        }
        return auditMessage;
    }

    /**
     * The method converts the audit message to xml format, having as input the Audit Message.
     * Uses the JAXB library to marshal the audit message object.
     *
     * @param auditMessage
     * @return
     * @throws JAXBException
     */
    public static synchronized String convertAuditObjectToXML(AuditMessage auditMessage) throws JAXBException {

        LOGGER.debug("Converting message - JAXB marshalling the Audit Object");
        StringWriter sw = new StringWriter();

        Marshaller marshaller = jaxbContext.createMarshaller();
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (PropertyException e) {
            LOGGER.error("Unable to format converted AuditMessage to XML: '{}'", e.getMessage(), e);
        }
        marshaller.marshal(auditMessage, sw);
        LOGGER.debug("Audit Messaged converted in XML stream");
        return sw.toString();
    }

    public static synchronized AuditMessage convertXMLToAuditObject(File xml) throws JAXBException {
        LOGGER.debug("Converting message - JAXB unmarshalling the Audit Object");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AuditMessage auditMessage = (AuditMessage) unmarshaller.unmarshal(xml);
        LOGGER.debug("XML converted in Audit Messaged");
        return auditMessage;
    }

    public static synchronized AuditMessage convertXMLToAuditObject(InputStream xml) throws JAXBException {
        LOGGER.debug("Converting message - JAXB unmarshalling the Audit Object");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AuditMessage auditMessage = (AuditMessage) unmarshaller.unmarshal(xml);
        LOGGER.debug("XML converted in Audit Messaged");
        return auditMessage;
    }

    /**
     * Method to create the audit message to be passed to the syslog client
     *
     * @param eventLog
     * @return the Audit Message object
     */
    public AuditMessage createAuditMessage(EventLog eventLog) {

        LOGGER.debug("createAuditMessage(EventLog '{}')", eventLog.getEventType());
        //TODO: Check if the Audit Message return with a null value shall be considered as fatal?
        AuditMessage message = new AuditMessage();
        AuditTrailUtils au = AuditTrailUtils.getInstance();
        if (StringUtils.equals(eventLog.getEventType(), EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            message = au.createAuditTrailForIdentificationService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PATIENT_SERVICE_LIST.getCode())) {
            message = au.createAuditTrailForPatientService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {
            message = au.createAuditTrailForPatientService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORDER_SERVICE_LIST.getCode())) {
            message = au.createAuditTrailForOrderService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORDER_SERVICE_RETRIEVE.getCode())) {
            message = au.createAuditTrailForOrderService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORCD_SERVICE_LIST.getCode())) {
            message = au.createAuditTrailForOrCDService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORCD_SERVICE_RETRIEVE.getCode())) {
            message = au.createAuditTrailForOrCDService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            message = au.createAuditTrailForDispensationService(eventLog, "Initialize");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {
            message = au.createAuditTrailForDispensationService(eventLog, AuditConstant.ACTION_DISCARD);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.HCP_AUTHENTICATION.getCode())) {
            message = au.createAuditTrailHCPIdentity(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.TRC_ASSERTION.getCode())) {
            message = au.createAuditTrailTRCAssertion(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.NOK_ASSERTION.getCode())) {
            message = au.createAuditTrailNOKAssertion(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            message = au.createAuditTrailNCPTrustedServiceList(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PIVOT_TRANSLATION.getCode())) {
            message = au.createAuditTrailPivotTranslation(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.COMMUNICATION_FAILURE.getCode())) {
            message = au.createAuditTrailForCommunicationFailure(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.SMP_QUERY.getCode())) {
            message = au._CreateAuditTrailForEhealthSMPQuery(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.SMP_PUSH.getCode())) {
            message = au._CreateAuditTrailForEhealthSMPPush(eventLog);
        }
        //  Non Repudiation information are not relevant for SML/SMP process
        if (!StringUtils.equals(eventLog.getEventType(), EventType.SMP_QUERY.getCode())
                && !StringUtils.equals(eventLog.getEventType(), EventType.SMP_PUSH.getCode())) {

            AuditTrailUtils.getInstance().addNonRepudiationSection(message, eventLog.getReqM_ParticipantObjectID(),
                    eventLog.getReqM_ParticipantObjectDetail(), eventLog.getResM_ParticipantObjectID(),
                    eventLog.getResM_ParticipantObjectDetail());
        }

        //TODO: Check if the Audit Message return with a null value shall be considered as fatal?
        /* Invoke audit message validation services */
        if (OpenNCPValidation.isValidationEnable()) {

            if (message == null) {
                LOGGER.error("Validation of the Audit Message cannot proceed on a Null value!!!");
            } else {
                validateAuditMessage(eventLog, message);
            }
        }
        return message;
    }

    /**
     * @param auditMessage
     * @param participantIdRequest
     * @param participantDetailRequest
     * @param participantIdResponse
     * @param participantDetailResponse
     */
    public void addNonRepudiationSection(AuditMessage auditMessage, String participantIdRequest, byte[] participantDetailRequest,
                                         String participantIdResponse, byte[] participantDetailResponse) {

        //TODO: Based on the current guidelines and functional specifications, this is not clear enough if an evidence
        // has to be generated including the Non-Repudiation section (Type Value pair attributes - security header)
        // while the Audit Message has been considering NCP internal actions.

        // Request Participant
        ParticipantObjectIdentificationContents participantRequest = createParticipantObjectIdentification("req",
                participantIdRequest, participantDetailRequest);
        auditMessage.getParticipantObjectIdentification().add(participantRequest);

        // Response Participant
        ParticipantObjectIdentificationContents participantResponse = createParticipantObjectIdentification("rsp",
                participantIdResponse, participantDetailResponse);

        auditMessage.getParticipantObjectIdentification().add(participantResponse);
    }

    /**
     * @param action
     * @param participantObjectId
     * @param participantObjectDetail
     * @return
     */
    private ParticipantObjectIdentificationContents createParticipantObjectIdentification(String action, String participantObjectId,
                                                                                      byte[] participantObjectDetail) {

        var participantObjectIdentification = new ParticipantObjectIdentificationContents();
        participantObjectIdentification.setParticipantObjectID(participantObjectId);
        participantObjectIdentification.setParticipantObjectTypeCode("4");

        ParticipantObjectIDTypeCode codedValueType = new ParticipantObjectIDTypeCode();
        codedValueType.setCsdCode(action);
        codedValueType.setCodeSystemName("eHealth DSI Msg");
        codedValueType.setOriginalText(action);
        if (StringUtils.equals("rsp", action)) {
            codedValueType.setDisplayName("Response Message");
        } else {
            codedValueType.setDisplayName("Request Message");
        }

        participantObjectIdentification.setParticipantObjectIDTypeCode(codedValueType);

        if (ArrayUtils.isNotEmpty(participantObjectDetail)) {
            ParticipantObjectDetail detail = new ParticipantObjectDetail();
            detail.setType("securityheader");
            detail.setValue(participantObjectDetail);
            participantObjectIdentification.getParticipantObjectDetail().add(detail);
        }
        return participantObjectIdentification;
    }

    private ParticipantObjectIdentificationType OldcreateParticipantObjectIdentification(String action, String participantObjectId,
                                                                                      byte[] participantObjectDetail) {

        var participantObjectIdentification = new ParticipantObjectIdentificationType();
        participantObjectIdentification.setParticipantObjectID(participantObjectId);
        participantObjectIdentification.setParticipantObjectTypeCode(Short.valueOf("4"));

        var codedValueType = new CodedValueType();
        codedValueType.setCode(action);
        codedValueType.setCodeSystemName("eHealth DSI Msg");
        if (StringUtils.equals("rsp", action)) {
            codedValueType.setDisplayName("Response Message");
        } else {
            codedValueType.setDisplayName("Request Message");
        }
        participantObjectIdentification.setParticipantObjectIDTypeCode(codedValueType);

        if (ArrayUtils.isNotEmpty(participantObjectDetail)) {
            TypeValuePairType typeValuePairType = new TypeValuePairType();
            typeValuePairType.setType("securityheader");
            typeValuePairType.setValue(participantObjectDetail);
            participantObjectIdentification.getParticipantObjectDetail().add(typeValuePairType);
        }
        return participantObjectIdentification;
    }

    /**
     * Constructs an Audit Message for the Patient Privacy Audit schema in eHealth NCP Query
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForEhealthSMPQuery(EventLog eventLog) {

        AuditMessage message = createAuditTrailForEhealthSMPQuery(eventLog);
        if (message != null) {
            //  TODO: Audit - Event Target
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "SMP", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "SignedServiceMetadata");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the Patient Privacy Audit schema in eHealth NCP Push
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForEhealthSMPPush(EventLog eventLog) {

        AuditMessage message = createAuditTrailForEhealthSMPPush(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "SMP", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "SignedServiceMetadata");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS Order Service According schema is HCP Assurance.
     *
     * @param eventLog - the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForOrderService(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addParticipantObject(message, eventLog.getReqM_ParticipantObjectID(), Short.valueOf("2"), Short.valueOf("24"),
                    "Patient", "ITI-38", "IHE Transactions", "Patient Number",
                    "Cross Gateway Patient Discovery", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());

            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"), AuditConstant.DICOM, "Cross Gateway Query");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the eHDSI OrCD Service According schema is HCP Assurance.
     *
     * @param eventLog - the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForOrCDService(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {

            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"), "", "");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for HCP Identity According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailHCPIdentity(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPIdentity(eventLog);
        if (message != null) {
            // Event Target
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "IdA", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "HCP Identity Assertion");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for NCP Trusted Service List According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailNCPTrustedServiceList(EventLog eventLog) {

        AuditMessage message = createAuditTrailForNCPTrustedServiceList(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "NSL", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "Trusted Service List");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Pivot Translation According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailPivotTranslation(EventLog eventLog) {

        AuditMessage message = createAuditTrailForPivotTranslation(eventLog);
        if (message != null) {
            // Event Target
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("4"), Short.valueOf("5"),
                    "in", "eHealth DSI Translation", "Input Data");
            addEventTarget(message, Arrays.asList(eventLog.getEventTargetAdditionalObjectId()), Short.valueOf("4"), Short.valueOf("5"),
                    "out", "eHealth DSI Translation", "Output Data");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for TRCA According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailTRCAssertion(EventLog eventLog) {

        AuditMessage message = createAuditTrailForTRCA(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "TrcA", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "TRC Assertion");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Next Of Kin Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailNOKAssertion(EventLog eventLog) {

        var message = createAuditTrailForNOKA(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "NokA", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY, "NOK Assertion");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS Dispensation Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @param action   the action of the service
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForDispensationService(EventLog eventLog, String action) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        // Event Target
        if (message != null) {
            if (action.equals(AuditConstant.ACTION_DISCARD)) {
                addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", AuditConstant.ACTION_DISCARD, Short.valueOf("14"), "", "");

            } else {
                addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", "", Short.valueOf("0"), "", "");
            }
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS HCER Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     * @deprecated
     */
    @Deprecated
    private AuditMessage createAuditTrailForHCERService(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"), "", "");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the Consent Service According schema is HCP Assurance
     *
     * @param eventLog - the EventLog object
     * @param action   - the action of the service
     * @return the created AuditMessage object
     * @deprecated
     */
    @Deprecated
    private AuditMessage createAuditTrailForConsentService(EventLog eventLog, String action) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            if (StringUtils.equalsIgnoreCase(action, AuditConstant.ACTION_DISCARD)) {
                addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", action, Short.valueOf("14"), "", "");
            }
            if (StringUtils.equalsIgnoreCase(action, "Put")) {
                addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", "Put", Short.valueOf("0"), "", "");
            }
            if (StringUtils.equalsIgnoreCase(action, "Pin")) {
                addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("4"), Short.valueOf("12"),
                        "PIN", AuditConstant.CODE_SYSTEM_EHDSI_SECURITY,
                        "Privacy Information Notice");
            }
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS Patient Service According schema is Patient Service.
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForPatientService(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addParticipantObject(message, eventLog.getReqM_ParticipantObjectID(), Short.valueOf("2"), Short.valueOf("24"),
                    "Patient", "ITI-38", "IHE Transactions", "Patient Number",
                    "Cross Gateway Query", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());

            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"), AuditConstant.DICOM, "Cross Gateway Query");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS Identification Service
     * According schema is Mapping Service
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForIdentificationService(EventLog eventLog) {

        AuditMessage message;
        // If patient id mapping has occurred (there is a patient source ID), use patient mapping audit scheme
        if (eventLog.getPS_ParticipantObjectID() != null) {
            message = createAuditTrailForPatientMapping(eventLog);
        } else {
            message = createAuditTrailForHCPAssurance(eventLog);
        }

        addParticipantObject(message, eventLog.getHciIdentifier(), Short.valueOf("2"), Short.valueOf("24"),
                "Patient", "ITI-55", "IHE Transactions", "Patient Number",
                "Cross Gateway Patient Discovery", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());

        return message;
    }

    /**
     * Constructs an Audit Message for the epSOS Identification Service According schema is Mapping Service
     *
     * @param eventLog - the EventLog object
     * @return the created AuditMessage object
     * @deprecated
     */
    @Deprecated
    private AuditMessage createAuditTrailForPACService(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("24"),
                    "10", "", Short.valueOf("0"), "", "");
        }
        return message;
    }

    /**
     * Constructs an Audit Message for the generic Request For Data Scheme
     *
     * @param eventLog - the EventLog object
     * @return the created AuditMessage object
     * @deprecated
     */
    @Deprecated
    private AuditMessage createAuditTrailForRequestOfData(EventLog eventLog) {

        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("24"),
                    "10", "", Short.valueOf("0"), "", "");
        }
        return message;
    }

    /**
     * @param auditMessage
     * @param auditSource
     * @return
     */
    private AuditMessage addAuditSource(AuditMessage auditMessage, String auditSource) {

        AuditSourceIdentificationContents auditSourceIdentification = new AuditSourceIdentificationContents();
        auditSourceIdentification.setAuditSourceID(auditSource);
        auditSourceIdentification.setAuditEnterpriseSiteID(auditSource);

        /*
        attribute code {
            "1" |                 ## End-user display device, diagnostic device
            "2" |                 ## Data acquisition device or instrument
            "3" |                 ## Web Server process or thread
            "4" |                 ## Application Server process or thread
            "5" |                 ## Database Server process or thread
            "6" |                 ## Security server, e.g., a domain controller
            "7" |                 ## ISO level 1-3 network component
            "8" |                 ## ISO level 4-6 operating software
            "9" |                 ## other
            token },              ## other values are allowed if a codeSystemName is present
        */
        AuditSourceTypeCode auditTypeSource = new AuditSourceTypeCode();
        auditTypeSource.setCsdCode("4");
        auditSourceIdentification.getAuditSourceTypeCode().add(auditTypeSource);

        auditMessage.setAuditSourceIdentification(auditSourceIdentification);
        return auditMessage;
    }

    /**
     * @param eventType
     * @return
     */
    private String getMappedEventType(String eventType) {

        if (StringUtils.equals(eventType, EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            return IHEEventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode();
        }
        if (StringUtils.equals(eventType, EventType.COMMUNICATION_FAILURE.getCode())) {
            return IHEEventType.COMMUNICATION_FAILURE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.CONSENT_SERVICE_DISCARD.getCode())) {
            return IHEEventType.CONSENT_SERVICE_DISCARD.getCode();
        }
        if (StringUtils.equals(eventType, EventType.CONSENT_SERVICE_PIN.getCode())) {
            return IHEEventType.CONSENT_SERVICE_PIN.getCode();
        }
        if (StringUtils.equals(eventType, EventType.CONSENT_SERVICE_PUT.getCode())) {
            return IHEEventType.CONSENT_SERVICE_PUT.getCode();
        }
        if (StringUtils.equals(eventType, EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {
            return IHEEventType.DISPENSATION_SERVICE_DISCARD.getCode();
        }
        if (StringUtils.equals(eventType, EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            return IHEEventType.DISPENSATION_SERVICE_INITIALIZE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.HCER_PUT.getCode())) {
            return IHEEventType.HCER_PUT.getCode();
        }
        if (StringUtils.equals(eventType, EventType.HCP_AUTHENTICATION.getCode())) {
            return IHEEventType.HCP_AUTHENTICATION.getCode();
        }
        if (StringUtils.equals(eventType, EventType.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            return IHEEventType.NCP_TRUSTED_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(eventType, EventType.ORDER_SERVICE_LIST.getCode())) {
            return IHEEventType.ORDER_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(eventType, EventType.ORDER_SERVICE_RETRIEVE.getCode())) {
            return IHEEventType.ORDER_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.PAC_RETRIEVE.getCode())) {
            return IHEEventType.PAC_RETRIEVE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.PATIENT_SERVICE_LIST.getCode())) {
            return IHEEventType.PATIENT_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(eventType, EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {
            return IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.ORCD_SERVICE_LIST.getCode())) {
            return IHEEventType.ORCD_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(eventType, EventType.ORCD_SERVICE_RETRIEVE.getCode())) {
            return IHEEventType.ORCD_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.PIVOT_TRANSLATION.getCode())) {
            return IHEEventType.PIVOT_TRANSLATION.getCode();
        }
        if (StringUtils.equals(eventType, EventType.TRC_ASSERTION.getCode())) {
            return IHEEventType.TRC_ASSERTION.getCode();
        }
        if (StringUtils.equals(eventType, EventType.NOK_ASSERTION.getCode())) {
            return IHEEventType.NOK_ASSERTION.getCode();
        }
        if (StringUtils.equals(eventType, EventType.MRO_LIST.getCode())) {
            return IHEEventType.MRO_LIST.getCode();
        }
        if (StringUtils.equals(eventType, EventType.MRO_RETRIEVE.getCode())) {
            return IHEEventType.MRO_RETRIEVE.getCode();
        }
        if (StringUtils.equals(eventType, EventType.SMP_QUERY.getCode())) {
            return IHEEventType.SMP_QUERY.getCode();
        }
        if (StringUtils.equals(eventType, EventType.SMP_PUSH.getCode())) {
            return IHEEventType.SMP_PUSH.getCode();
        }
        // TODO: Fix this issue, does the mappedEventType should be initialized?
        return "Event Type Not Mapped";
    }

    /**
     * @param operation
     * @return
     */
    private String getMappedTransactionName(String operation) {

        if (StringUtils.equals(operation, TransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            return IHETransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.COMMUNICATION_FAILURE.getCode())) {
            return IHETransactionName.COMMUNICATION_FAILURE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.DISPENSATION_SERVICE_DISCARD.getCode())) {
            return IHETransactionName.DISPENSATION_SERVICE_DISCARD.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            return IHETransactionName.DISPENSATION_SERVICE_INITIALIZE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.HCP_AUTHENTICATION.getCode())) {
            return IHETransactionName.HCP_AUTHENTICATION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            return IHETransactionName.NCP_TRUSTED_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORCD_SERVICE_LIST.getCode())) {
            return IHETransactionName.ORCD_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORCD_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionName.ORCD_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORDER_SERVICE_LIST.getCode())) {
            return IHETransactionName.ORDER_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORDER_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionName.ORDER_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PATIENT_SERVICE_LIST.getCode())) {
            return IHETransactionName.PATIENT_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PATIENT_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionName.PATIENT_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PIVOT_TRANSLATION.getCode())) {
            return IHETransactionName.PIVOT_TRANSLATION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.TRC_ASSERTION.getCode())) {
            return IHETransactionName.TRC_ASSERTION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.NOK_ASSERTION.getCode())) {
            return IHETransactionName.NOK_ASSERTION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.SMP_QUERY.getCode())) {
            return IHETransactionName.SMP_QUERY.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.SMP_PUSH.getCode())) {
            return IHETransactionName.SMP_PUSH.getCode();
        }
        // TODO: Fix this issue, does the mappedEventType should be initialized?
        return "Transaction not Mapped";
    }

    private String getMappedTransactionOriginalName(String operation) {

        if (StringUtils.equals(operation, TransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            return IHETransactionOriginalName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.COMMUNICATION_FAILURE.getCode())) {
            return IHETransactionOriginalName.COMMUNICATION_FAILURE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.DISPENSATION_SERVICE_DISCARD.getCode())) {
            return IHETransactionOriginalName.DISPENSATION_SERVICE_DISCARD.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            return IHETransactionOriginalName.DISPENSATION_SERVICE_INITIALIZE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.HCP_AUTHENTICATION.getCode())) {
            return IHETransactionOriginalName.HCP_AUTHENTICATION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            return IHETransactionOriginalName.NCP_TRUSTED_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORCD_SERVICE_LIST.getCode())) {
            return IHETransactionOriginalName.ORCD_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORCD_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionOriginalName.ORCD_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORDER_SERVICE_LIST.getCode())) {
            return IHETransactionOriginalName.ORDER_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.ORDER_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionOriginalName.ORDER_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PATIENT_SERVICE_LIST.getCode())) {
            return IHETransactionOriginalName.PATIENT_SERVICE_LIST.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PATIENT_SERVICE_RETRIEVE.getCode())) {
            return IHETransactionOriginalName.PATIENT_SERVICE_RETRIEVE.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.PIVOT_TRANSLATION.getCode())) {
            return IHETransactionOriginalName.PIVOT_TRANSLATION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.TRC_ASSERTION.getCode())) {
            return IHETransactionOriginalName.TRC_ASSERTION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.NOK_ASSERTION.getCode())) {
            return IHETransactionOriginalName.NOK_ASSERTION.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.SMP_QUERY.getCode())) {
            return IHETransactionOriginalName.SMP_QUERY.getCode();
        }
        if (StringUtils.equals(operation, TransactionName.SMP_PUSH.getCode())) {
            return IHETransactionOriginalName.SMP_PUSH.getCode();
        }
        // TODO: Fix this issue, does the mappedEventType should be initialized?
        return "Transaction Original Name not Mapped";
    }

    /**
     * @param auditMessage
     * @param eventType
     * @param transactionName
     * @param eventActionCode
     * @param eventDateTime
     * @param eventOutcomeIndicator
     * @return
     */
    private AuditMessage addEventIdentification(AuditMessage auditMessage, String eventType, String transactionName,
                                                String eventActionCode, XMLGregorianCalendar eventDateTime,
                                                BigInteger eventOutcomeIndicator) {

        // Change EventType to new ones
        //EventIdentificationType eventIdentification = new EventIdentificationType();
        EventIdentificationContents eventIdentification = new EventIdentificationContents();

        EventID eventID = new EventID();
        eventID.setCsdCode("ITI-39".equals(getMappedEventType(eventType)) ? "110107" : "110112"); // TODO : extract it?
        eventID.setCodeSystemName("DCM");
        eventID.setDisplayName(getMappedTransactionName(transactionName));
        eventID.setOriginalText("Query"); // extract it? (getMappedTransactionName(transactionName));
        eventIdentification.setEventID(eventID);

        EventTypeCode eventTypeCode = new EventTypeCode();
        eventTypeCode.setCsdCode(getMappedEventType(eventType));
        eventTypeCode.setCodeSystemName("IHE Transactions");
        eventTypeCode.setOriginalText(getMappedTransactionOriginalName(transactionName));
        eventIdentification.getEventTypeCode().add(eventTypeCode);

        if (StringUtils.equals(eventType, EventType.PATIENT_SERVICE_LIST.getCode())
                || StringUtils.equals(eventType, EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {

            // IHE Validator will complain if we add this code - PS
            //eventIdentification.getEventTypeCode().add(createCodedValue("60591-5", AuditConstant.CODE_SYSTEM_LOINC, "Patient Summary Document"));
        }
        if (StringUtils.equals(eventType, EventType.ORDER_SERVICE_LIST.getCode())
                || StringUtils.equals(eventType, EventType.ORDER_SERVICE_RETRIEVE.getCode())) {

            // IHE Validator will complain if we add this code - EP
            //eventIdentification.getEventTypeCode().add(createCodedValue("57833-6", AuditConstant.CODE_SYSTEM_LOINC, "Prescription for Medication"));
        }
        if (StringUtils.equals(eventType, EventType.CONSENT_SERVICE_PUT.getCode())
                || StringUtils.equals(eventType, EventType.CONSENT_SERVICE_DISCARD.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("57016-8", AuditConstant.CODE_SYSTEM_LOINC, "Privacy Policy Acknowledgement Document"));
        }
        if (StringUtils.equals(eventType, EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("60593-1", AuditConstant.CODE_SYSTEM_LOINC, "Medication Dispensed Document"));
        }
        if (StringUtils.equals(eventType, EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {
            eventIdentification.getEventTypeCode().add(createCodedValue("DISCARD-60593-1", AuditConstant.CODE_SYSTEM_LOINC, "Discard Medication Dispensed"));
        }
        if (StringUtils.equals(eventType, EventType.HCER_PUT.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("34133-9", AuditConstant.CODE_SYSTEM_LOINC, "Summary of Episode Note"));
        }
        if (StringUtils.equals(eventType, EventType.PAC_RETRIEVE.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("N/A", AuditConstant.CODE_SYSTEM_LOINC, "PAC"));
        }
        if (StringUtils.equals(eventType, EventType.SMP_QUERY.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("SMP", "EHDSI-193", "SMP::Query"));
            eventIdentification.getEventID().setCsdCode("SMP");
            eventIdentification.getEventID().setCodeSystemName("EHDSI-193");
            eventIdentification.getEventID().setDisplayName("SMP::Query");
        }
        if (StringUtils.equals(eventType, EventType.SMP_PUSH.getCode())) {

            eventIdentification.getEventTypeCode().add(createCodedValue("SMP", "EHDSI-194", "SMP::Push"));
            eventIdentification.getEventID().setCsdCode("SMP");
            eventIdentification.getEventID().setCodeSystemName("EHDSI-194");
            eventIdentification.getEventID().setDisplayName("SMP::Push");
        }

        eventIdentification.setEventActionCode(eventActionCode); // supposed tp be 'E'
        eventIdentification.setEventDateTime(eventDateTime);
        eventIdentification.setEventOutcomeIndicator(eventOutcomeIndicator.toString());

        auditMessage.setEventIdentification(eventIdentification);

        return auditMessage;
    }

    /**
     * @param message
     * @param userId
     * @param roleId
     * @param userIsRequester
     * @param codeSystem
     * @return
     */
    private AuditMessage addPointOfCare(AuditMessage message, String userId, String roleId, boolean userIsRequester, String codeSystem) {

        if (StringUtils.isBlank(userId)) {
            LOGGER.debug("This is service provider and doesn't need Point of Care");
        } else {//ActiveParticipantType   participant2 = new ActiveParticipantType();//participant2.setNetworkAccessPointID();
            ActiveParticipantContents participant = new ActiveParticipantContents();
            participant.setUserID(userId);
            participant.setAlternativeUserID(userId);
            participant.setNetworkAccessPointID("127.0.0.1");
            participant.setNetworkAccessPointTypeCode("1");
            participant.setUserIsRequestor(userIsRequester);

            RoleIDCode codedValue = new RoleIDCode();
            codedValue.setCsdCode("110152");
            codedValue.setCodeSystemName("DCM");
            codedValue.setOriginalText(codeSystem); // Should be "Destination Role ID"
            participant.getRoleIDCode().add(codedValue);
            message.getActiveParticipant().add(participant);
        }
        return message;
    }

    /**
     * @param auditMessage
     * @param userId
     * @param alternativeUserID
     * @param roleId
     * @param userIsRequester
     * @return
     */
    private AuditMessage addHumanRequestor(AuditMessage auditMessage, String userId, String alternativeUserID,
                                           String roleId, boolean userIsRequester) {

        ActiveParticipantContents humanRequester = new ActiveParticipantContents();
        humanRequester.setUserID(userId);
        humanRequester.setAlternativeUserID(alternativeUserID);
        humanRequester.setNetworkAccessPointID("127.0.0.1");
        humanRequester.setNetworkAccessPointTypeCode("2");
        humanRequester.setUserIsRequestor(userIsRequester);

        RoleIDCode humanRequesterRoleId = new RoleIDCode();
        humanRequesterRoleId.setCsdCode("110153");
        humanRequesterRoleId.setOriginalText(roleId);
        humanRequesterRoleId.setCodeSystemName("DCM");
        humanRequesterRoleId.setOriginalText("Source Role ID");

        humanRequester.getRoleIDCode().add(humanRequesterRoleId);
        auditMessage.getActiveParticipant().add(humanRequester);
        return auditMessage;
    }

    /**
     * @param auditMessage
     * @param userId
     * @param userIsRequester
     * @param code
     * @param codeSystem
     * @param displayName
     * @return
     */
    private AuditMessage addService(AuditMessage auditMessage, String userId, boolean userIsRequester, String code,
                                    String codeSystem, String displayName) {

        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (StringUtils.isBlank(userId)) {
            LOGGER.warn("No Service, as this is Service Consumer");
            throw new IllegalArgumentException("Both ServiceConsumer User ID and ServiceProvider User ID must exist!");
        } else {
            ActiveParticipantContents activeParticipant = new ActiveParticipantContents();
            activeParticipant.setUserID(userId);
            activeParticipant.setAlternativeUserID(userId);
            activeParticipant.setUserIsRequestor(userIsRequester);

            RoleIDCode serviceConsumerRoleId = new RoleIDCode();
            serviceConsumerRoleId.setCsdCode(code);
            serviceConsumerRoleId.setCodeSystemName(codeSystem);
            serviceConsumerRoleId.setDisplayName(displayName);
            serviceConsumerRoleId.setOriginalText(displayName);
            activeParticipant.getRoleIDCode().add(serviceConsumerRoleId);
            auditMessage.getActiveParticipant().add(activeParticipant);
        }
        return auditMessage;
    }
    /**
     * @param auditMessage
     * @param userId
     * @param userIsRequester
     * @param code
     * @param codeSystem
     * @param displayName
     * @param ipAddress
     * @return
     */
    private AuditMessage addService(AuditMessage auditMessage, String userId, boolean userIsRequester, String code,
                                    String codeSystem, String displayName, String ipAddress) {

        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (StringUtils.isBlank(userId)) {
            LOGGER.warn("No Service, as this is Service Consumer");
            throw new IllegalArgumentException("Both ServiceConsumer User ID and ServiceProvider User ID must exist!");
        } else {
            ActiveParticipantContents activeParticipant = new ActiveParticipantContents();
            activeParticipant.setNetworkAccessPointID(ipAddress);
            if (validator.isValidInet4Address(ipAddress) || validator.isValidInet6Address(ipAddress)) {
                activeParticipant.setNetworkAccessPointTypeCode("2");
            } else {
                activeParticipant.setNetworkAccessPointTypeCode("1");
            }
            activeParticipant.setUserID(userId);
            activeParticipant.setAlternativeUserID(userId);
            activeParticipant.setUserIsRequestor(userIsRequester);

            RoleIDCode serviceConsumerRoleId = new RoleIDCode();
            serviceConsumerRoleId.setCsdCode(code);
            serviceConsumerRoleId.setCodeSystemName(codeSystem);
            serviceConsumerRoleId.setDisplayName(displayName);
            serviceConsumerRoleId.setOriginalText(displayName);
            activeParticipant.getRoleIDCode().add(serviceConsumerRoleId);
            auditMessage.getActiveParticipant().add(activeParticipant);
        }
        return auditMessage;
    }

    /**
     * @param auditMessage
     * @param participantId
     * @param participantCode
     * @param participantRole
     * @param participantName
     * @param PS_ObjectCode
     * @param PS_ObjectCodeName
     * @param PS_ObjectCodeValue
     * @return
     */
    private AuditMessage addParticipantObject(AuditMessage auditMessage, String participantId, Short participantCode,
                                              Short participantRole, String participantName, String PS_ObjectCode, String PS_ObjectCodeName,
                                              String PS_ObjectCodeValue, String PS_originalText,
                                              String PS_getQueryByParameterPayload, String PS_getHciIdentifierPayload) {

        ParticipantObjectIdentificationContents participantObjectIdentification = new ParticipantObjectIdentificationContents();
        participantObjectIdentification.setParticipantObjectID(participantId);
        participantObjectIdentification.setParticipantObjectTypeCode(participantCode.toString());
        participantObjectIdentification.setParticipantObjectTypeCodeRole(participantRole.toString());

        ParticipantObjectIDTypeCode codedValue = new ParticipantObjectIDTypeCode();
        codedValue.setCsdCode(PS_ObjectCode);
        codedValue.setCodeSystemName(PS_ObjectCodeName);
        codedValue.setDisplayName(PS_ObjectCodeValue);
        codedValue.setOriginalText(PS_originalText);

        // SystemObject and Query
        if(participantCode == 2 && participantRole == 24) {
            ParticipantObjectDetail participantObjectDetail = new ParticipantObjectDetail();
            participantObjectDetail.setType("ihe:homeCommunityID");
            try {
                participantObjectDetail.setValue(PS_getHciIdentifierPayload.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                participantObjectDetail.setValue(new byte[]{2});
                LOGGER.debug("Error addParticipantObject() - participantObjectDetail: '{}'", e.getMessage());
            }
            try {
                participantObjectIdentification.setParticipantObjectQuery(PS_getQueryByParameterPayload.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                participantObjectIdentification.setParticipantObjectQuery(new byte[]{2});
                LOGGER.debug("Error addParticipantObject() - participantObjectIdentification: '{}'", e.getMessage());
            }
            participantObjectIdentification.getParticipantObjectDetail().add(participantObjectDetail);
        }

        participantObjectIdentification.setParticipantObjectIDTypeCode(codedValue);
        auditMessage.getParticipantObjectIdentification().add(participantObjectIdentification);
        return auditMessage;
    }

    /**
     * @param auditMessage
     * @param errorMessagePartObjectId
     * @param errorMessagePartObjectDetail
     * @param errorMessageCode
     * @param errorMessageCodeRole
     * @param errorMessageTypeCode
     * @param errorMessageQualifier
     * @return
     */
    private AuditMessage addError(AuditMessage auditMessage, String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail,
                                  Short errorMessageCode, Short errorMessageCodeRole, String errorMessageTypeCode,
                                  String errorMessageQualifier) {

        // Error Message handling for audit purpose
        if (StringUtils.isNotBlank(errorMessagePartObjectId)) {

            LOGGER.debug("Error Message Participant ID is: '{}'", errorMessagePartObjectId);
            ParticipantObjectIDTypeCode codedValueType = new ParticipantObjectIDTypeCode();
            codedValueType.setCsdCode(errorMessageTypeCode);
            codedValueType.setOriginalText("error message");
            codedValueType.setCodeSystemName("DCM");

            ParticipantObjectIdentificationContents participantObjectIdentificationType = new ParticipantObjectIdentificationContents();
            participantObjectIdentificationType.setParticipantObjectID(errorMessagePartObjectId);
            participantObjectIdentificationType.setParticipantObjectTypeCode(errorMessageCode.toString());
            participantObjectIdentificationType.setParticipantObjectTypeCodeRole(errorMessageCodeRole.toString());
            participantObjectIdentificationType.setParticipantObjectIDTypeCode(codedValueType);

            if (errorMessagePartObjectDetail != null) {
                ParticipantObjectDetail typeValuePairType = new ParticipantObjectDetail();
                typeValuePairType.setType(errorMessageQualifier);
                typeValuePairType.setValue(errorMessagePartObjectDetail);
                participantObjectIdentificationType.getParticipantObjectDetail().add(typeValuePairType);
            }
            auditMessage.getParticipantObjectIdentification().add(participantObjectIdentificationType);

        } else {
            LOGGER.debug("No Error Message reported by the auditing process!");
        }
        return auditMessage;
    }

    /**
     * @param auditMessage
     * @param eventTargetObjectId
     * @param typeCode
     * @param typeCodeRole
     * @param errorMessageCode
     * @param action
     * @param objectDataLifeCycle
     * @return
     */
    private AuditMessage addEventTarget(AuditMessage auditMessage, List<String> eventTargetObjectId, Short typeCode,
                                        Short typeCodeRole, String errorMessageCode, String action, Short objectDataLifeCycle,
                                        String EM_CodeSystemName, String EM_DisplayName) {

        LOGGER.debug("AuditMessage addEventTarget('{}','{}','{}','{}','{}','{}','{}')", auditMessage, eventTargetObjectId,
                typeCode, typeCodeRole, errorMessageCode, action, objectDataLifeCycle);
        for (String eventTargetId : eventTargetObjectId) {

            ParticipantObjectIdentificationContents em = new ParticipantObjectIdentificationContents();
            em.setParticipantObjectID(eventTargetId);
            em.setParticipantObjectTypeCode(typeCode.toString());
            em.setParticipantObjectTypeCodeRole(typeCodeRole.toString());
            ParticipantObjectIDTypeCode errorMessageCodedValueType = new ParticipantObjectIDTypeCode();
            errorMessageCodedValueType.setCsdCode(errorMessageCode);
            errorMessageCodedValueType.setCodeSystemName(EM_CodeSystemName);
            errorMessageCodedValueType.setOriginalText(EM_DisplayName);
            errorMessageCodedValueType.setDisplayName(EM_DisplayName);
            if (action.equals(AuditConstant.ACTION_DISCARD) || action.equals("Pin")) {
                em.setParticipantObjectDataLifeCycle(objectDataLifeCycle.toString());
            }
            em.setParticipantObjectIDTypeCode(errorMessageCodedValueType);
            auditMessage.getParticipantObjectIdentification().add(em);
        }
        return auditMessage;
    }

    /**
     * @param auditMessage
     * @param eventTargetObjectId
     * @param objectTypeCode
     * @param objectDataLifeCycle
     * @param EM_Code
     * @param EM_CodeSystemName
     * @param EM_DisplayName
     * @return
     */
    private AuditMessage addEventTarget(AuditMessage auditMessage, List<String> eventTargetObjectId, Short objectTypeCode,
                                        Short objectDataLifeCycle, String EM_Code, String EM_CodeSystemName, String EM_DisplayName) {

        LOGGER.debug("AuditMessage addEventTarget('{}','{}','{}','{}','{}','{}','{}')", auditMessage, eventTargetObjectId,
                objectTypeCode, objectDataLifeCycle, EM_Code, EM_CodeSystemName, EM_DisplayName);

        for (String eventTargetId : eventTargetObjectId) {

            ParticipantObjectIdentificationContents eventTarget = new ParticipantObjectIdentificationContents();
            eventTarget.setParticipantObjectID(eventTargetId);
            eventTarget.setParticipantObjectTypeCode(objectTypeCode.toString());
            if (objectDataLifeCycle != null) {
                eventTarget.setParticipantObjectDataLifeCycle(objectDataLifeCycle.toString());
            }
            ParticipantObjectIDTypeCode eventTargetDescription = new ParticipantObjectIDTypeCode();
            eventTargetDescription.setCsdCode(EM_Code);
            eventTargetDescription.setCodeSystemName(EM_CodeSystemName);
            eventTargetDescription.setDisplayName(EM_DisplayName);
            eventTargetDescription.setOriginalText(EM_DisplayName);
            eventTarget.setParticipantObjectIDTypeCode(eventTargetDescription);
            auditMessage.getParticipantObjectIdentification().add(eventTarget);
        }
        return auditMessage;
    }

    /**
     * Constructs an Audit Message for Patient Privacy Audit Schema for eHealth SMP Query
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForEhealthSMPQuery(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Provider", eventLog.getTargetip());
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Patient Privacy Audit Schema for eHealth SMP Push
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForEhealthSMPPush(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider", eventLog.getTargetip());
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for HCP Assurance Schema
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForHCPAssurance(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addPointOfCare(message, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), false,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    false);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer"); // eventLog.getSourceip()
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider"); // eventLog.getTargetip()
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "Patient", "2", AuditConstant.DICOM, "Patient Number",
                    "Cross Gateway Patient Discovery", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for CommunicationFailure
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForCommunicationFailure(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addAuditSource(message, "N/A");
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider", eventLog.getTargetip());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "Patient", "2", AuditConstant.DICOM, "Patient Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Issuance of a Treatment Relationship Confirmation Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForTRCA(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            // Audit Source
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());
            // Point Of Care
            addPointOfCare(message, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            // Human Requestor
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER, AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer",
                    eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider",
                    eventLog.getTargetip());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"), "Patient",
                     "2", AuditConstant.DICOM, "Patient Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Issuance of a Next Of Kin Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForNOKA(EventLog eventLog) {
        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            // Audit Source
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());
            // Point Of Care
            addPointOfCare(message, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            // Human Requestor
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER, AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer",
                    eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider",
                    eventLog.getTargetip());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("10"), "Guarantor",
                    "7", AuditConstant.DICOM, "Guarantor Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for NCP Trusted Service List
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForNCPTrustedServiceList(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            // Audit Source
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Pivot Translation of a Medical Document
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForPivotTranslation(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());

            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for HCP Identity Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForHCPIdentity(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            // Audit Source
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());
            // Point Of Care
            addPointOfCare(message, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            // Human Requester
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * Constructs an Audit Message for Patient Mapping Schema
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForPatientMapping(EventLog eventLog) {

        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Provider", eventLog.getTargetip());
            addService(message, eventLog.getSP_UserID(), false, "MasterPatientIndex", AuditConstant.CODE_SYSTEM_EHDSI,
                    "Master Patient Index", eventLog.getTargetip());
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addParticipantObject(message, eventLog.getPS_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "PatientSource", "2", AuditConstant.DICOM, "Patient Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "PatientTarget", "2", AuditConstant.DICOM, "Patient Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(message.toString());
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }

    /**
     * @param auditmessage
     * @param auditmsg
     */
    private void writeTestAudits(AuditMessage auditmessage, String auditmsg) {

        String wta = ConfigurationManagerFactory.getConfigurationManager().getProperty("WRITE_TEST_AUDITS");
        LOGGER.debug("Writing test audits: '{}'", wta);
        if (StringUtils.equals(wta, "true")) {

            String tap = Utils.getProperty("TEST_AUDITS_PATH");
            try {
                if (auditmessage.getEventIdentification() != null) {
                    Utils.writeXMLToFile(auditmsg, tap + (auditmessage.getEventIdentification().getEventTypeCode()
                            .get(0).getDisplayName().split("::"))[0] + "-" +
                            new SimpleDateFormat("yyyy.MM.dd'at'HH-mm-ss.SSS").format(new Date(System.currentTimeMillis())) + ".xml");
                }
            } catch (Exception e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
                try {
                    Utils.writeXMLToFile(auditmsg, tap + new SimpleDateFormat("yyyy.MM.dd'at'HH-mm-ss.SSS")
                            .format(new Date(System.currentTimeMillis())) + "-ERROR.xml");
                } catch (Exception ex) {
                    LOGGER.warn("Unable to write test audit: '{}'", ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * @param auditLogSerializer
     * @param auditMessage
     * @param facility
     * @param severity
     */
    public synchronized void sendATNASyslogMessage(AuditLogSerializer auditLogSerializer, AuditMessage auditMessage,
                                                   String facility, String severity) {

        LOGGER.info("[Audit Util] Starting new thread for sending message");
        MessageSender messageSender = new MessageSender();
        new Thread(() -> messageSender.send(auditLogSerializer, auditMessage, facility, severity)).start();
    }

    /**
     * Based on a given EventLog this method will infer about the correct model to apply for a message validation.
     *
     * @param eventLog an EventLog object containing information about the audit message.
     * @return the Audit Message Model
     */
    private void validateAuditMessage(EventLog eventLog, AuditMessage auditMessage) {

        LOGGER.debug("validateAuditMessage(EventLog '{}', AuditMessage '{}', PC UserId: '{}')", eventLog.getEventType(),
                auditMessage.getEventIdentification() != null ? auditMessage.getEventIdentification().getEventActionCode() : "N/A",
                eventLog.getPC_UserID());
        try {
            // Infer model according to NCP Side and EventCode
            NcpSide ncpSide = eventLog.getNcpSide();

            if (StringUtils.equals(eventLog.getEventType(), "EHDSI-CF")) {
                throw new UnsupportedOperationException("EventCode not supported.");
            }
            OpenNCPValidation.validateAuditMessage(convertAuditObjectToXML(auditMessage), eventLog.getEventType(), ncpSide);
        } catch (JAXBException e) {
            LOGGER.error("JAXBException: {}", e.getMessage(), e);
        }
    }

    /**
     * Internal method creating a CodedValueType object required by the Audit Message.
     *
     * @param code           - Code of the Event.
     * @param codeSystemName - Name of the Code System handling the Event Code.
     * @param displayName    - Human readable value of the Event Code.
     * @return Initialized CodedValueType used to identify the type of Event from the Audit Messages.
     */
    private EventTypeCode createCodedValue(String code, String codeSystemName, String displayName) {

        EventTypeCode type = new ObjectFactory().createEventTypeCode();
        type.setCsdCode(code);
        type.setCodeSystemName(codeSystemName);
        type.setDisplayName(displayName);
        return type;
    }
}
