package epsos.ccd.gnomon.auditmanager;

import epsos.ccd.gnomon.utils.SecurityMgr;
import epsos.ccd.gnomon.utils.Utils;
import eu.epsos.util.audit.AuditLogSerializer;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import net.RFC3881.*;
import net.RFC3881.AuditMessage.ActiveParticipant;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.math.BigInteger;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditTrailUtils.class);

    private static JAXBContext jaxbContext;

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
            eventTypeCode = auditmessage.getEventIdentification().getEventTypeCode().get(0).getCode();
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
            LOGGER.debug("'{}' Validating Schema", auditmessage.getEventIdentification().getEventID().getCode());
            validated = Utils.validateSchema(auditMessage);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        boolean forceWrite = Boolean.parseBoolean(Utils.getProperty("auditrep.forcewrite", "true", true));
        if (!validated) {
            LOGGER.debug("'{}' Message not validated", auditmessage.getEventIdentification().getEventID().getCode());
            if (!forceWrite) {
                auditMessage = "";
            }
        }
        if (validated || forceWrite) {

            if (validated) {
                LOGGER.debug("'{}' Audit Message validated", auditmessage.getEventIdentification().getEventID().getCode());
            } else {
                LOGGER.debug("'{}' Audit Message not validated", auditmessage.getEventIdentification().getEventID().getCode());
            }

            if (forceWrite && !validated) {
                LOGGER.debug("'{}' AuditManager is force to send the message. So trying ...",
                        auditmessage.getEventIdentification().getEventID().getCode());
            }

            try {
                // Validating XML according to XSD
                LOGGER.debug("'{}' XML stuff: Create Dom From String", auditmessage.getEventIdentification().getEventID().getCode());
                Document doc = Utils.createDomFromString(auditMessage);
                if (sign) {

                    auditMessage = SecurityMgr.getSignedDocumentAsString(SecurityMgr.signDocumentEnveloped(doc));
                    LOGGER.debug("'{}' Audit Message signed", auditmessage.getEventIdentification().getEventID().getCode());
                }
            } catch (Exception e) {
                auditMessage = "";
                LOGGER.error("'{}' Error signing doc: '{}'", auditmessage.getEventIdentification().getEventID().getCode(), e.getMessage(), e);
            }
        }
        return auditMessage;
    }

    /**
     * The method converts the audit message to xml format, having as input the Audit Message.
     * Uses the JAXB library to marshal the audit message object.
     *
     * @param am
     * @return
     * @throws JAXBException
     */
    public static synchronized String convertAuditObjectToXML(AuditMessage am) throws JAXBException {

        LOGGER.debug("Converting message - JAXB marshalling the Audit Object");
        StringWriter sw = new StringWriter();

        Marshaller marshaller = jaxbContext.createMarshaller();
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (PropertyException e) {
            LOGGER.error("Unable to format converted AuditMessage to XML: '{}'", e.getMessage(), e);
        }
        marshaller.marshal(am, sw);
        LOGGER.debug("Audit Messaged converted in XML stream");
        return sw.toString();
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
        AuditMessage am = new AuditMessage();
        AuditTrailUtils au = AuditTrailUtils.getInstance();
        if (StringUtils.equals(eventLog.getEventType(), EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            am = au._CreateAuditTrailForIdentificationService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PATIENT_SERVICE_LIST.getCode())) {
            am = au._CreateAuditTrailForPatientService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {
            am = au._CreateAuditTrailForPatientService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORDER_SERVICE_LIST.getCode())) {
            am = au._CreateAuditTrailForOrderService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.ORDER_SERVICE_RETRIEVE.getCode())) {
            am = au._CreateAuditTrailForOrderService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            am = au._CreateAuditTrailForDispensationService(eventLog, "Initialize");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {
            am = au._CreateAuditTrailForDispensationService(eventLog, "Discard");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.CONSENT_SERVICE_PUT.getCode())) {
            am = au._CreateAuditTrailForConsentService(eventLog, "Put");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.CONSENT_SERVICE_DISCARD.getCode())) {
            am = au._CreateAuditTrailForConsentService(eventLog, "Discard");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.CONSENT_SERVICE_PIN.getCode())) {
            am = au._CreateAuditTrailForConsentService(eventLog, "Pin");
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.HCP_AUTHENTICATION.getCode())) {
            am = au._CreateAuditTrailHCPIdentity(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.TRC_ASSERTION.getCode())) {
            am = au._CreateAuditTrailTRCA(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            am = au._CreateAuditTrailNCPTrustedServiceList(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PIVOT_TRANSLATION.getCode())) {
            am = au._CreateAuditTrailPivotTranslation(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.COMMUNICATION_FAILURE.getCode())) {
            am = au.createAuditTrailForCommunicationFailure(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.PAC_RETRIEVE.getCode())) {
            am = au._CreateAuditTrailForPACService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.HCER_PUT.getCode())) {
            am = au._CreateAuditTrailForHCERService(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.MRO_LIST.getCode())) {
            am = au._CreateAuditTrailForRequestOfData(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.MRO_RETRIEVE.getCode())) {
            am = au._CreateAuditTrailForRequestOfData(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.SMP_QUERY.getCode())) {
            am = au._CreateAuditTrailForEhealthSMPQuery(eventLog);
        }
        if (StringUtils.equals(eventLog.getEventType(), EventType.SMP_PUSH.getCode())) {
            am = au._CreateAuditTrailForEhealthSMPPush(eventLog);
        }
        //  Non Repudiation information are not relevant for SML/SMP process
        if (!StringUtils.equals(eventLog.getEventType(), EventType.SMP_QUERY.getCode())
                && !StringUtils.equals(eventLog.getEventType(), EventType.SMP_PUSH.getCode())) {

            am = AuditTrailUtils.getInstance().addNonRepudiationSection(am, eventLog.getReqM_ParticipantObjectID(),
                    eventLog.getReqM_PatricipantObjectDetail(), eventLog.getResM_ParticipantObjectID(),
                    eventLog.getResM_PatricipantObjectDetail());
        }

        //TODO: Check if the Audit Message return with a null value shall be considered as fatal?
        /* Invoke audit message validation services */
        if (OpenNCPValidation.isValidationEnable()) {

            if (am == null) {
                LOGGER.error("Validation of the Audit Message cannot proceed on a Null value!!!");
            } else {
                validateAuditMessage(eventLog, am);
            }
        }
        return am;
    }

    /**
     * @param auditMessage
     * @param participantObjectIDRequest
     * @param participantObjectDetailRequest
     * @param participantObjectIDResponse
     * @param participantObjectDetailResponse
     * @return
     */
    public AuditMessage addNonRepudiationSection(AuditMessage auditMessage, String participantObjectIDRequest,
                                                 byte[] participantObjectDetailRequest, String participantObjectIDResponse,
                                                 byte[] participantObjectDetailResponse) {

        //TODO: Based on the current guidelines and functional specifications, this is not clear enough if an evidence
        // has to be generated including the Non-Repudiation section (Type Value pair attributes - security header)
        // while the Audit Message has been considering NCP internal actions.
        // Request
        ParticipantObjectIdentificationType poiRequest = createParticipantObjectIdentification("req",
                participantObjectIDRequest, participantObjectDetailRequest);
        auditMessage.getParticipantObjectIdentification().add(poiRequest);

        // Response
        ParticipantObjectIdentificationType poiResponse = createParticipantObjectIdentification("rsp",
                participantObjectIDResponse, participantObjectDetailResponse);
        auditMessage.getParticipantObjectIdentification().add(poiResponse);

        return auditMessage;
    }

    /**
     * @param action
     * @param participantObjectId
     * @param participantObjectDetail
     * @return
     */
    private ParticipantObjectIdentificationType createParticipantObjectIdentification(String action, String participantObjectId,
                                                                                      byte[] participantObjectDetail) {

        ParticipantObjectIdentificationType participantObjectIdentification = new ParticipantObjectIdentificationType();
        participantObjectIdentification.setParticipantObjectID(participantObjectId);
        participantObjectIdentification.setParticipantObjectTypeCode(Short.valueOf("4"));

        CodedValueType codedValue = new CodedValueType();
        codedValue.setCode(action);
        codedValue.setCodeSystemName("eHealth DSI Msg");
        if (StringUtils.equals("rsp", action)) {
            codedValue.setDisplayName("Response Message");
        } else {
            codedValue.setDisplayName("Request Message");
        }
        participantObjectIdentification.setParticipantObjectIDTypeCode(codedValue);

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

        AuditMessage am = createAuditTrailForEhealthSMPQuery(eventLog);
        if (am != null) {
            //  TODO: Audit - Event Target
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "SMP", "eHealth DSI Security", "SignedServiceMetadata");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the Patient Privacy Audit schema in eHealth NCP Push
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForEhealthSMPPush(EventLog eventLog) {

        AuditMessage am = createAuditTrailForEhealthSMPPush(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "SMP", "eHealth DSI Security", "SignedServiceMetadata");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS Order Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForOrderService(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {

            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"));
        }
        return am;
    }

    /**
     * Constructs an Audit Message for HCP Identity According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailHCPIdentity(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPIdentity(eventLog);
        if (am != null) {
            // Event Target
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "IdA", "eHealth DSI Security", "HCP Identity Assertion");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for NCP Trusted Service List According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailNCPTrustedServiceList(EventLog eventLog) {

        AuditMessage am = createAuditTrailForNCPTrustedServiceList(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "NSL", "eHealth DSI Security", "Trusted Service List");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for Pivot Translation According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailPivotTranslation(EventLog eventLog) {

        AuditMessage am = createAuditTrailForPivotTranslation(eventLog);
        if (am != null) {
            // Event Target
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("4"), Short.valueOf("5"),
                    "in", "eHealth DSI Translation", "Input Data");
            addEventTarget(am, Arrays.asList(eventLog.getEventTargetAdditionalObjectId()), Short.valueOf("4"), Short.valueOf("5"),
                    "out", "eHealth DSI Translation", "Output Data");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for TRCA According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailTRCA(EventLog eventLog) {

        AuditMessage am = createAuditTrailForTRCA(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), null,
                    "TrcA", "eHealth DSI Security", "TRC Assertion");
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS Dispensation Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @param action   the action of the service
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForDispensationService(EventLog eventLog, String action) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        // Event Target
        if (am != null) {
            if (action.equals("Discard")) {
                addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", "Discard", Short.valueOf("14"));

            } else {
                addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", "", Short.valueOf("0"));
            }
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS HCER Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForHCERService(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"));
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS Consent Service According schema is HCP Assurance
     *
     * @param eventLog the EventLog object
     * @param action   the action of the service
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForConsentService(EventLog eventLog, String action) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {
            if (StringUtils.equalsIgnoreCase(action, "Discard")) {
                addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", action, Short.valueOf("14"));
            }

            if (StringUtils.equalsIgnoreCase(action, "Put")) {
                addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", "Put", Short.valueOf("0"));
            }
            if (StringUtils.equalsIgnoreCase(action, "Pin")) {
                addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("4"), Short.valueOf("12"),
                        "PIN", "eHealth DSI Security",
                        "Privacy Information Notice");
            }
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS Patient Service According schema is Patient Service.
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForPatientService(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {

            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"));
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the epSOS Identification Service
     * According schema is Mapping Service
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForIdentificationService(EventLog eventLog) {

        // If patient id mapping has occurred (there is a patient source ID), use patient mapping audit scheme
        if (eventLog.getPS_PatricipantObjectID() != null) {
            return createAuditTrailForPatientMapping(eventLog);
        } else {
            return createAuditTrailForHCPAssurance(eventLog);
        }
    }

    /**
     * Constructs an Audit Message for the epSOS Identification Service According schema is Mapping Service
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForPACService(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("24"),
                    "10", "", Short.valueOf("0"));
        }
        return am;
    }

    /**
     * Constructs an Audit Message for the generic Request For Data Scheme
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage _CreateAuditTrailForRequestOfData(EventLog eventLog) {

        AuditMessage am = createAuditTrailForHCPAssurance(eventLog);
        if (am != null) {
            addEventTarget(am, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("24"),
                    "10", "", Short.valueOf("0"));
        }
        return am;
    }

    /**
     * @param am
     * @param auditSource
     * @return
     */
    private AuditMessage addAuditSource(AuditMessage am, String auditSource) {

        AuditSourceIdentificationType auditSourceIdentification = new AuditSourceIdentificationType();
        auditSourceIdentification.setAuditSourceID(auditSource);
        am.getAuditSourceIdentification().add(auditSourceIdentification);
        return am;
    }

    /**
     * @param eventType
     * @return
     */
    private String getMappedEventType(String eventType) {

        if (eventType.equals(
                epsos.ccd.gnomon.auditmanager.EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.COMMUNICATION_FAILURE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.COMMUNICATION_FAILURE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.CONSENT_SERVICE_DISCARD.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.CONSENT_SERVICE_DISCARD.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.CONSENT_SERVICE_PIN.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.CONSENT_SERVICE_PIN.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.CONSENT_SERVICE_PUT.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.CONSENT_SERVICE_PUT.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.DISPENSATION_SERVICE_DISCARD.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.DISPENSATION_SERVICE_INITIALIZE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.HCER_PUT.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.HCER_PUT.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.HCP_AUTHENTICATION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.HCP_AUTHENTICATION.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.NCP_TRUSTED_SERVICE_LIST.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.ORDER_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.ORDER_SERVICE_LIST.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.ORDER_SERVICE_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.ORDER_SERVICE_RETRIEVE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.PAC_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.PAC_RETRIEVE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.PATIENT_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.PATIENT_SERVICE_LIST.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.PIVOT_TRANSLATION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.PIVOT_TRANSLATION.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.TRC_ASSERTION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.TRC_ASSERTION.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.MRO_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.MRO_LIST.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.MRO_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.MRO_RETRIEVE.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.SMP_QUERY.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.SMP_QUERY.getCode();
        }
        if (eventType.equals(epsos.ccd.gnomon.auditmanager.EventType.SMP_PUSH.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHEEventType.SMP_PUSH.getCode();
        }
        // TODO: Fix this issue, does the mappedEventType should be initialized?
        return "Event Type Not Mapped";
    }

    /**
     * @param name
     * @return
     */
    private String getMappedTransactionName(String name) {

        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS
                .getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS
                    .getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.COMMUNICATION_FAILURE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.COMMUNICATION_FAILURE.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.CONSENT_SERVICE_DISCARD.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.CONSENT_SERVICE_DISCARD.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.CONSENT_SERVICE_PIN.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.CONSENT_SERVICE_PIN.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.CONSENT_SERVICE_PUT.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.CONSENT_SERVICE_PUT.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.DISPENSATION_SERVICE_DISCARD.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.DISPENSATION_SERVICE_DISCARD.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.DISPENSATION_SERVICE_INITIALIZE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.DISPENSATION_SERVICE_INITIALIZE.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.HCER_PUT.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.HCER_PUT.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.HCP_AUTHENTICATION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.HCP_AUTHENTICATION.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.NCP_TRUSTED_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.NCP_TRUSTED_SERVICE_LIST.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.ORDER_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.ORDER_SERVICE_LIST.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.ORDER_SERVICE_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.ORDER_SERVICE_RETRIEVE.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.PATIENT_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.PATIENT_SERVICE_LIST.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.PATIENT_SERVICE_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.PATIENT_SERVICE_RETRIEVE.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.PIVOT_TRANSLATION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.PIVOT_TRANSLATION.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.TRC_ASSERTION.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.TRC_ASSERTION.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.MRO_SERVICE_LIST.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.MRO_SERVICE_LIST.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.MRO_SERVICE_RETRIEVE.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.MRO_SERVICE_RETRIEVE.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.SMP_QUERY.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.SMP_QUERY.getCode();
        }
        if (name.equals(epsos.ccd.gnomon.auditmanager.TransactionName.SMP_PUSH.getCode())) {
            return epsos.ccd.gnomon.auditmanager.IHETransactionName.SMP_PUSH.getCode();
        }
        // TODO: Fix this issue, does the mappedEventType should be initialized?
        return "Transaction not Mapped";
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
        EventIdentificationType eit = new EventIdentificationType();

        CodedValueType iheEventID = new CodedValueType();
        iheEventID.setCode(getMappedEventType(eventType));
        iheEventID.setCodeSystemName("IHE Transactions");
        iheEventID.setDisplayName(getMappedTransactionName(transactionName));
        eit.setEventID(iheEventID);

        CodedValueType eventID = new CodedValueType();
        eventID.setCode(eventType);
        eventID.setCodeSystemName("eHDSI Transactions");
        eventID.setDisplayName(transactionName);
        eit.getEventTypeCode().add(eventID);

        if (eventType.equals(EventType.PATIENT_SERVICE_LIST.getCode())
                || eventType.equals(EventType.PATIENT_SERVICE_RETRIEVE.getCode())) {

            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("60591-5");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("Patient Summary Document");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.ORDER_SERVICE_LIST.getCode())
                || eventType.equals(EventType.ORDER_SERVICE_RETRIEVE.getCode())) {

            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("57833-6");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("Prescription for Medication");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.CONSENT_SERVICE_PUT.getCode())
                || eventType.equals(EventType.CONSENT_SERVICE_DISCARD.getCode())) {

            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("57016-8");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("Privacy Policy Acknowledgement Document");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.DISPENSATION_SERVICE_INITIALIZE.getCode())
                || eventType.equals(EventType.DISPENSATION_SERVICE_DISCARD.getCode())) {

            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("60593-1");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("Medication Dispensed Document");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.HCER_PUT.getCode())) {
            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("34133-9");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("Summary of Episode Note");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.PAC_RETRIEVE.getCode())) {
            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("N/A");
            eventID_epsos.setCodeSystemName("LOINC");
            eventID_epsos.setDisplayName("PAC");
            eit.getEventTypeCode().add(eventID_epsos);
        }
        if (eventType.equals(EventType.SMP_QUERY.getCode())) {
            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("SMP");
            eventID_epsos.setCodeSystemName("EHDSI-193");
            eventID_epsos.setDisplayName("SMP::Query");
            eit.getEventTypeCode().add(eventID_epsos);

            eit.getEventID().setCode("SMP");
            eit.getEventID().setCodeSystemName("EHDSI-193");
            eit.getEventID().setDisplayName("SMP::Query");
        }
        if (eventType.equals(EventType.SMP_PUSH.getCode())) {
            CodedValueType eventID_epsos = new CodedValueType();
            eventID_epsos.setCode("SMP");
            eventID_epsos.setCodeSystemName("EHDSI-194");
            eventID_epsos.setDisplayName("SMP::Push");
            eit.getEventTypeCode().add(eventID_epsos);

            eit.getEventID().setCode("SMP");
            eit.getEventID().setCodeSystemName("EHDSI-194");
            eit.getEventID().setDisplayName("SMP::Push");
        }

        eit.setEventActionCode(eventActionCode);
        eit.setEventDateTime(eventDateTime);
        eit.setEventOutcomeIndicator(eventOutcomeIndicator);
        auditMessage.setEventIdentification(eit);

        return auditMessage;
    }

    /**
     * @param am
     * @param PC_UserID
     * @param PC_RoleID
     * @param userIsRequester
     * @param codeSystem
     * @return
     */
    private AuditMessage addPointOfCare(AuditMessage am, String PC_UserID, String PC_RoleID, boolean userIsRequester,
                                        String codeSystem) {

        if (StringUtils.isBlank(PC_UserID)) {
            LOGGER.debug("This is service provider and doesn't need Point of Care");
        } else {
            ActiveParticipant a = new ActiveParticipant();
            a.setUserID(PC_UserID);
            a.setUserIsRequestor(userIsRequester);
            CodedValueType roleId = new CodedValueType();
            roleId.setCode(PC_RoleID);
            roleId.setCodeSystem(codeSystem);
            a.getRoleIDCode().add(roleId);
            am.getActiveParticipant().add(a);
        }
        return am;
    }

    /**
     * @param auditMessage
     * @param HR_UserID
     * @param HR_AlternativeUserID
     * @param HR_RoleID
     * @param userIsRequester
     * @return
     */
    private AuditMessage addHumanRequestor(AuditMessage auditMessage, String HR_UserID, String HR_AlternativeUserID,
                                           String HR_RoleID, boolean userIsRequester) {

        ActiveParticipant humanRequester = new ActiveParticipant();
        humanRequester.setUserID(HR_UserID);
        humanRequester.setAlternativeUserID(HR_AlternativeUserID);
        humanRequester.setUserIsRequestor(userIsRequester);
        CodedValueType humanRequesterRoleId = new CodedValueType();
        humanRequesterRoleId.setCode(HR_RoleID);
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
     * @param ipAddress
     * @return
     */
    private AuditMessage addService(AuditMessage auditMessage, String userId, boolean userIsRequester, String code,
                                    String codeSystem, String displayName, String ipAddress) {

        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (StringUtils.isBlank(userId)) {
            LOGGER.warn("No Service, as this is Service Consumer");
        } else {
            ActiveParticipant activeParticipant = new ActiveParticipant();
            activeParticipant.setNetworkAccessPointID(ipAddress);
            if (validator.isValidInet4Address(ipAddress) || validator.isValidInet6Address(ipAddress)) {
                activeParticipant.setNetworkAccessPointTypeCode(Short.valueOf("2"));
            } else {
                activeParticipant.setNetworkAccessPointTypeCode(Short.valueOf("1"));
            }
            activeParticipant.setUserID(userId);
            activeParticipant.setUserIsRequestor(userIsRequester);
            CodedValueType scroleId = new CodedValueType();
            scroleId.setCode(code);
            scroleId.setCodeSystem(codeSystem);
            scroleId.setDisplayName(displayName);
            activeParticipant.getRoleIDCode().add(scroleId);
            auditMessage.getActiveParticipant().add(activeParticipant);
        }
        return auditMessage;
    }

    /**
     * @param am
     * @param PS_PatricipantObjectID
     * @param PS_TypeCode
     * @param PS_TypeRole
     * @param PS_Name
     * @param PS_ObjectCode
     * @param PS_ObjectCodeName
     * @param PS_ObjectCodeValue
     * @return
     */
    private AuditMessage addParticipantObject(AuditMessage am, String PS_PatricipantObjectID, Short PS_TypeCode,
                                              Short PS_TypeRole, String PS_Name, String PS_ObjectCode, String PS_ObjectCodeName,
                                              String PS_ObjectCodeValue) {

        ParticipantObjectIdentificationType poit = new ParticipantObjectIdentificationType();
        poit.setParticipantObjectID(PS_PatricipantObjectID);
        poit.setParticipantObjectTypeCode(PS_TypeCode);
        poit.setParticipantObjectTypeCodeRole(PS_TypeRole);
        poit.setParticipantObjectName(PS_Name);
        CodedValueType PS_object = new CodedValueType();
        PS_object.setCode(PS_ObjectCode);
        PS_object.setCodeSystemName(PS_ObjectCodeName);
        PS_object.setDisplayName(PS_ObjectCodeValue);
        poit.setParticipantObjectIDTypeCode(PS_object);
        am.getParticipantObjectIdentification().add(poit);
        return am;
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
            CodedValueType codedValueType = new CodedValueType();
            codedValueType.setCode(errorMessageTypeCode);

            ParticipantObjectIdentificationType participantObjectIdentificationType = new ParticipantObjectIdentificationType();
            participantObjectIdentificationType.setParticipantObjectID(errorMessagePartObjectId);
            participantObjectIdentificationType.setParticipantObjectTypeCode(errorMessageCode);
            participantObjectIdentificationType.setParticipantObjectTypeCodeRole(errorMessageCodeRole);
            participantObjectIdentificationType.setParticipantObjectIDTypeCode(codedValueType);

            if (errorMessagePartObjectDetail != null) {
                TypeValuePairType typeValuePairType = new TypeValuePairType();
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
                                        Short typeCodeRole, String errorMessageCode, String action, Short objectDataLifeCycle) {

        LOGGER.debug("AuditMessage addEventTarget('{}','{}','{}','{}','{}','{}','{}')", auditMessage, eventTargetObjectId,
                typeCode, typeCodeRole, errorMessageCode, action, objectDataLifeCycle);
        for (String eventTargetId : eventTargetObjectId) {

            ParticipantObjectIdentificationType em = new ParticipantObjectIdentificationType();
            em.setParticipantObjectID(eventTargetId);
            em.setParticipantObjectTypeCode(typeCode);
            em.setParticipantObjectTypeCodeRole(typeCodeRole);
            CodedValueType errorMessageCodedValueType = new CodedValueType();
            errorMessageCodedValueType.setCode(errorMessageCode);
            if (action.equals("Discard") || action.equals("Pin")) {
                em.setParticipantObjectDataLifeCycle(objectDataLifeCycle);
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

            ParticipantObjectIdentificationType eventTarget = new ParticipantObjectIdentificationType();
            eventTarget.setParticipantObjectID(eventTargetId);
            eventTarget.setParticipantObjectTypeCode(objectTypeCode);
            if (objectDataLifeCycle != null) {
                eventTarget.setParticipantObjectDataLifeCycle(objectDataLifeCycle);
            }
            CodedValueType eventTargetDescription = new CodedValueType();
            eventTargetDescription.setCode(EM_Code);
            eventTargetDescription.setCodeSystemName(EM_CodeSystemName);
            eventTargetDescription.setDisplayName(EM_DisplayName);
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

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI",
                    "eHealth DSI Service Consumer", eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI",
                    "eHealth DSI Service Provider", eventLog.getTargetip());
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            addError(am, eventLog.getEM_PatricipantObjectID(), eventLog.getEM_PatricipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for Patient Privacy Audit Schema for eHealth SMP Push
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForEhealthSMPPush(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI", "eHealth DSI Service Consumer",
                    eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI", "eHealth DSI Service Provider",
                    eventLog.getTargetip());
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            addError(am, eventLog.getEM_PatricipantObjectID(), eventLog.getEM_PatricipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for HCP Assurance Schema
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForHCPAssurance(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addPointOfCare(am, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            addHumanRequestor(am, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI", "eHealth DSI Service Consumer",
                    eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI", "eHealth DSI Service Provider",
                    eventLog.getTargetip());
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            addParticipantObject(am, eventLog.getPT_PatricipantObjectID(), Short.valueOf("1"), Short.valueOf("1"), "Patient",
                    "2", "RFC-3881", "Patient Number");
            addError(am, eventLog.getEM_PatricipantObjectID(), eventLog.getEM_PatricipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for CommunicationFailure
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForCommunicationFailure(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addAuditSource(am, "N/A");
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addHumanRequestor(am, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI", "eHealth DSI Service Consumer",
                    eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI", "eHealth DSI Service Provider",
                    eventLog.getTargetip());
            addParticipantObject(am, eventLog.getPT_PatricipantObjectID(), Short.valueOf("1"), Short.valueOf("1"), "Patient",
                    "2", "RFC-3881", "Patient Number");
            addError(am, eventLog.getEM_PatricipantObjectID(), eventLog.getEM_PatricipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for Issuance of a Treatment Relationship Confirmation Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForTRCA(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            // Audit Source
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());
            // Point Of Care
            addPointOfCare(am, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            // Human Requestor
            addHumanRequestor(am, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI", "eHealth DSI Service Consumer",
                    eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI", "eHealth DSI Service Provider",
                    eventLog.getTargetip());
            addParticipantObject(am, eventLog.getPT_PatricipantObjectID(), Short.valueOf("1"), Short.valueOf("1"), "Patient",
                    "2", "RFC-3881", "Patient Number");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for NCP Trusted Service List
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForNCPTrustedServiceList(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            // Audit Source
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI", "eHealth DSI Service Consumer",
                    eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI", "eHealth DSI Service Provider",
                    eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for Pivot Translation of a Medical Document
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForPivotTranslation(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());

            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI",
                    "eHealth DSI Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for HCP Identity Assertion
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForHCPIdentity(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            // Audit Source
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            // Event Identification
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(), "E",
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator());
            // Point Of Care
            addPointOfCare(am, eventLog.getPC_UserID(), eventLog.getPC_RoleID(), true,
                    "1.3.6.1.4.1.12559.11.10.1.3.2.2.2");
            // Human Requester
            addHumanRequestor(am, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI",
                    "eHealth DSI Service Consumer", eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI",
                    "eHealth DSI Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * Constructs an Audit Message for Patient Mapping Schema
     *
     * @param eventLog the EventLog object
     * @return the created AuditMessage object
     */
    private AuditMessage createAuditTrailForPatientMapping(EventLog eventLog) {

        AuditMessage am = null;
        try {
            ObjectFactory of = new ObjectFactory();
            am = of.createAuditMessage();
            addEventIdentification(am, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator());
            addHumanRequestor(am, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(am, eventLog.getSC_UserID(), true, "ServiceConsumer", "eHealth DSI",
                    "eHealth DSI Service Consumer", eventLog.getSourceip());
            addService(am, eventLog.getSP_UserID(), false, "ServiceProvider", "eHealth DSI",
                    "eHealth DSI Service Provider", eventLog.getTargetip());
            addService(am, eventLog.getSP_UserID(), false, "MasterPatientIndex", "eHealth DSI",
                    "Master Patient Index", eventLog.getTargetip());
            addAuditSource(am, eventLog.getAS_AuditSourceId());
            addParticipantObject(am, eventLog.getPS_PatricipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "PatientSource", "2", "RFC-3881", "Patient Number");
            addParticipantObject(am, eventLog.getPT_PatricipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "PatientTarget", "2", "RFC-3881", "Patient Number");
            addError(am, eventLog.getEM_PatricipantObjectID(), eventLog.getEM_PatricipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(am.toString());
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return am;
    }

    /**
     * @param auditmessage
     * @param auditmsg
     */
    private void writeTestAudits(AuditMessage auditmessage, String auditmsg) {

        String wta = Constants.WRITE_TEST_AUDITS;
        LOGGER.debug("Writing test audits: '{}'", wta);
        if (StringUtils.equals(wta, "true")) {

            String tap = Utils.getProperty("TEST_AUDITS_PATH");
            try {
                Utils.writeXMLToFile(auditmsg, tap + (auditmessage.getEventIdentification().getEventTypeCode()
                        .get(0).getDisplayName().split("::"))[0] + "-" +
                        new SimpleDateFormat("yyyy.MM.dd'at'HH-mm-ss.SSS").format(new Date(System.currentTimeMillis())) + ".xml");
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
    private void validateAuditMessage(EventLog eventLog, AuditMessage am) {

        LOGGER.debug("validateAuditMessage(EventLog '{}', AuditMessage '{}', PC UserId: '{}')", eventLog.getEventType(),
                am.getEventIdentification().getEventActionCode(), eventLog.getPC_UserID());
        try {
            // Infer model according to NCP Side and EventCode
            NcpSide ncpSide = eventLog.getNcpSide();

            if (StringUtils.equals(eventLog.getEventType(), "EHDSI-CF")) {
                throw new UnsupportedOperationException("EventCode not supported.");
            }
            OpenNCPValidation.validateAuditMessage(convertAuditObjectToXML(am), eventLog.getEventType(), ncpSide);
        } catch (JAXBException e) {
            LOGGER.error("JAXBException: {}", e.getMessage(), e);
        }
    }
}
