package tr.com.srdc.epsos.ws.server.xcpd.impl;

import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.protocolterminators.ws.server.xcpd.PatientSearchInterface;
import eu.epsos.protocolterminators.ws.server.xcpd.PatientSearchInterfaceWithDemographics;
import eu.epsos.protocolterminators.ws.server.xcpd.XCPDServiceInterface;
import eu.epsos.util.EvidenceUtils;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.Helper;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.XSDValidationException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML2Validator;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hl7.v3.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.DateUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class XCPDServiceImpl implements XCPDServiceInterface {

    private static final String ERROR_DEMOGRAPHIC_QUERY_NOT_ALLOWED = "DemographicsQueryNotAllowed";
    private static final String ERROR_ANSWER_NOT_AVAILABLE = "AnswerNotAvailable";
    private static final String ERROR_INSUFFICIENT_RIGHTS = "InsufficientRights";
    private final Logger logger = LoggerFactory.getLogger(XCPDServiceImpl.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");
    private final ObjectFactory objectFactory;
    private final PatientSearchInterface patientSearchService;

    public XCPDServiceImpl() {

        objectFactory = new ObjectFactory();
        ServiceLoader<PatientSearchInterface> serviceLoader = ServiceLoader.load(PatientSearchInterface.class);
        try {
            logger.info("Loading National implementation of PatientSearchInterface...");
            patientSearchService = serviceLoader.iterator().next();
            logger.info("Successfully loaded PatientSearchService");
        } catch (Exception e) {
            logger.error("Failed to load implementation of PatientSearchService: " + e.getMessage(), e);
            throw e;
        }
    }

    private String getParticipantObjectID(II id) {
        return id.getExtension() + "^^^&" + id.getRoot() + "&ISO";
    }

    public void prepareEventLog(EventLog eventLog, PRPAIN201305UV02 inputMessage, PRPAIN201306UV02 outputMessage, Element sh) {

        logger.info("prepareEventLog('{}')", eventLog.getEventType());
        eventLog.setEventType(EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS);
        eventLog.setEI_TransactionName(TransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS);
        eventLog.setEI_EventActionCode(EventActionCode.EXECUTE);
        try {
            eventLog.setEI_EventDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            logger.error("DatatypeConfigurationException: {}", e.getMessage(), e);
        }
        String userIdAlias = Helper.getAssertionsSPProvidedId(sh);
        eventLog.setHR_UserID(StringUtils.isNotBlank(userIdAlias) ? userIdAlias : "" + "<" + Helper.getUserID(sh)
                + "@" + Helper.getAssertionsIssuer(sh) + ">");
        eventLog.setHR_AlternativeUserID(Helper.getAlternateUserID(sh));
        eventLog.setHR_RoleID(Helper.getFunctionalRoleID(sh));
        // Add point of care to the event log for assertion purposes
        eventLog.setPC_UserID(Helper.getPointOfCareUserId(sh));
        eventLog.setPC_RoleID(Helper.getPC_RoleID(sh));
        eventLog.setSP_UserID(HTTPUtil.getSubjectDN(true));

        II source_ii;
        II target_ii;
        if (!inputMessage.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().isEmpty()) {
            source_ii = inputMessage.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getValue().get(0);
            target_ii = outputMessage.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getValue().get(0);
        } else {
            source_ii = new II();
            target_ii = new II();
        }
        eventLog.setPT_PatricipantObjectID(getParticipantObjectID(target_ii));
        // Check if patient id mapping has occurred, prepare event log for patient audit mapping in this case
        if (!source_ii.getRoot().equals(target_ii.getRoot()) || !source_ii.getExtension().equals(target_ii.getExtension())) {
            eventLog.setPS_PatricipantObjectID(getParticipantObjectID(source_ii));
        }
        eventLog.setAS_AuditSourceId(Constants.COUNTRY_PRINCIPAL_SUBDIVISION);
        if (!outputMessage.getAcknowledgement().get(0).getAcknowledgementDetail().isEmpty()) {
            String detail = outputMessage.getAcknowledgement().get(0).getAcknowledgementDetail().get(0).getText().getContent();
            if (detail.startsWith("(")) {
                String code = detail.substring(1, 5);
                eventLog.setEM_PatricipantObjectID(code);
                if (StringUtils.equals(code, "1102")) {
                    eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.TEMPORAL_FAILURE);
                } else {
                    eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
                }
            } else {
                eventLog.setEM_PatricipantObjectID("0");
                eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.PERMANENT_FAILURE);
            }
            eventLog.setEM_PatricipantObjectDetail(detail.getBytes());
        } else {
            eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        }
    }

    /**
     * @param request
     * @param soapHeader
     * @param eventLog
     * @return
     * @throws Exception
     */
    public PRPAIN201306UV02 queryPatient(PRPAIN201305UV02 request, SOAPHeader soapHeader, EventLog eventLog) throws Exception {

        PRPAIN201306UV02 response = objectFactory.createPRPAIN201306UV02();
        pRPAIN201306UV02Builder(request, response, soapHeader, eventLog);
        return response;
    }

    private PRPAIN201306UV02MFMIMT700711UV01Subject1 getSubjectByPatientDemographic(PatientDemographics pd) {

        PRPAIN201306UV02MFMIMT700711UV01Subject1 response = objectFactory.createPRPAIN201306UV02MFMIMT700711UV01Subject1();
        response.getTypeCode().add("SUBJ");

        // Set registrationEvent
        response.setRegistrationEvent(objectFactory.createPRPAIN201306UV02MFMIMT700711UV01RegistrationEvent());
        response.getRegistrationEvent().getClassCode().add("REG");
        response.getRegistrationEvent().getMoodCode().add("EVN");

        // Set registrationEvent/id
        response.getRegistrationEvent().getId().add(objectFactory.createII());
        response.getRegistrationEvent().getId().get(0).getNullFlavor().add("NA");

        // Set registrationEvent/statusCode
        response.getRegistrationEvent().setStatusCode(objectFactory.createCS());
        response.getRegistrationEvent().getStatusCode().setCode("active");

        // Create registrationEvent/Subject
        response.getRegistrationEvent().setSubject1(objectFactory.createPRPAIN201306UV02MFMIMT700711UV01Subject2());
        response.getRegistrationEvent().getSubject1().setTypeCode(ParticipationTargetSubject.SBJ);

        // Create registrationEvent/Subject/Patient
        response.getRegistrationEvent().getSubject1().setPatient(objectFactory.createPRPAMT201310UV02Patient());
        response.getRegistrationEvent().getSubject1().getPatient().getClassCode().add("PAT");

        // Set registrationEvent/Subject/Patient/id
        response.getRegistrationEvent().getSubject1().getPatient().getId().add(objectFactory.createII());
        response.getRegistrationEvent().getSubject1().getPatient().getId().get(0).setRoot(pd.getIdList().get(0).getRoot());
        response.getRegistrationEvent().getSubject1().getPatient().getId().get(0).setExtension(pd.getIdList().get(0).getExtension());

        // Set registrationEvent/Subject/Patient/statusCode
        response.getRegistrationEvent().getSubject1().getPatient().setStatusCode(objectFactory.createCS());
        response.getRegistrationEvent().getSubject1().getPatient().getStatusCode().setCode("active");

        // Set registrationEvent/Subject/Patient/patientPerson
        PRPAMT201310UV02Person person = objectFactory.createPRPAMT201310UV02Person();
        person.getClassCode().add("PSN");
        person.setDeterminerCode("INSTANCE");
        person.setAdministrativeGenderCode(getAdministrativeCode(pd));
        person.setBirthTime(getBirthTime(pd));
        person.getName().add(getName(pd));
        person.getAddr().add(getAddress(pd));
        response.getRegistrationEvent().getSubject1().getPatient().setPatientPerson(objectFactory.createPRPAMT201310UV02PatientPatientPerson(person));

        // Set registrationEvent/Subject/Patient/subjectOf1
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().add(objectFactory.createPRPAMT201310UV02Subject());

        // Set registrationEvent/Subject/Patient/subjectOf1/queryMatchObservation
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).setQueryMatchObservation(objectFactory.createPRPAMT201310UV02QueryMatchObservation());
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().add("OBS");
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getMoodCode().add("EVN");

        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().setCode(objectFactory.createCD());
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getCode().setCode("IHE_PDQ");
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getCode().setCodeSystem("2.16.840.1.113883.1.11.19914");

        INT matchInt = objectFactory.createINT();
        matchInt.setValue(BigInteger.valueOf(100));
        response.getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().setValue(matchInt);

        // Set registrationEvent/custodian
        response.getRegistrationEvent().setCustodian(objectFactory.createMFMIMT700711UV01Custodian());
        response.getRegistrationEvent().getCustodian().getTypeCode().add("CST");

        // Set registrationEvent/custodian/assignedEntity
        response.getRegistrationEvent().getCustodian().setAssignedEntity(objectFactory.createCOCTMT090003UV01AssignedEntity());
        response.getRegistrationEvent().getCustodian().getAssignedEntity().setClassCode("ASSIGNED");

        // Set registrationEvent/custodian/assignedEntity/id
        response.getRegistrationEvent().getCustodian().getAssignedEntity().getId().add(objectFactory.createII());
        response.getRegistrationEvent().getCustodian().getAssignedEntity().getId().get(0).setRoot(Constants.HOME_COMM_ID);

        // Set registrationEvent/custodian/assignedEntity/code
        response.getRegistrationEvent().getCustodian().getAssignedEntity().setCode(objectFactory.createCE());
        response.getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCode("NotHealthDataLocator");
        response.getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCodeSystem("1.3.6.1.4.1.19376.1.2.27.2");

        return response;
    }

    private CE getAdministrativeCode(PatientDemographics pd) {
        CE result = objectFactory.createCE();
        result.setCode(pd.getAdministrativeGender().toString());
        return result;
    }

    private TS getBirthTime(PatientDemographics pd) {
        TS result = objectFactory.createTS();
        Date date = pd.getBirthDate();
        SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
        result.setValue(dateformatYYYYMMDD.format(date));
        return result;
    }

    private PN getName(PatientDemographics pd) {
        PN result = objectFactory.createPN();
        EnFamily enFamily = objectFactory.createEnFamily();
        enFamily.setContent(pd.getFamilyName());
        result.getContent().add(objectFactory.createENFamily(enFamily));

        EnGiven enGiven = objectFactory.createEnGiven();
        enGiven.setContent(pd.getGivenName());
        result.getContent().add(objectFactory.createENGiven(enGiven));
        return result;
    }

    private AD getAddress(PatientDemographics pd) {
        // Adding the city
        AD result = objectFactory.createAD();
        AdxpCity city = objectFactory.createAdxpCity();
        city.setContent(pd.getCity());
        result.getContent().add(objectFactory.createADCity(city));

        // Adding the postal code
        AdxpPostalCode postal = objectFactory.createAdxpPostalCode();
        postal.setContent(pd.getPostalCode());
        result.getContent().add(objectFactory.createADPostalCode(postal));

        // Adding the address street line
        AdxpStreetAddressLine street = objectFactory.createAdxpStreetAddressLine();
        street.setContent(pd.getStreetAddress());
        result.getContent().add(objectFactory.createADStreetAddressLine(street));

        // Adding the country
        AdxpCountry country = objectFactory.createAdxpCountry();
        country.setContent(pd.getCountry());
        result.getContent().add(objectFactory.createADCountry(country));

        return result;
    }

    /**
     * Prepares a reasonOf element according to error type
     *
     * @param errorType
     * @return
     */
    private MFMIMT700711UV01Reason getReasonOfElement(String errorType) {
        MFMIMT700711UV01Reason result = objectFactory.createMFMIMT700711UV01Reason();
        result.setTypeCode("RSON");

        // Set detectedIssueEvent
        result.setDetectedIssueEvent(objectFactory.createMCAIMT900001UV01DetectedIssueEvent());
        result.getDetectedIssueEvent().getClassCode().add("ALRT");
        result.getDetectedIssueEvent().getMoodCode().add("EVN");

        // Set detectedIssueEvent/code
        result.getDetectedIssueEvent().setCode(objectFactory.createCD());
        result.getDetectedIssueEvent().getCode().setCode("ActAdministrativeDetectedIssueCode");
        result.getDetectedIssueEvent().getCode().setCodeSystem("2.16.840.1.113883.5.4");

        if (errorType.equals(ERROR_DEMOGRAPHIC_QUERY_NOT_ALLOWED)) {
            // Set detectedIssueEvent/triggerFor
            result.getDetectedIssueEvent().getTriggerFor().add(objectFactory.createMCAIMT900001UV01Requires());
            result.getDetectedIssueEvent().getTriggerFor().get(0).getTypeCode().add("TRIG");

            // Set detectedIssueEvent/triggerFor/actOrRequired
            result.getDetectedIssueEvent().getTriggerFor().get(0).setActOrderRequired(objectFactory.createMCAIMT900001UV01ActOrderRequired());
            result.getDetectedIssueEvent().getTriggerFor().get(0).getActOrderRequired().getClassCode().add("ACT");
            result.getDetectedIssueEvent().getTriggerFor().get(0).getActOrderRequired().getMoodCode().add("RQO");

            // Set detectedIssueEvent/triggerFor/actOrRequired/code
            result.getDetectedIssueEvent().getTriggerFor().get(0).getActOrderRequired().setCode(objectFactory.createCE());
            result.getDetectedIssueEvent().getTriggerFor().get(0).getActOrderRequired().getCode().setCode(ERROR_DEMOGRAPHIC_QUERY_NOT_ALLOWED);
            result.getDetectedIssueEvent().getTriggerFor().get(0).getActOrderRequired().getCode().setCodeSystem("1.3.6.1.4.1.12559.11.10.1.3.2.2.1");
        } else if (errorType.equals(ERROR_ANSWER_NOT_AVAILABLE)) {
            // Set detectedIssueEvent/mitigatedBy
            result.getDetectedIssueEvent().getMitigatedBy().add(objectFactory.createMCAIMT900001UV01SourceOf());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).setTypeCode(ActRelationshipMitigates.MITGT);

            // Set detectedIssueEvent/mitigatedBy/detectedIssueManagement
            result.getDetectedIssueEvent().getMitigatedBy().get(0).setDetectedIssueManagement(objectFactory.createMCAIMT900001UV01DetectedIssueManagement());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getClassCode().add("ACT");
            // TODO Could not set moodCode to RQO, set EVN instead
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().setMoodCode(XActMoodDefEvn.EVN);

            // Set detectedIssueEvent/mitigatedBy/detectedIssueManagement/code
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().setCode(objectFactory.createCD());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getCode().setCode(ERROR_ANSWER_NOT_AVAILABLE);
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getCode().setCodeSystem("1.3.6.1.4.1.19376.1.2.27.3");
        } else if (errorType.equals(ERROR_INSUFFICIENT_RIGHTS)) {
            // Set detectedIssueEvent/mitigatedBy
            result.getDetectedIssueEvent().getMitigatedBy().add(objectFactory.createMCAIMT900001UV01SourceOf());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).setTypeCode(ActRelationshipMitigates.MITGT);

            // Set detectedIssueEvent/mitigatedBy/detectedIssueManagement
            result.getDetectedIssueEvent().getMitigatedBy().get(0).setDetectedIssueManagement(objectFactory.createMCAIMT900001UV01DetectedIssueManagement());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getClassCode().add("ACT");
            // TODO Could not set moodCode to RQO, set EVN instead
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().setMoodCode(XActMoodDefEvn.EVN);

            // Set detectedIssueEvent/mitigatedBy/detectedIssueManagement/code
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().setCode(objectFactory.createCD());
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getCode().setCode(ERROR_INSUFFICIENT_RIGHTS);
            result.getDetectedIssueEvent().getMitigatedBy().get(0).getDetectedIssueManagement().getCode().setCodeSystem("1.3.6.1.4.1.12559.11.10.1.3.2.2.1");
        }
        return result;
    }

    private void fillOutputMessage(PRPAIN201306UV02 outputMessage, String detail, String reason) {
        fillOutputMessage(outputMessage, detail, reason, "AE");
    }

    private void fillOutputMessage(PRPAIN201306UV02 outputMessage, String detail, String reason, String errorCode) {
        if (reason != null) {
            outputMessage.getControlActProcess().getReasonOf().add(getReasonOfElement(reason));
        }

        // Set queryAck/queryResponseCode
        outputMessage.getControlActProcess().getQueryAck().setQueryResponseCode(objectFactory.createCS());
        outputMessage.getControlActProcess().getQueryAck().getQueryResponseCode().setCode(errorCode);
        if (detail != null) {
            logger.error(detail);
            // Set acknowledgement/acknowledgementDetail
            outputMessage.getAcknowledgement().get(0).getTypeCode().setCode("AE");
            outputMessage.getAcknowledgement().get(0).getAcknowledgementDetail().add(objectFactory.createMCCIMT000300UV01AcknowledgementDetail());
            outputMessage.getAcknowledgement().get(0).getAcknowledgementDetail().get(0).setText(objectFactory.createED());
            outputMessage.getAcknowledgement().get(0).getAcknowledgementDetail().get(0).getText().setContent(detail);
        } else {
            logger.info("XCPD Request is valid.");
        }
    }

    /**
     * @param inputMessage
     * @return
     */
    private PatientDemographics parsePRPAIN201305UV02toPatientDemographics(PRPAIN201305UV02 inputMessage) {

        PatientDemographics pd = new PatientDemographics();
        PRPAIN201305UV02QUQIMT021001UV01ControlActProcess cap = inputMessage.getControlActProcess();

        if (cap != null) {
            PRPAMT201306UV02QueryByParameter qbp = cap.getQueryByParameter().getValue();
            if (qbp != null) {
                PRPAMT201306UV02ParameterList pl = qbp.getParameterList();

                // Administrative gender
                try {
                    List<PRPAMT201306UV02LivingSubjectAdministrativeGender> genders = pl.getLivingSubjectAdministrativeGender();
                    if (genders != null && !genders.isEmpty()) {
                        PRPAMT201306UV02LivingSubjectAdministrativeGender gender = genders.get(0);
                        pd.setAdministrativeGender(PatientDemographics.Gender.parseGender(gender.getValue().get(0).getCode()));
                    }
                } catch (Exception e) {
                    logger.warn("Unable to parse administrative gender", e);
                }

                // BirthDate
                try {
                    List<PRPAMT201306UV02LivingSubjectBirthTime> bds = pl.getLivingSubjectBirthTime();
                    if (bds != null && !bds.isEmpty()) {
                        PRPAMT201306UV02LivingSubjectBirthTime bd = bds.get(0);
                        String sbd = bd.getValue().get(0).getValue();
                        pd.setBirthDate(DateUtil.parseDateFromString(sbd, "yyyyMMdd"));
                    }
                } catch (Exception e) {
                    logger.warn("Unable to parse birthDate", e);
                }

                // City, street name, country, postal code
                try {
                    List<PRPAMT201306UV02PatientAddress> pas = pl.getPatientAddress();
                    if (pas != null && !pas.isEmpty()) {
                        PRPAMT201306UV02PatientAddress pa = pas.get(0);
                        List<Serializable> content = pa.getValue().get(0).getContent();
                        for (Serializable s : content) {
                            if (s instanceof JAXBElement) {
                                JAXBElement element = (JAXBElement) s;
                                String eName = element.getName().getLocalPart();
                                if (StringUtils.equals("city", eName)) {
                                    AdxpCity ac = (AdxpCity) element.getValue();
                                    pd.setCity(ac.getContent());
                                } else if (StringUtils.equals("streetName", eName)) {
                                    AdxpStreetName asn = (AdxpStreetName) element.getValue();
                                    pd.setStreetAddress(asn.getContent());
                                } else if (StringUtils.equals("country", eName)) {
                                    AdxpCountry ac = (AdxpCountry) element.getValue();
                                    pd.setCountry(ac.getContent());
                                } else if (StringUtils.equals("postalCode", eName)) {
                                    AdxpPostalCode apc = (AdxpPostalCode) element.getValue();
                                    pd.setPostalCode(apc.getContent());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to parse city, street name, country or postal code", e);
                }

                // Given name, family name
                try {
                    List<PRPAMT201306UV02LivingSubjectName> sns = pl.getLivingSubjectName();
                    if (sns != null && !sns.isEmpty()) {
                        PRPAMT201306UV02LivingSubjectName sn = sns.get(0);
                        List<Serializable> content = sn.getValue().get(0).getContent();
                        for (Serializable s : content) {
                            if (s instanceof JAXBElement) {
                                JAXBElement element = (JAXBElement) s;
                                String eName = element.getName().getLocalPart();
                                if (StringUtils.equals("given", eName)) {
                                    EnGiven eg = (EnGiven) element.getValue();
                                    pd.setGivenName(eg.getContent());
                                } else if (StringUtils.equals("family", eName)) {
                                    EnFamily ef = (EnFamily) element.getValue();
                                    pd.setFamilyName(ef.getContent());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to parse given name or family name", e);
                }

                // Id
                try {
                    List<PRPAMT201306UV02LivingSubjectId> ids = pl.getLivingSubjectId();
                    if (ids != null && !ids.isEmpty()) {
                        PRPAMT201306UV02LivingSubjectId id = ids.get(0);
                        pd.setId(id.getValue().get(0).getExtension());
                    }
                } catch (Exception e) {
                    logger.warn("Unable to parse patient id", e);
                }

                //TODO Email
                //TODO Telephone
            }
        }

        return pd;
    }

    /**
     * @param inputMessage
     * @param outputMessage
     * @param sh
     * @param eventLog
     * @throws Exception
     */
    private void pRPAIN201306UV02Builder(PRPAIN201305UV02 inputMessage, PRPAIN201306UV02 outputMessage, SOAPHeader sh, EventLog eventLog) throws Exception {

        String sigCountryCode;

        if (patientSearchService instanceof PatientSearchInterfaceWithDemographics) {
            PatientSearchInterfaceWithDemographics psiwd = (PatientSearchInterfaceWithDemographics) patientSearchService;
            PatientDemographics pd = parsePRPAIN201305UV02toPatientDemographics(inputMessage);
            psiwd.setPatientDemographics(pd);
        }

        // Set id of the message
        outputMessage.setId(objectFactory.createII());
        outputMessage.getId().setRoot(UUID.randomUUID().toString());

        // Generate and Set random extension
        Random generator = new Random();
        StringBuilder extension = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int d = generator.nextInt(10);
            extension.append(d);
        }
        outputMessage.getId().setExtension(extension.toString());

        // Set creation time
        outputMessage.setCreationTime(objectFactory.createTS());
        outputMessage.getCreationTime().setValue(DateUtil.getCurrentTimeUTC());

        // Set ITSVersion element
        outputMessage.setITSVersion("XML_1.0");

        // Set version element
        outputMessage.setVersionCode(inputMessage.getVersionCode());

        // Set interaction id
        outputMessage.setInteractionId(objectFactory.createII());

        outputMessage.getInteractionId().setRoot(inputMessage.getInteractionId().getRoot());
        outputMessage.getInteractionId().setExtension("PRPA_IN201306UV02");

        // Set Processing code
        outputMessage.setProcessingCode(objectFactory.createCS());
        outputMessage.getProcessingCode().setCode("P");

        // Set Processing mode code
        outputMessage.setProcessingModeCode(objectFactory.createCS());
        outputMessage.getProcessingModeCode().setCode("T");

        // Set Accept act code
        outputMessage.setAcceptAckCode(objectFactory.createCS());
        outputMessage.getAcceptAckCode().setCode("NE");

        // Create acknowledgement
        outputMessage.getAcknowledgement().add(objectFactory.createMCCIMT000300UV01Acknowledgement());

        // Create acknowledgement/targetMessage
        outputMessage.getAcknowledgement().get(0).setTargetMessage(objectFactory.createMCCIMT000300UV01TargetMessage());

        // Set acknowledgement/targetMessage/id
        outputMessage.getAcknowledgement().get(0).getTargetMessage().setId(inputMessage.getId());

        // Set acknowledgement/typeCode
        outputMessage.getAcknowledgement().get(0).setTypeCode(objectFactory.createCS());
        outputMessage.getAcknowledgement().get(0).getTypeCode().setCode("AA");

        PRPAMT201306UV02QueryByParameter inputQBP = inputMessage.getControlActProcess().getQueryByParameter().getValue();

        // Create controlActProcess
        outputMessage.setControlActProcess(objectFactory.createPRPAIN201306UV02MFMIMT700711UV01ControlActProcess());
        outputMessage.getControlActProcess().setClassCode(ActClassControlAct.CACT);
        outputMessage.getControlActProcess().setMoodCode(XActMoodIntentEvent.EVN);

        // Create controlActProcess/code
        outputMessage.getControlActProcess().setCode(objectFactory.createCD());
        outputMessage.getControlActProcess().getCode().setCode("PRPA_TE201306UV02");

        // Create controlActProcess/queryAck
        outputMessage.getControlActProcess().setQueryAck(objectFactory.createMFMIMT700711UV01QueryAck());

        // Set controlActProcess/queryAck/queryId
        outputMessage.getControlActProcess().getQueryAck().setQueryId(objectFactory.createII());
        outputMessage.getControlActProcess().getQueryAck().getQueryId().setRoot(inputQBP.getQueryId().getRoot());
        outputMessage.getControlActProcess().getQueryAck().getQueryId().setExtension(inputQBP.getQueryId().getExtension());

        Element shElement;
        try {
            shElement = XMLUtils.toDOM(sh);
        } catch (Exception e) {
            logger.error("SOAP header jaxb to dom failed.", e);
            throw e;
        }
        patientSearchService.setSOAPHeader(shElement);

        try {
            sigCountryCode = SAML2Validator.validateXCPDHeader(shElement);

            String senderHomeCommID = inputMessage.getSender().getDevice().getId().get(0).getRoot();
            String receiverHomeCommID = inputMessage.getReceiver().get(0).getDevice().getId().get(0).getRoot();
            logger.info("Sender Home Community ID.... '{}'", senderHomeCommID);
            logger.info("Receiver Home Community ID.. '{}'", receiverHomeCommID);
            logger.info("Constants.HOME_COMM_ID...... '{}'", Constants.HOME_COMM_ID);

            List<PRPAMT201306UV02LivingSubjectId> idList = inputQBP.getParameterList().getLivingSubjectId();
            // TODO: enable demographic searches
            if (!receiverHomeCommID.equals(Constants.HOME_COMM_ID)) {
                fillOutputMessage(outputMessage, "Receiver has wrong Home Community ID.", ERROR_ANSWER_NOT_AVAILABLE);
            } else if (!idList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                List<PatientId> patientIdList = new ArrayList<>();
                sb.append("<patient>");
                for (int idIndex = 0; idIndex < idList.size(); idIndex++) {
                    PatientId patientId = new PatientId();
                    patientId.setRoot(idList.get(idIndex).getValue().get(0).getRoot());
                    patientId.setExtension(idList.get(idIndex).getValue().get(0).getExtension());
                    sb.append("<identifier>");
                    sb.append("<id>").append(patientId.getRoot()).append("</id>");
                    sb.append("<extension>").append(patientId.getExtension()).append("</extension>");
                    sb.append("</identifier>");
                    if (!org.apache.commons.lang3.StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name())) {
                        loggerClinical.info("Using ID Namespace (root)...... '{}':'{}'", idIndex, patientId.getRoot());
                        loggerClinical.info("Using Patient ID (extension)... '{}':'{}'", idIndex, patientId.getExtension());
                    }
                    patientIdList.add(patientId);
                }
                sb.append("</patient>");
                logger.debug("patientIdList.size: '{}'", patientIdList.size());


                // Joao: we have an adhoc XML document, so we can generate this evidence correctly
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));
                    EvidenceUtils.createEvidenceREMNRO(doc, Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            Constants.NCP_SIG_PRIVATEKEY_ALIAS, Constants.SP_KEYSTORE_PATH, Constants.SP_KEYSTORE_PASSWORD,
                            Constants.SP_PRIVATEKEY_ALIAS, Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            Constants.NCP_SIG_PRIVATEKEY_ALIAS, IHEEventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode(),
                            new DateTime(), EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(), "NI_XCPD_REQ",
                            Helper.getHCPAssertion(shElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }

                // call to NI
                List<PatientDemographics> pdList = patientSearchService.getPatientDemographics(patientIdList);

                // Joao: the NRR is being generated based on the request data, not on the response. This NRR is optional as per the CP, so it's left commented
//                try {
//                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                    factory.setNamespaceAware(true);
//                    DocumentBuilder builder = factory.newDocumentBuilder();
//                    Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));
//                    EvidenceUtils.createEvidenceREMNRR(doc,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                            IHEEventType.epsosIdentificationServiceFindIdentityByTraits.getCode(),
//                            new DateTime(),
//                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                            "NI_XCPD_RES",
//                            Helper.getHCPAssertion(shElement).getID() + "__" + DateUtil.getCurrentTimeGMT());
//                } catch (Exception e) {
//                    logger.error(ExceptionUtils.getStackTrace(e));
//                }
                if (pdList.isEmpty()) {
                    // Preparing answer not available error
                    fillOutputMessage(outputMessage, "No patient found.", ERROR_ANSWER_NOT_AVAILABLE, "NF");
                    outputMessage.getAcknowledgement().get(0).getTypeCode().setCode("AA");
                } else {
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
                        logger.info("Could not get client country code from the service consumer certificate. " +
                                "The reason can be that the call was not via HTTPS. Will check the country code from the signature certificate now.");
                        if (sigCountryCode != null) {
                            logger.info("Found the client country code via the signature certificate.");
                            countryCode = sigCountryCode;
                        }
                    }
                    logger.info("The client country code to be used by the PDP: '{}'", countryCode);

                    // Then, it is the Policy Decision Point (PDP) that decides according to the consents of the patients
                    /*
                     * TODO: Uncomment when PDP works. You may also need to pass
                     * the whole PatientID (both the root and extension) to PDP,
                     * if required by PDP procedures.
                     *
                     */
                    for (int i = 0; i < pdList.size(); i++) {
                        if (!SAML2Validator.isConsentGiven(pdList.get(i).getIdList().get(0).getExtension(), countryCode)) {
                            // This patient data cannot be sent to Country B
                            pdList.remove(i);
                            i--;
                        } else {
                            outputMessage.getControlActProcess().getSubject().add(getSubjectByPatientDemographic(pdList.get(i)));
                        }
                    }
                    if (!pdList.isEmpty()) {
                        // There are patient data to be sent, OK
                        fillOutputMessage(outputMessage, null, null, "OK");
                    } else {
                        // No patient data can be sent to Country B.
                        fillOutputMessage(outputMessage, "(4703) Either the security policy of country A or a privacy policy of the patient (that was given in country A) does not allow the requested operation to be performed by the HCP .", ERROR_INSUFFICIENT_RIGHTS);
                        outputMessage.getAcknowledgement().get(0).getTypeCode().setCode("AE");
                    }
                }
            } else {
                // Preparing demographic query not allowed error
                fillOutputMessage(outputMessage, "Queries are only available with patient identifiers", ERROR_DEMOGRAPHIC_QUERY_NOT_ALLOWED);
            }
        } catch (MissingFieldException | InvalidFieldException | InsufficientRightsException | XSDValidationException e) {

            fillOutputMessage(outputMessage, e.getMessage(), ERROR_INSUFFICIENT_RIGHTS);
            logger.error(e.getMessage(), e);
        } catch (Exception e) {

            fillOutputMessage(outputMessage, e.getMessage(), ERROR_ANSWER_NOT_AVAILABLE);
            logger.error(e.getMessage(), e);
        }
        // Set queryByParameter
        PRPAMT201306UV02QueryByParameter qbpValue = objectFactory.createPRPAMT201306UV02QueryByParameter();
        qbpValue.setQueryId(inputQBP.getQueryId());
        qbpValue.setStatusCode(inputQBP.getStatusCode());
        qbpValue.setParameterList(inputQBP.getParameterList());
        outputMessage.getControlActProcess().setQueryByParameter(objectFactory.createPRPAIN201306UV02MFMIMT700711UV01ControlActProcessQueryByParameter(qbpValue));

        // Set sender of the input to receiver of the output
        MCCIMT000300UV01Receiver outReceiver = objectFactory.createMCCIMT000300UV01Receiver();
        outReceiver.setTypeCode(CommunicationFunctionType.RCV);
        outReceiver.setDevice(objectFactory.createMCCIMT000300UV01Device());
        outReceiver.getDevice().setDeterminerCode("INSTANCE");
        outReceiver.getDevice().setClassCode(EntityClassDevice.DEV);
        outReceiver.getDevice().getId().add(inputMessage.getSender().getDevice().getId().get(0));
        // Set asAgent
        MCCIMT000300UV01Agent agent = objectFactory.createMCCIMT000300UV01Agent();
        agent.getClassCode().add("AGNT");

        MCCIMT000300UV01Organization organization = objectFactory.createMCCIMT000300UV01Organization();
        MCCIMT000100UV01Organization inpOrganization = inputMessage.getSender().getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue();
        organization.setClassCode(inpOrganization.getClassCode());
        organization.setDeterminerCode(inpOrganization.getDeterminerCode());
        organization.getId().add(inpOrganization.getId().get(0));

        agent.setRepresentedOrganization(objectFactory.createMCCIMT000300UV01AgentRepresentedOrganization(organization));
        outReceiver.getDevice().setAsAgent(objectFactory.createMCCIMT000300UV01DeviceAsAgent(agent));
        outputMessage.getReceiver().add(outReceiver);

        // Set receiver of the input to sender of the output
        MCCIMT000300UV01Sender outSender = objectFactory.createMCCIMT000300UV01Sender();
        outSender.setTypeCode(CommunicationFunctionType.SND);
        outSender.setDevice(objectFactory.createMCCIMT000300UV01Device());
        outSender.getDevice().setDeterminerCode("INSTANCE");
        outSender.getDevice().setClassCode(EntityClassDevice.DEV);
        outSender.getDevice().getId().add(objectFactory.createII());
        outSender.getDevice().getId().get(0).setRoot(Constants.HOME_COMM_ID);
        outputMessage.setSender(outSender);

        // Prepare Audit Log
        try {
            prepareEventLog(eventLog, inputMessage, outputMessage, shElement);
            logger.info("Preparing Event Log: '{}' - SC UserId: '{}' - SP UserId: '{}'", eventLog.getEventType(),
                    eventLog.getSC_UserID(), eventLog.getSP_UserID());

        } catch (Exception ex) {
            logger.error("Prepare Audit log failed.", ex);
            // Is it fatal, if Audit log cannot be created?
        }
    }
}
