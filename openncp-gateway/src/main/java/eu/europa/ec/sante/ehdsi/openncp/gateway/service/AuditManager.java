package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import com.sun.jndi.toolkit.url.Uri;
import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditService;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.http.HTTPUtil;
import tr.com.srdc.epsos.util.http.IPUtil;

import javax.xml.XMLConstants;
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
import java.net.MalformedURLException;

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
    public static void handleDynamicDiscoveryQuery(String smpServerUri, String objectID, String errorMessagePartObjectId,
                                                   byte[] errorMessagePartObjectDetail) {

        AuditService auditService = AuditServiceFactory.getInstance();
        EventLog eventLog = createDynamicDiscoveryEventLog(TransactionName.SMP_QUERY, objectID, errorMessagePartObjectId,
                errorMessagePartObjectDetail, smpServerUri);
        eventLog.setEventType(EventType.SMP_QUERY);
        eventLog.setNcpSide(NcpSide.NCP_A);
        auditService.write(eventLog, "13", "2");
    }

    /**
     * @param smpServerUri
     * @param objectID
     * @param errorMessagePartObjectId
     * @param errorMessagePartObjectDetail
     */
    public static void handleDynamicDiscoveryPush(String smpServerUri, String objectID, String errorMessagePartObjectId,
                                                  byte[] errorMessagePartObjectDetail) {

        AuditService auditService = AuditServiceFactory.getInstance();
        EventLog eventLog = createDynamicDiscoveryEventLog(TransactionName.SMP_PUSH, objectID, errorMessagePartObjectId,
                errorMessagePartObjectDetail, smpServerUri);
        eventLog.setEventType(EventType.SMP_PUSH);
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
        String localIp = IPUtil.getPrivateServerIp();
        String participantId = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
        Uri uri = null;
        try {
            uri = new Uri(smpServerUri);
        } catch (MalformedURLException e) {
            LOGGER.error("MalformedURLException: '{}'", e.getMessage(), e);
        }
        return EventLog.createEventLogPatientPrivacy(transactionName, EventActionCode.EXECUTE, DateTimeUtil.timeUTC(),
                EventOutcomeIndicator.FULL_SUCCESS, null, null, null,
                serviceConsumerUserId, serviceProviderUserId, participantId, null,
                errorMessagePartObjectId, errorMessagePartObjectDetail, objectID, null,
                new byte[1], null, new byte[1], localIp, uri != null ? uri.getHost() : smpServerUri);
    }
}
