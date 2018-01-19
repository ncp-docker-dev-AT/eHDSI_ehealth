package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import epsos.ccd.gnomon.auditmanager.*;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

    public static void sendAuditQuery(String sc_fullname, String sc_email, String sp_fullname, String sp_email,
                                      String partid, String sourceip, String targetip, String objectID,
                                      String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail) {
        try {
            AuditService asd = AuditServiceFactory.getInstance();

            /*
             *
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
             * @param xPT_PatricipantObjectID
             * @param EM_PatricipantObjectID
             * @param EM_PatricipantObjectDetail
             * @param ET_ObjectID
             * @param ReqM_ParticipantObjectID
             * @param ReqM_PatricipantObjectDetail
             * @param ResM_ParticipantObjectID
             * @param ResM_PatricipantObjectDetail
             * @param sourceip
             * @param targetip
             * @return
             */
            String sc_userid = sc_fullname + "<saml:" + sc_email + ">";
            String sp_userid = sp_fullname + "<saml:" + sp_email + ">";
            EventLog eventLog1 = EventLog.createEventLogPatientPrivacy(
                    TransactionName.ehealthSMPQuery,
                    EventActionCode.EXECUTE,
                    DateTimeUtil.timeUTC(),
                    EventOutcomeIndicator.FULL_SUCCESS,
                    null, null, null,
                    sc_userid,
                    sp_userid,
                    partid,
                    null,
                    EM_PatricipantObjectID,
                    EM_PatricipantObjectDetail,
                    objectID,
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1],
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1], // Base64 encoded error message
                    sourceip, targetip);
            eventLog1.setEventType(EventType.ehealthSMPQuery);
            //facility = 13 --> log audit | severity = 2 --> Critical: critical conditions
            //Acording to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
            asd.write(eventLog1, "13", "2");
    /*  try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        LOGGER.error(null, ex);
      }*/
        } catch (Exception e) {
            LOGGER.error("Error sending audit for eHealth SMP Query: '{}'", e.getMessage(), e);
        }
    }


    public static void sendAuditPush(String sc_fullname, String sc_email, String sp_fullname, String sp_email,
                                     String partid, String sourceip, String targetip, String objectID,
                                     String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail) {

        LOGGER.info("void sendAuditPush('{}', '{}', '{}','{}','{}','{}','{}', '{}','{}','{}')",
                sc_fullname, sc_email, sp_fullname, sp_email, partid, sourceip, targetip, objectID, EM_PatricipantObjectID,
                "EM_PatricipantObjectDetail");
        try {
            AuditService asd = AuditServiceFactory.getInstance();

            /*
             *
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
             * @param xPT_PatricipantObjectID
             * @param EM_PatricipantObjectID
             * @param EM_PatricipantObjectDetail
             * @param ET_ObjectID
             * @param ReqM_ParticipantObjectID
             * @param ReqM_PatricipantObjectDetail
             * @param ResM_ParticipantObjectID
             * @param ResM_PatricipantObjectDetail
             * @param sourceip
             * @param targetip
             * @return
             */
            String sc_userid = sc_fullname + "<saml:" + sc_email + ">";
            String sp_userid = sp_fullname + "<saml:" + sp_email + ">";
            EventLog eventLog1 = EventLog.createEventLogPatientPrivacy(
                    TransactionName.ehealthSMPPush,
                    EventActionCode.EXECUTE,
                    DateTimeUtil.timeUTC(),
                    EventOutcomeIndicator.FULL_SUCCESS,
                    null, null, null,
                    sc_userid,
                    sp_userid,
                    partid,
                    null,
                    EM_PatricipantObjectID,
                    EM_PatricipantObjectDetail,
                    objectID,
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1],
                    //"urn:uuid:00000000-0000-0000-0000-000000000000",
                    null,
                    new byte[1], // Base64 encoded error message
                    sourceip, targetip);
            eventLog1.setEventType(EventType.ehealthSMPPush);
            //facility = 13 --> log audit | severity = 2 --> Critical: critical conditions
            //Acording to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
            asd.write(eventLog1, "13", "2");
     /* try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        LOGGER.error(null, ex);
      }*/
        } catch (Exception e) {
            LOGGER.error("Error sending audit for eHealth SMP Push: '{}'", e.getMessage(), e);
        }
    }

    public static String prepareEventLog(byte[] bytes) {

        StringWriter sw = new StringWriter();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(bais);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
        } catch (TransformerConfigurationException ex) {
            LOGGER.error("\n TransformerConfigurationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (TransformerException ex) {
            LOGGER.error("\n TransformerException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (ParserConfigurationException ex) {
            LOGGER.error("\n ParserConfigurationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (UnsupportedOperationException ex) {
            LOGGER.error("\n UnsupportedOperationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (SAXException ex) {
            LOGGER.error("\n SAXException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            LOGGER.error("\n IOException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        return sw.toString();
    }
}
