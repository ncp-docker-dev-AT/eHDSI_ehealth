package epsos.ccd.gnomon.auditmanager;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * EventLog is a java object with the minimal input variables needed to construct an AuditMessage according to RFC3881.
 * Java Bean object for the Event Log object.
 * Getters and setters for all the properties Constructors for OpenNCP audit schemas.
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 */
public class EventLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLog.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    // NCPSide A or B information required for online validation
    private NcpSide ncpSide;
    private String EI_TransactionNumber;  // Number of the transaction including the 'Epsos-' prefix
    private String EI_TransactionName;    // Name of the transaction, specified in the use cases diagram
    private XMLGregorianCalendar EI_EventDateTime;
    private String EI_EventActionCode;    //  C:create, R:Read,View,Print,Query, U:Update, D:Delete, E:Execute
    private BigInteger EI_EventOutcomeIndicator; // Possible values are: 0:full success,1:partial delivery,4:temporal or recoverable failures,8:permanent failure
    // Event Identification
    private String EventType;             // one of the available epsos event ids

    // Point of Care
    private String PC_UserID;  // Point of Care: Oid of the department
    private String PC_RoleID;  // Point of Care: Role of the department

    // Human Requester
    private String HR_UserID;  // Identifier of the HCP initiated the event
    private String HR_AlternativeUserID; // Human readable name of the HCP as given in the Subject-ID
    private String HR_UserName;
    private String HR_RoleID;
    // Service Consumer NCP
    // The string encoded CN of the TLS certificate of the NCP triggered the epsos operation
    private String SC_UserID;
    // Service Provider
    // The string encoded CN of the TLS certificate of the NCP triggered the epsos operation
    private String SP_UserID;
    // Audit Source
    private String AS_AuditSourceId; // The authority that is legally responsible for the audit source
    // Patient Source
    private String PS_PatricipantObjectID; // Patient Code in HL7 format
    // Patient Target
    private String PT_PatricipantObjectID; // Mapped PatientCode in HL7 format
    // Error Message
    private String EM_PatricipantObjectID;  // String-encoded error code
    private byte[] EM_PatricipantObjectDetail;  // Base64 encoded error message
    // Mapping Service
    private String MS_UserID;  // The string encoded OID of the service instance performed the mapping

    // List of string encoded UID of the returned document
    private List<String> eventTargetParticipantObjectIds = new ArrayList<>();
    // The string encoded UID of the Additional returned document
    private String eventTargetAdditionalObjectId;
    // Non-Repudiation
    private String ReqM_ParticipantObjectID;  // ReqM_ParticipantObjectID String-encoded UUID of the request message
    private byte[] ReqM_PatricipantObjectDetail;  // Base64 encoded error message
    private String ResM_ParticipantObjectID;   // ReqM_ParticipantObjectID String-encoded UUID of the response message
    private byte[] ResM_PatricipantObjectDetail;  // Base64 encoded error message
    // IP Addresses
    private String sourceip;
    private String targetip;


    public EventLog() {
    }

    /**
     * This method creates an EventLog object for use in HCP Authentication
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the department
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param eventTargetObjectId          The string encoded UID of the returned document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceIp                     The IP Address of the source Gateway
     * @param targetIp                     The IP Address of the target Gateway
     */
    public static EventLog createEventLogHCPIdentity(TransactionName eventIdentificationTransactionName, EventActionCode eventIdentificationActionCode,
                                                     XMLGregorianCalendar eventIdentificationTime, EventOutcomeIndicator eventIdentificationOutcomeIndicator,
                                                     String PC_UserID, String PC_RoleID, String HR_UserID, String HR_RoleID,
                                                     String HR_AlternativeUserID, String SC_UserID, String SP_UserID,
                                                     String AS_AuditSourceId, String eventTargetObjectId, String ReqM_ParticipantObjectID,
                                                     byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                     byte[] ResM_PatricipantObjectDetail, String sourceIp, String targetIp,
                                                     NcpSide ncpSide) {

        LOGGER.info("Creating EventLog for HCP Identification: '{}'-'{}'",
                eventIdentificationTransactionName, eventIdentificationActionCode);
        EventLog eventLog = new EventLog();
        // Setup Event Identification information
        eventLog.setEI_TransactionName(eventIdentificationTransactionName);
        eventLog.setEI_EventActionCode(eventIdentificationActionCode);
        eventLog.setEI_EventDateTime(eventIdentificationTime);
        eventLog.setEI_EventOutcomeIndicator(eventIdentificationOutcomeIndicator);
        // Setup Event Identification information
        eventLog.setPC_UserID(nullToEmptyString(PC_UserID));
        eventLog.setPC_RoleID(nullToEmptyString(PC_RoleID));
        // Setup Event Identification information
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        // Setup Event Identification information
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setAS_AuditSourceId(nullToEmptyString(AS_AuditSourceId));
        // Setup Event Identification information
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectId));
        //  TODO: Audit - Event Target
        // Setup Event Identification information
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        // Setup Event Identification information
        eventLog.setSourceip(nullToEmptyString(sourceIp));
        eventLog.setTargetip(nullToEmptyString(targetIp));
        // Setup Event Identification information
        eventLog.setNcpSide(ncpSide);

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("EventLog Details: '{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * This method creates an EventLog object for use in NLS Import
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param eventTargetObjectId          The string encoded UUID of the returned document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     */
    public static EventLog createEventLogNCPTrustedServiceList(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                               XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                               String SC_UserID, String SP_UserID, String eventTargetObjectId,
                                                               String ReqM_ParticipantObjectID, byte[] ReqM_PatricipantObjectDetail,
                                                               String ResM_ParticipantObjectID, byte[] ResM_PatricipantObjectDetail,
                                                               String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for NCP Trust Service List: '{}'-'{}'",
                EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setAS_AuditSourceId("EP-00");
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        //  TODO: Audit - Event Target
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectId));
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceip));
        eventLog.setTargetip(nullToEmptyString(targetip));

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("'{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * This method creates an EventLog object for use in Pivot Translation
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param eventTargetObjectIdIn        The string encoded UUID of the source document
     * @param eventTargetObjectIdOut       The string encoded UUID of the target document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param targetip                     The IP Address of the target Gateway
     */
    public static EventLog createEventLogPivotTranslation(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                          XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                          String SP_UserID, String eventTargetObjectIdIn, String eventTargetObjectIdOut,
                                                          String ReqM_ParticipantObjectID, byte[] ReqM_PatricipantObjectDetail,
                                                          String ResM_ParticipantObjectID, byte[] ResM_PatricipantObjectDetail,
                                                          String targetip) {

        LOGGER.info("Creating EventLog for CDA Pivot Translation: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        // Set Audit Source
        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        eventLog.setAS_AuditSourceId(configurationManager.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION"));
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectIdIn));
        //  TODO: Audit - Event Target
        eventLog.setEventTargetAdditionalObjectId(nullToEmptyString(eventTargetObjectIdOut));
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        eventLog.setTargetip(nullToEmptyString(targetip));

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("'{}'", eventLog);
        }
        return eventLog;
    }

    public static EventLog createEventLogPatientPrivacy(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                        XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                        String HR_UserID, String HR_AlternativeUserID, String HR_RoleID,
                                                        String SC_UserID, String SP_UserID, String AS_AuditSourceId,
                                                        String PT_PatricipantObjectID, String EM_PatricipantObjectID,
                                                        byte[] EM_PatricipantObjectDetail, String eventTargetObjectId,
                                                        String ReqM_ParticipantObjectID, byte[] ReqM_PatricipantObjectDetail,
                                                        String ResM_ParticipantObjectID, byte[] ResM_PatricipantObjectDetail,
                                                        String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Patient Privacy: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        return EventLog.createEventLogHCPAssurance(EI_TransactionName, EI_EventActionCode, EI_EventDateTime,
                EI_EventOutcomeIndicator, null, null, HR_UserID, HR_AlternativeUserID, HR_RoleID,
                SC_UserID, SP_UserID, AS_AuditSourceId, PT_PatricipantObjectID, EM_PatricipantObjectID,
                EM_PatricipantObjectDetail, eventTargetObjectId, ReqM_ParticipantObjectID, ReqM_PatricipantObjectDetail,
                ResM_ParticipantObjectID, ResM_PatricipantObjectDetail, sourceip, targetip);
    }

    /**
     * This method creates an EventLog object for use in Consent PIN
     *
     * @param EI_TransactionName           value is epsosConsentServicePin
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the point of care
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param PT_PatricipantObjectID       Patient Identifier in HL7 II format
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogConsentPINack(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                       XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                       String PC_UserID, String PC_RoleID, String HR_UserID, String HR_AlternativeUserID,
                                                       String HR_RoleID, String SC_UserID, String SP_UserID, String AS_AuditSourceId,
                                                       String PT_PatricipantObjectID, String ReqM_ParticipantObjectID,
                                                       byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                       byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Consent PIN ACK: '{}'-'{}'",
                EI_TransactionName, EI_EventActionCode);
        return EventLog.createEventLogHCPAssurance(EI_TransactionName, EI_EventActionCode, EI_EventDateTime, EI_EventOutcomeIndicator,
                PC_UserID, PC_RoleID, HR_UserID, HR_AlternativeUserID, HR_RoleID, SC_UserID, SP_UserID, AS_AuditSourceId,
                PT_PatricipantObjectID, null, null, "PINack",
                ReqM_ParticipantObjectID, ReqM_PatricipantObjectDetail, ResM_ParticipantObjectID, ResM_PatricipantObjectDetail,
                sourceip, targetip);
    }

    /**
     * This method creates an EventLog object for use in Consent PIN
     *
     * @param EI_TransactionName           value is epsosConsentServicePin
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the point of care
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param PT_PatricipantObjectID       Patient Identifier in HL7 II format
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogConsentPINdny(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                       XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                       String PC_UserID, String PC_RoleID, String HR_UserID, String HR_AlternativeUserID,
                                                       String HR_RoleID, String SC_UserID, String SP_UserID, String AS_AuditSourceId,
                                                       String PT_PatricipantObjectID, String ReqM_ParticipantObjectID,
                                                       byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                       byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Consent PIN Deny: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        return EventLog.createEventLogHCPAssurance(EI_TransactionName, EI_EventActionCode, EI_EventDateTime, EI_EventOutcomeIndicator,
                PC_UserID, PC_RoleID, HR_UserID, HR_AlternativeUserID, HR_RoleID, SC_UserID, SP_UserID, AS_AuditSourceId,
                PT_PatricipantObjectID, null, null, "PINdny",
                ReqM_ParticipantObjectID, ReqM_PatricipantObjectDetail, ResM_ParticipantObjectID, ResM_PatricipantObjectDetail,
                sourceip, targetip);
    }

    /**
     * This method creates an EventLog object for use in HCP Assurance Schema
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the point of care
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param PT_PatricipantObjectID       Patient Identifier in HL7 II format
     * @param EM_PatricipantObjectID       The error code included with the response
     *                                     message
     * @param EM_PatricipantObjectDetail   Contains the base64 encoded error
     *                                     message
     * @param eventTargetObjectId          The string encoded UUID of the returned document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogHCPAssurance(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                      XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                      String PC_UserID, String PC_RoleID, String HR_UserID, String HR_AlternativeUserID,
                                                      String HR_RoleID, String SC_UserID, String SP_UserID, String AS_AuditSourceId,
                                                      String PT_PatricipantObjectID, String EM_PatricipantObjectID,
                                                      byte[] EM_PatricipantObjectDetail, String eventTargetObjectId,
                                                      String ReqM_ParticipantObjectID, byte[] ReqM_PatricipantObjectDetail,
                                                      String ResM_ParticipantObjectID, byte[] ResM_PatricipantObjectDetail,
                                                      String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Healthcare Provider Assurance: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setPC_UserID(nullToEmptyString(PC_UserID));
        eventLog.setPC_RoleID(nullToEmptyString(PC_RoleID));
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setAS_AuditSourceId(nullToEmptyString(AS_AuditSourceId));
        eventLog.setPT_PatricipantObjectID(nullToEmptyString(PT_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectID(nullToEmptyString(EM_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectDetail(EM_PatricipantObjectDetail);
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectId));
        //  TODO: Audit - Event Target
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceip));
        eventLog.setTargetip(nullToEmptyString(targetip));

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("'{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * @param EI_TransactionName
     * @param EI_EventActionCode
     * @param EI_EventDateTime
     * @param EI_EventOutcomeIndicator
     * @param PC_UserID
     * @param PC_RoleID
     * @param HR_UserID
     * @param HR_AlternativeUserID
     * @param HR_RoleID
     * @param SC_UserID
     * @param SP_UserID
     * @param AS_AuditSourceId
     * @param PT_PatricipantObjectID
     * @param EM_PatricipantObjectID
     * @param EM_PatricipantObjectDetail
     * @param eventTargetObjectId
     * @param ReqM_ParticipantObjectID
     * @param ReqM_PatricipantObjectDetail
     * @param ResM_ParticipantObjectID
     * @param ResM_PatricipantObjectDetail
     * @param sourceip
     * @param targetip
     * @return
     */
    public static EventLog createEventLogPAC(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                             XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                             String PC_UserID, String PC_RoleID, String HR_UserID, String HR_AlternativeUserID, String HR_RoleID,
                                             String SC_UserID, String SP_UserID, String AS_AuditSourceId, String PT_PatricipantObjectID,
                                             String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail,
                                             String eventTargetObjectId, String ReqM_ParticipantObjectID,
                                             byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                             byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Patient Access Control(PAC): '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        return EventLog.createEventLogHCPAssurance(EI_TransactionName, EI_EventActionCode, EI_EventDateTime, EI_EventOutcomeIndicator,
                PC_UserID, PC_RoleID, HR_UserID, HR_AlternativeUserID, HR_RoleID, SC_UserID, SP_UserID, AS_AuditSourceId,
                PT_PatricipantObjectID, EM_PatricipantObjectID, EM_PatricipantObjectDetail, eventTargetObjectId,
                ReqM_ParticipantObjectID, ReqM_PatricipantObjectDetail, ResM_ParticipantObjectID, ResM_PatricipantObjectDetail,
                sourceip, targetip);
    }

    /**
     * @param EI_TransactionName
     * @param EI_EventActionCode
     * @param EI_EventDateTime
     * @param EI_EventOutcomeIndicator
     * @param PC_UserID
     * @param PC_RoleID
     * @param HR_UserID
     * @param HR_AlternativeUserID
     * @param HR_RoleID
     * @param SC_UserID
     * @param SP_UserID
     * @param AS_AuditSourceId
     * @param PT_PatricipantObjectID
     * @param EM_PatricipantObjectID
     * @param EM_PatricipantObjectDetail
     * @param eventTargetObjectId
     * @param ReqM_ParticipantObjectID
     * @param ReqM_PatricipantObjectDetail
     * @param ResM_ParticipantObjectID
     * @param ResM_PatricipantObjectDetail
     * @param sourceip
     * @param targetip
     * @return
     */
    public static EventLog createEventLogPatientService(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                        XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                        String PC_UserID, String PC_RoleID, String HR_UserID,
                                                        String HR_AlternativeUserID, String HR_RoleID, String SC_UserID,
                                                        String SP_UserID, String AS_AuditSourceId, String PT_PatricipantObjectID,
                                                        String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail,
                                                        String eventTargetObjectId, String ReqM_ParticipantObjectID,
                                                        byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                        byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Patient Service: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        return EventLog.createEventLogHCPAssurance(EI_TransactionName, EI_EventActionCode, EI_EventDateTime, EI_EventOutcomeIndicator,
                PC_UserID, PC_RoleID, HR_UserID, HR_AlternativeUserID, HR_RoleID, SC_UserID, SP_UserID, AS_AuditSourceId,
                PT_PatricipantObjectID, EM_PatricipantObjectID, EM_PatricipantObjectDetail, eventTargetObjectId,
                ReqM_ParticipantObjectID, ReqM_PatricipantObjectDetail, ResM_ParticipantObjectID, ResM_PatricipantObjectDetail,
                sourceip, targetip);
    }

    /**
     * This method creates an EventLog object for use in Issuance of a Treatment Relationship Confirmation Assertion.
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the Point of Care: Oid of the department
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param PT_ParticipantObjectID       Patient Identifier in HL7 II format
     * @param eventTargetObjectId          The string encoded UUID of the returned document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_ParticipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_ParticipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceIp                     The IP Address of the source Gateway
     * @param targetIp                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogTRCA(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                              XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                              String PC_UserID, String PC_RoleID, String HR_UserID, String HR_RoleID,
                                              String HR_AlternativeUserID, String SC_UserID, String SP_UserID,
                                              String AS_AuditSourceId, String PT_ParticipantObjectID, String eventTargetObjectId,
                                              String ReqM_ParticipantObjectID, byte[] ReqM_ParticipantObjectDetail,
                                              String ResM_ParticipantObjectID, byte[] ResM_ParticipantObjectDetail,
                                              String sourceIp, String targetIp, NcpSide ncpSide) {

        LOGGER.info("Creating EventLog for TRC Assertions: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setPC_UserID(nullToEmptyString(PC_UserID));
        eventLog.setPC_RoleID(nullToEmptyString(PC_RoleID));
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setAS_AuditSourceId(nullToEmptyString(AS_AuditSourceId));
        eventLog.setPT_PatricipantObjectID(nullToEmptyString(PT_ParticipantObjectID));
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectId));
        //  TODO: Audit - Event Target
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_ParticipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_ParticipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceIp));
        eventLog.setTargetip(nullToEmptyString(targetIp));
        eventLog.ncpSide = ncpSide;

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("EventLog: '{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * This method creates an EventLog object for use in Issuance of a Next of Kin Assertion.
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occurred
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Role of the Point of Care: Oid of the department
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of HCP initiated the event
     * @param HR_AlternativeUserID         Human-readable name of the HCP as given in
     *                                     the Subject-ID
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param NOK_ParticipantObjectID      Next of Kin Identifier in HL7 II format
     * @param eventTargetObjectId          The string encoded UUID of the returned document
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_ParticipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_ParticipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceIp                     The IP Address of the source Gateway
     * @param targetIp                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogNOKA(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                              XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                              String PC_UserID, String PC_RoleID, String HR_UserID, String HR_RoleID,
                                              String HR_AlternativeUserID, String SC_UserID, String SP_UserID,
                                              String AS_AuditSourceId, String NOK_ParticipantObjectID, String eventTargetObjectId,
                                              String ReqM_ParticipantObjectID, byte[] ReqM_ParticipantObjectDetail,
                                              String ResM_ParticipantObjectID, byte[] ResM_ParticipantObjectDetail,
                                              String sourceIp, String targetIp, NcpSide ncpSide) {

        LOGGER.info("Creating EventLog for NOK Assertions: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setPC_UserID(nullToEmptyString(PC_UserID));
        eventLog.setPC_RoleID(nullToEmptyString(PC_RoleID));
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setAS_AuditSourceId(nullToEmptyString(AS_AuditSourceId));
        eventLog.setPT_PatricipantObjectID(nullToEmptyString(NOK_ParticipantObjectID));
        eventLog.getEventTargetParticipantObjectIds().add(nullToEmptyString(eventTargetObjectId));
        //  TODO: Audit - Event Target
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_ParticipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_ParticipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceIp));
        eventLog.setTargetip(nullToEmptyString(targetIp));
        eventLog.ncpSide = ncpSide;

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("EventLog: '{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * This method creates an EventLog object for use in Patient ID Mapping
     * Audit Schema
     *
     * @param EI_TransactionName
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID attrbute of the HCP identity assertion
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param AS_AuditSourceId             the iso3166-2 code of the country responsible for
     *                                     the audit source
     * @param PS_PatricipantObjectID       Patient Identifier in HL7 II format
     *                                     (Patient Source)
     * @param PT_PatricipantObjectID       Patient Identifier in HL7 II format
     *                                     (Patient Target)
     * @param EM_PatricipantObjectID       The error code included with the response
     *                                     message
     * @param EM_PatricipantObjectDetail   Contains the base64 encoded error
     *                                     message
     * @param MS_UserID                    The string encoded OID of the service instance performed
     *                                     the mapping
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogPatientMapping(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                        XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                        String HR_UserID, String HR_RoleID, String HR_AlternativeUserID,
                                                        String SC_UserID, String SP_UserID, String AS_AuditSourceId,
                                                        String PS_PatricipantObjectID, String PT_PatricipantObjectID,
                                                        String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail,
                                                        String MS_UserID, String ReqM_ParticipantObjectID,
                                                        byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                        byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for Patient ID Mapping: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setAS_AuditSourceId(nullToEmptyString(AS_AuditSourceId));
        eventLog.setPS_PatricipantObjectID(nullToEmptyString(PS_PatricipantObjectID));
        eventLog.setPT_PatricipantObjectID(nullToEmptyString(PT_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectID(nullToEmptyString(EM_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectDetail(EM_PatricipantObjectDetail);
        eventLog.setMS_UserID(nullToEmptyString(MS_UserID));
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceip));
        eventLog.setTargetip(nullToEmptyString(targetip));

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("'{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * This method creates an EventLog object for use in HCP Assurance Schema
     *
     * @param EI_EventActionCode           Possible values according to D4.5.6 are E,R,U,D
     * @param EI_EventDateTime             The datetime the event occured
     * @param EI_EventOutcomeIndicator     <br>
     *                                     0 for full success <br>
     *                                     1 in case of partial delivery <br>
     *                                     4 for temporal failures <br>
     *                                     8 for permanent failure <br>
     * @param PC_UserID                    Point of Care: Oid of the department
     * @param PC_RoleID                    Point of Care: Role of the department
     * @param HR_UserID                    Identifier of the HCP initiated the event
     * @param HR_RoleID                    Role of the HCP initiated the event
     * @param HR_AlternativeUserID         Human readable name of the HCP as given in
     *                                     the Subject-ID
     * @param SC_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     triggered the epsos operation
     * @param SP_UserID                    The string encoded CN of the TLS certificate of the NCP
     *                                     processed the epsos operation
     * @param PT_PatricipantObjectID       Patient Identifier in HL7 II format
     * @param EM_PatricipantObjectID       The error code included with the response
     *                                     message
     * @param EM_PatricipantObjectDetail   Contains the base64 encoded error
     *                                     message
     * @param ReqM_ParticipantObjectID     String-encoded UUID of the request
     *                                     message
     * @param ReqM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param ResM_ParticipantObjectID     String-encoded UUID of the response
     *                                     message
     * @param ResM_PatricipantObjectDetail The value MUST contain the base64
     *                                     encoded security header.
     * @param sourceip                     The IP Address of the source Gateway
     * @param targetip                     The IP Address of the target Gateway
     * @return the EventLog object
     */
    public static EventLog createEventLogCommunicationFailure(TransactionName EI_TransactionName, EventActionCode EI_EventActionCode,
                                                              XMLGregorianCalendar EI_EventDateTime, EventOutcomeIndicator EI_EventOutcomeIndicator,
                                                              String PC_UserID, String PC_RoleID, String HR_UserID,
                                                              String HR_RoleID, String HR_AlternativeUserID, String SC_UserID,
                                                              String SP_UserID, String PT_PatricipantObjectID, String EM_PatricipantObjectID,
                                                              byte[] EM_PatricipantObjectDetail, String ReqM_ParticipantObjectID,
                                                              byte[] ReqM_PatricipantObjectDetail, String ResM_ParticipantObjectID,
                                                              byte[] ResM_PatricipantObjectDetail, String sourceip, String targetip) {

        LOGGER.info("Creating EventLog for System Communication Failure: '{}'-'{}'", EI_TransactionName, EI_EventActionCode);
        EventLog eventLog = new EventLog();
        eventLog.setEI_TransactionName(EI_TransactionName);
        eventLog.setEI_EventActionCode(EI_EventActionCode);
        eventLog.setEI_EventDateTime(EI_EventDateTime);
        eventLog.setEI_EventOutcomeIndicator(EI_EventOutcomeIndicator);
        eventLog.setPC_UserID(nullToEmptyString(PC_UserID));
        eventLog.setPC_RoleID(nullToEmptyString(PC_RoleID));
        eventLog.setHR_UserID(nullToEmptyString(HR_UserID));
        eventLog.setHR_RoleID(nullToEmptyString(HR_RoleID));
        eventLog.setHR_AlternativeUserID(nullToEmptyString(HR_AlternativeUserID));
        eventLog.setSC_UserID(nullToEmptyString(SC_UserID));
        eventLog.setSP_UserID(nullToEmptyString(SP_UserID));
        eventLog.setPT_PatricipantObjectID(nullToEmptyString(PT_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectID(nullToEmptyString(EM_PatricipantObjectID));
        eventLog.setEM_PatricipantObjectDetail(EM_PatricipantObjectDetail);
        eventLog.setReqM_ParticipantObjectID(nullToEmptyString(ReqM_ParticipantObjectID));
        eventLog.setReqM_PatricipantObjectDetail(ReqM_PatricipantObjectDetail);
        eventLog.setResM_ParticipantObjectID(nullToEmptyString(ResM_ParticipantObjectID));
        eventLog.setResM_PatricipantObjectDetail(ResM_PatricipantObjectDetail);
        eventLog.setSourceip(nullToEmptyString(sourceip));
        eventLog.setTargetip(nullToEmptyString(targetip));

        if (LOGGER_CLINICAL.isDebugEnabled() && !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
            LOGGER_CLINICAL.debug("'{}'", eventLog);
        }
        return eventLog;
    }

    /**
     * @param str represents a string
     * @return empty string if the param is null. Otherwise returns the string as is
     */
    private static String nullToEmptyString(String str) {

        return str == null ? "" : str;
    }

    public String getHR_RoleID() {
        return HR_RoleID;
    }

    public void setHR_RoleID(String HR_RoleID) {
        this.HR_RoleID = HR_RoleID;
    }

    public String getEventTargetAdditionalObjectId() {
        return eventTargetAdditionalObjectId;
    }

    public void setEventTargetAdditionalObjectId(String eventTargetAdditionalObjectId) {
        this.eventTargetAdditionalObjectId = eventTargetAdditionalObjectId;
    }

    public String getPC_RoleID() {
        return PC_RoleID;
    }

    public void setPC_RoleID(String PC_RoleID) {
        this.PC_RoleID = PC_RoleID;
    }

    public String getSourceip() {
        return sourceip;
    }

    public void setSourceip(String sourceip) {
        this.sourceip = sourceip;
    }

    public String getTargetip() {
        return targetip;
    }

    public void setTargetip(String targetip) {
        this.targetip = targetip;
    }

    public String getReqM_ParticipantObjectID() {
        return ReqM_ParticipantObjectID;
    }

    public void setReqM_ParticipantObjectID(String ReqM_ParticipantObjectID) {
        this.ReqM_ParticipantObjectID = ReqM_ParticipantObjectID;
    }

    public byte[] getReqM_PatricipantObjectDetail() {
        return ReqM_PatricipantObjectDetail;
    }

    public void setReqM_PatricipantObjectDetail(byte[] ReqM_PatricipantObjectDetail) {
        this.ReqM_PatricipantObjectDetail = ReqM_PatricipantObjectDetail;
    }

    public String getResM_ParticipantObjectID() {
        return ResM_ParticipantObjectID;
    }

    public void setResM_ParticipantObjectID(String ResM_ParticipantObjectID) {
        this.ResM_ParticipantObjectID = ResM_ParticipantObjectID;
    }

    public byte[] getResM_PatricipantObjectDetail() {
        return ResM_PatricipantObjectDetail;
    }

    public void setResM_PatricipantObjectDetail(byte[] ResM_PatricipantObjectDetail) {
        this.ResM_PatricipantObjectDetail = ResM_PatricipantObjectDetail;
    }

    public List<String> getEventTargetParticipantObjectIds() {
        return eventTargetParticipantObjectIds;
    }

    public void setEventTargetParticipantObjectIds(List<String> eventTargetParticipantObjectIds) {
        this.eventTargetParticipantObjectIds = eventTargetParticipantObjectIds;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(EventType EventType) {
        this.EventType = EventType.getCode();
    }

    public byte[] getEM_PatricipantObjectDetail() {
        return EM_PatricipantObjectDetail;
    }

    public void setEM_PatricipantObjectDetail(byte[] EM_PatricipantObjectDetail) {
        this.EM_PatricipantObjectDetail = EM_PatricipantObjectDetail;
    }

    public String getAS_AuditSourceId() {
        return AS_AuditSourceId;
    }

    public void setAS_AuditSourceId(String AS_AuditSourceId) {
        this.AS_AuditSourceId = AS_AuditSourceId;
    }

    public String getEI_EventActionCode() {
        return EI_EventActionCode;
    }

    public void setEI_EventActionCode(EventActionCode EI_EventActionCode) {
        this.EI_EventActionCode = EI_EventActionCode.getCode();
    }

    public XMLGregorianCalendar getEI_EventDateTime() {
        return EI_EventDateTime;
    }

    public void setEI_EventDateTime(XMLGregorianCalendar EI_EventDateTime) {
        this.EI_EventDateTime = EI_EventDateTime;
    }

    public BigInteger getEI_EventOutcomeIndicator() {
        return EI_EventOutcomeIndicator;
    }

    public void setEI_EventOutcomeIndicator(EventOutcomeIndicator EI_EventOutcomeIndicator) {
        this.EI_EventOutcomeIndicator = EI_EventOutcomeIndicator.getCode();
    }

    public String getEI_TransactionName() {
        return EI_TransactionName;
    }

    public void setEI_TransactionName(TransactionName EI_TransactionName) {
        this.EI_TransactionName = EI_TransactionName.getCode();
    }

    public String getEI_TransactionNumber() {
        return EI_TransactionNumber;
    }

    public void setEI_TransactionNumber(String EI_TransactionNumber) {
        this.EI_TransactionNumber = EI_TransactionNumber;
    }

    public String getEM_PatricipantObjectID() {
        return EM_PatricipantObjectID;
    }

    public void setEM_PatricipantObjectID(String EM_PatricipantObjectID) {
        this.EM_PatricipantObjectID = EM_PatricipantObjectID;
    }

    public String getHR_AlternativeUserID() {
        return HR_AlternativeUserID;
    }

    public void setHR_AlternativeUserID(String HR_AlternativeUserID) {
        this.HR_AlternativeUserID = HR_AlternativeUserID;
    }

    public String getHR_UserID() {
        return HR_UserID;
    }

    public void setHR_UserID(String HR_UserID) {
        this.HR_UserID = HR_UserID;
    }

    public String getHR_UserName() {
        return HR_UserName;
    }

    public void setHR_UserName(String HR_UserName) {
        this.HR_UserName = HR_UserName;
    }

    public String getMS_UserID() {
        return MS_UserID;
    }

    public void setMS_UserID(String MS_UserID) {
        this.MS_UserID = MS_UserID;
    }

    public String getPC_UserID() {
        return PC_UserID;
    }

    public void setPC_UserID(String PC_UserID) {
        this.PC_UserID = PC_UserID;
    }

    public String getPS_PatricipantObjectID() {
        return PS_PatricipantObjectID;
    }

    public void setPS_PatricipantObjectID(String PS_PatricipantObjectID) {
        this.PS_PatricipantObjectID = PS_PatricipantObjectID;
    }

    public String getPT_PatricipantObjectID() {
        return PT_PatricipantObjectID;
    }

    public void setPT_PatricipantObjectID(String PT_PatricipantObjectID) {
        this.PT_PatricipantObjectID = PT_PatricipantObjectID;
    }

    public String getSC_UserID() {
        return SC_UserID;
    }

    public void setSC_UserID(String SC_UserID) {
        this.SC_UserID = SC_UserID;
    }

    public String getSP_UserID() {
        return SP_UserID;
    }

    public void setSP_UserID(String SP_UserID) {
        this.SP_UserID = SP_UserID;
    }

    public NcpSide getNcpSide() {
        return ncpSide;
    }

    public void setNcpSide(NcpSide ncpSide) {
        this.ncpSide = ncpSide;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "EI_TransactionNumber='" + EI_TransactionNumber + '\'' +
                ", EI_TransactionName='" + EI_TransactionName + '\'' +
                ", EI_EventDateTime=" + EI_EventDateTime +
                ", EI_EventActionCode='" + EI_EventActionCode + '\'' +
                ", EI_EventOutcomeIndicator=" + EI_EventOutcomeIndicator +
                ", SC_UserID='" + SC_UserID + '\'' +
                ", SP_UserID='" + SP_UserID + '\'' +
                ", ReqM_ParticipantObjectID='" + ReqM_ParticipantObjectID + '\'' +
                ", ResM_ParticipantObjectID='" + ResM_ParticipantObjectID + '\'' +
                ", SourceIP='" + sourceip + '\'' +
                ", TargetIP='" + targetip + '\'' +
                ", EventType='" + EventType + '\'' +
                '}';
    }
}
