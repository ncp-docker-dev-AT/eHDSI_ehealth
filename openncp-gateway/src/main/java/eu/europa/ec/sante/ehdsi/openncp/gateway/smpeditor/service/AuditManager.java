package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Inês Garganta
 */
@Service
public class AuditManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditManager.class);

    private AuditManager() {
    }

    /**
     * @param smpServerUri
     * @param objectID
     * @param errorMessagePartObjectId
     * @param errorMessagePartObjectDetail
     */
    public static void handleDynamicDiscoveryQuery(String smpServerUri, String objectID, String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail) {

        AuditService auditService = AuditServiceFactory.getInstance();
        EventLog eventLog = createDynamicDiscoveryEventLog(TransactionName.ehealthSMPQuery, objectID, errorMessagePartObjectId, errorMessagePartObjectDetail, smpServerUri);
        eventLog.setEventType(EventType.ehealthSMPQuery);
        eventLog.setNcpSide(NcpSide.NCP_A);
        auditService.write(eventLog, "13", "2");
    }

    /**
     * @param smpServerUri
     * @param objectID
     * @param errorMessagePartObjectId
     * @param errorMessagePartObjectDetail
     */
    public static void handleDynamicDiscoveryPush(String smpServerUri, String objectID, String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail) {

        AuditService auditService = AuditServiceFactory.getInstance();
        EventLog eventLog = createDynamicDiscoveryEventLog(TransactionName.ehealthSMPPush, objectID, errorMessagePartObjectId, errorMessagePartObjectDetail, smpServerUri);
        eventLog.setEventType(EventType.ehealthSMPPush);
        eventLog.setNcpSide(NcpSide.NCP_A);
        auditService.write(eventLog, "13", "2");
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
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));

        } catch (TransformerException | ParserConfigurationException | UnsupportedOperationException | SAXException | IOException e) {
            LOGGER.error("{} response: '{}'", e.getClass(), SimpleErrorHandler.printExceptionStackTrace(e));
        }
        return sw.toString();
    }

    /**
     * @param transactionName
     * @param objectID
     * @param errorMessagePartObjectId
     * @param errorMessagePartObjectDetail
     * @param smpServerUri
     * @return
     */
    private static EventLog createDynamicDiscoveryEventLog(TransactionName transactionName, String objectID,
                                                           String errorMessagePartObjectId, byte[] errorMessagePartObjectDetail,
                                                           String smpServerUri) {

        String serviceConsumerUserId = HTTPUtil.getSubjectDN(false);
        String serviceProviderUserId = HTTPUtil.getTlsCertificateCommonName(smpServerUri);
        String localIp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SERVER_IP");
        String participantId = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");

        return EventLog.createEventLogPatientPrivacy(transactionName, EventActionCode.EXECUTE, timeUTC(),
                EventOutcomeIndicator.FULL_SUCCESS, null, null, null,
                serviceConsumerUserId, serviceProviderUserId, participantId, null,
                errorMessagePartObjectId, errorMessagePartObjectDetail, objectID, null,
                new byte[1], null, new byte[1], localIp, smpServerUri);
    }

    /**
     * @return
     */
    private static XMLGregorianCalendar timeUTC() {
        DatatypeFactory factory;
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException();
        }
        return factory.newXMLGregorianCalendar(new GregorianCalendar(TimeZone.getTimeZone(ZoneOffset.UTC)));
    }
}
