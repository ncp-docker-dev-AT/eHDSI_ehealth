package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Inês Garganta
 */
@Service
public class Audit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Audit.class);

    private Audit() {
    }

    public static void sendAuditQuery(String serviceConsumerFullName, String serviceConsumerEmail, String serviceProviderFullName,
                                      String serviceProviderEmail, String participantId, String sourceIP, String targetIP,
                                      String objectId, String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail) {

        LOGGER.info("void sendAuditQuery('{}', '{}', '{}','{}','{}', '{}')",
                participantId, sourceIP, targetIP, objectId, errorMessagePartObjectId, errorMessagePartObjectDetail);

        try {

            AuditService asd = AuditServiceFactory.getInstance();
            String smpServer = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
            String serviceConsumerUserId = HTTPUtil.getSubjectDN(false);
            String serviceProviderUserId = HTTPUtil.getTlsCertificateCommonName(smpServer);

            /*
             * Event Log creation parameters.
             * @param EI_TransactionName
             * @param EI_EventActionCode
             * @param EI_EventDateTime
             * @param EI_EventOutcomeIndicator
             * @param xHR_UserID
             * @param xHR_AlternativeUserID
             * @param xHR_RoleID
             * @param SC_UserID
             * @param SP_UserID
             * @param AS_AuditSourceId
             * @param xPT_ParticipantObjectID
             * @param EM_ParticipantObjectID
             * @param EM_ParticipantObjectDetail
             * @param ET_ObjectID
             * @param ReqM_ParticipantObjectID
             * @param ReqM_ParticipantObjectDetail
             * @param ResM_ParticipantObjectID
             * @param ResM_ParticipantObjectDetail
             * @param sourceIP
             * @param targetIP
             * @return Event Log object initialized.
             */
            EventLog eventLog = EventLog.createEventLogPatientPrivacy(
                    TransactionName.ehealthSMPQuery,
                    EventActionCode.EXECUTE,
                    DateTimeUtil.timeUTC(),
                    EventOutcomeIndicator.FULL_SUCCESS,
                    null, null, null,
                    serviceConsumerUserId,
                    serviceProviderUserId,
                    participantId,
                    null,
                    errorMessagePartObjectId,
                    errorMessagePartObjectDetail,
                    objectId,
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1],
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1], // Base64 encoded error message
                    sourceIP, targetIP);
            eventLog.setEventType(EventType.ehealthSMPQuery);
            eventLog.setNcpSide(NcpSide.NCP_A);

            //  According to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
            //  facility = 13 --> log audit | severity = 2 --> Critical: critical conditions
            asd.write(eventLog, "13", "2");

        } catch (Exception e) {
            LOGGER.error("Error sending audit for eHealth SMP Query: '{}'", e.getMessage(), e);
        }
    }


    public static void sendAuditPush(String serviceConsumerFullName, String serviceConsumerEmail, String serviceProviderFullName,
                                     String serviceProviderEmail, String participantId, String sourceIP, String targetIP,
                                     String objectId, String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail) {

        LOGGER.info("void sendAuditPush('{}', '{}', '{}','{}', '{}')",
                participantId, sourceIP, targetIP, objectId, errorMessagePartObjectId);

        try {

            AuditService asd = AuditServiceFactory.getInstance();
            String smpServer = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
            String serviceConsumerUserId = HTTPUtil.getSubjectDN(false);
            String serviceProviderUserId = HTTPUtil.getTlsCertificateCommonName(smpServer);

            /*
             * Event Log creation parameters.
             * @param EI_TransactionName
             * @param EI_EventActionCode
             * @param EI_EventDateTime
             * @param EI_EventOutcomeIndicator
             * @param xHR_UserID
             * @param xHR_AlternativeUserID
             * @param xHR_RoleID
             * @param SC_UserID
             * @param SP_UserID
             * @param AS_AuditSourceId
             * @param xPT_ParticipantObjectID
             * @param EM_ParticipantObjectID
             * @param EM_ParticipantObjectDetail
             * @param ET_ObjectID
             * @param ReqM_ParticipantObjectID
             * @param ReqM_ParticipantObjectDetail
             * @param ResM_ParticipantObjectID
             * @param ResM_ParticipantObjectDetail
             * @param sourceIP
             * @param targetIP
             * @return Event Log object initialized.
             */
            EventLog eventLog = EventLog.createEventLogPatientPrivacy(
                    TransactionName.ehealthSMPPush,
                    EventActionCode.EXECUTE,
                    DateTimeUtil.timeUTC(),
                    EventOutcomeIndicator.FULL_SUCCESS,
                    null, null, null,
                    serviceConsumerUserId,
                    serviceProviderUserId,
                    participantId,
                    null,
                    errorMessagePartObjectId,
                    errorMessagePartObjectDetail,
                    objectId,
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1],
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1], // Base64 encoded error message
                    sourceIP, targetIP);
            eventLog.setEventType(EventType.ehealthSMPPush);
            eventLog.setNcpSide(NcpSide.NCP_A);

            //  According to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
            //  facility = 13 --> log audit | severity = 2 --> Critical: critical conditions
            asd.write(eventLog, "13", "2");

        } catch (Exception e) {
            LOGGER.error("Error sending audit for eHealth SMP Push: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param bytes
     * @return
     */
    public static String prepareEventLog(byte[] bytes) {

        StringWriter sw = new StringWriter();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(stream);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));

        } catch (TransformerException | ParserConfigurationException | UnsupportedOperationException | SAXException | IOException e) {
            LOGGER.error("{} response: '{}'", e.getClass(), SimpleErrorHandler.printExceptionStackTrace(e));
        }
        return sw.toString();
    }
}
