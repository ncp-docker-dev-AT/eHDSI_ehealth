package ee.affecto.epsos.util;

import ee.affecto.epsos.ws.handler.DummyMustUnderstandHandler;
import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.List;

//TODO: Review OpenNCP-2.5.5
public class EventLogClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogClientUtil.class);

    private EventLogClientUtil() {
    }

    /**
     * @param stub
     */
    public static void createDummyMustUnderstandHandler(org.apache.axis2.client.Stub stub) {

        HandlerDescription description = new HandlerDescription("DummyMustUnderstandHandler");
        description.setHandler(new DummyMustUnderstandHandler());
        AxisConfiguration axisConfiguration = stub._getServiceClient().getServiceContext().getConfigurationContext()
                .getAxisConfiguration();
        List<Phase> phasesList = axisConfiguration.getInFlowPhases();
        Phase myPhase = new Phase("MyPhase");
        try {
            myPhase.addHandler(description);
        } catch (PhaseException ex) {
            throw new RuntimeException(ex);
        }
        phasesList.add(0, myPhase);
        axisConfiguration.setInFaultPhases(phasesList);
    }

    /**
     * @param epr
     * @return
     */
    public static String getServerCertificateDN(String epr) {

        String dn = null;

        try {
            // TODO A.R. Silly, very silly  to open connection again just for getting server certificate.
            // But can we get it from AXIS2?

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL url = new URL(epr);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.connect();
            Certificate[] certs = con.getServerCertificates();
            // Peers certificate is first
            if (certs != null && certs.length > 0) {
                X509Certificate cert = (X509Certificate) certs[0];
                dn = cert.getSubjectDN().getName();
            }
            con.disconnect();
        } catch (Exception ex) {
            LOGGER.error("Exception: '{}'", ex.getMessage(), ex);
        }
        return dn;
    }

    /**
     * @return
     */
    public static String getLocalIpAddress() {

        // TODO A.R. should be changed for multihomed hosts... It's better to get address from AXIS2 actual socket but how?
        String ipAddress = null;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {

            LOGGER.error("UnknownHostException: '{}'", e.getMessage(), e);
        }
        return ipAddress;
    }

    /**
     * @param epr
     * @return
     */
    public static String getServerIpAddress(String epr) {

        URL url;
        String ipAddress = null;
        try {
            url = new URL(epr);
            ipAddress = InetAddress.getByName(url.getHost()).getHostAddress();
        } catch (Exception e) {
            LOGGER.error(null, e);
        }
        return ipAddress;
    }

    /**
     * @param msgContext
     * @param soapEnvelope
     * @param address
     * @return
     */
    public static EventLog prepareEventLog(MessageContext msgContext, SOAPEnvelope soapEnvelope, String address) {

        EventLog eventLog = new EventLog();

        try {
            eventLog.setEI_EventDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
        }

        // Set Active Participant Identification: Service Consumer NCP
        eventLog.setSC_UserID(HTTPUtil.getSubjectDN(false));
        eventLog.setSP_UserID(HTTPUtil.getServerCertificate(address));

        // Set Audit Source
        ConfigurationManager cms = ConfigurationManagerFactory.getConfigurationManager();
        eventLog.setAS_AuditSourceId(cms.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION"));

        // Set Source Ip
        String localIp = EventLogClientUtil.getLocalIpAddress();
        if (localIp != null) {
            eventLog.setSourceip(localIp);
        }

        // Set Target Ip
        String serverIp = EventLogClientUtil.getServerIpAddress(address);
        if (serverIp != null) {
            eventLog.setTargetip(serverIp);
        }

        // Set Participant Object: Request Message
        String reqMessageId = appendUrnUuid(EventLogUtil.getMessageID(msgContext.getEnvelope()));
        eventLog.setReqM_ParticipantObjectID(reqMessageId);
        eventLog.setReqM_PatricipantObjectDetail(msgContext.getEnvelope().getHeader().toString().getBytes());

        // Set Participant Object: ResponseMessage
        String rspMessageId = appendUrnUuid(EventLogUtil.getMessageID(soapEnvelope));
        eventLog.setResM_ParticipantObjectID(rspMessageId);
        eventLog.setResM_PatricipantObjectDetail(soapEnvelope.getHeader().toString().getBytes());

        return eventLog;
    }

    /**
     * @param eventLog
     * @param idAssertion
     */
    public static void logIdAssertion(EventLog eventLog, Assertion idAssertion) {

        String spProvidedID = idAssertion.getSubject().getNameID().getSPProvidedID();
        String humanReqUserId = StringUtils.isNotBlank(spProvidedID) ? spProvidedID : "" + "<" + idAssertion.getSubject().getNameID().getValue()
                + "@" + idAssertion.getIssuer().getValue() + ">";
        eventLog.setHR_UserID(humanReqUserId);
        boolean isOrganizationProvided = false;

        for (AttributeStatement attributeStatement : idAssertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:oasis:names:tc:xacml:1.0:subject:subject-id")) {
                    eventLog.setHR_AlternativeUserID(EventLogUtil.getAttributeValue(attribute));
                } else if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:oasis:names:tc:xacml:2.0:subject:role")) {
                    eventLog.setHR_RoleID(EventLogUtil.getAttributeValue(attribute));
                } else if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:epsos:names:wp3.4:subject:healthcare-facility-type")) {
                    eventLog.setPC_RoleID(EventLogUtil.getAttributeValue(attribute));
                } else if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:oasis:names:tc:xspa:1.0:subject:organization")) {
                    eventLog.setPC_UserID(EventLogUtil.getAttributeValue(attribute));
                    isOrganizationProvided = true;
                } else if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:oasis:names:tc:xspa:1.0:environment:locality") && !isOrganizationProvided) {
                    eventLog.setPC_UserID(EventLogUtil.getAttributeValue(attribute));
                }
            }
        }
    }

    /**
     * @param eventLog
     * @param idAssertion
     */
    public static void logTrcAssertion(EventLog eventLog, Assertion idAssertion) {

        for (AttributeStatement attributeStatement : idAssertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (StringUtils.equalsIgnoreCase(attribute.getName(), "urn:oasis:names:tc:xacml:1.0:resource:resource-id")) {
                    eventLog.setPT_PatricipantObjectID(EventLogUtil.getAttributeValue(attribute));
                    break;
                }
            }
        }
    }

    /**
     * @param eventLog
     */
    public static void sendEventLog(EventLog eventLog) {

        AuditServiceFactory.getInstance().write(eventLog, "", "1");
    }

    /**
     * @param uuid
     * @return
     */
    public static String appendUrnUuid(String uuid) {

        if (uuid == null || uuid.isEmpty() || uuid.startsWith(Constants.UUID_PREFIX)) {
            return uuid;
        } else return Constants.UUID_PREFIX + uuid;
    }
}
