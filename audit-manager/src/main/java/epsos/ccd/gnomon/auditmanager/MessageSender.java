package epsos.ccd.gnomon.auditmanager;

import epsos.ccd.gnomon.auditmanager.ssl.AuthSSLSocketFactory;
import epsos.ccd.gnomon.auditmanager.ssl.KeystoreDetails;
import epsos.ccd.gnomon.utils.SerializableMessage;
import epsos.ccd.gnomon.utils.Utils;
import eu.epsos.util.audit.AuditLogSerializer;
import eu.europa.ec.sante.ehdsi.openncp.audit.Configuration;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import net.RFC3881.AuditMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

/**
 * Thread for sending the messages to the syslog repository. Each message is being sent using a different thread.
 * If a message can;t be send immediately, it tries for a time interval.
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
public class MessageSender extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static String[] enabledProtocols = {"TLSv1"};
    private static String AUDIT_REPOSITORY_URL = "audit.repository.url";
    private static String AUDIT_REPOSITORY_PORT = "audit.repository.port";
    private static String KEYSTORE_FILE = "NCP_SIG_KEYSTORE_PATH";
    private static String TRUSTSTORE = "TRUSTSTORE_PATH";
    private static String KEY_ALIAS = "NCP_SIG_PRIVATEKEY_ALIAS";

    private AuditLogSerializer auditLogSerializer;
    private AuditMessage auditmessage;
    private String facility;
    private String severity;

    public MessageSender(AuditLogSerializer auditLogSerializer, AuditMessage auditmessage, String facility, String severity) {

        super();
        this.auditLogSerializer = auditLogSerializer;
        this.auditmessage = auditmessage;
        this.facility = facility;
        this.severity = severity;
    }

    @Override
    public void run() {

        boolean sent = false;
        LOGGER.info("Try to construct the message");
        try {
            LOGGER.info(auditmessage.getEventIdentification().getEventTypeCode().get(0).getCode() + " Try to construct the message");
            String auditmsg = AuditTrailUtils.constructMessage(auditmessage, true);
            if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                LOGGER_CLINICAL.debug("Audit Message sent:\n{}", auditmsg);
            }

            if (!Utils.isEmpty(auditmsg)) {
                long timeout = Long.parseLong(Utils.getProperty("audit.time.to.try", "60000", true));
                boolean timeouted;
                LOGGER.info("Try to send the message for '{}' msec", timeout);
                timeout += System.currentTimeMillis();

                do {
                    try {
                        sent = sendMessage(auditmsg, facility, severity);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    timeouted = System.currentTimeMillis() > timeout;
                    if (!sent && !timeouted) {
                        Utils.sleep(1000);
                    }
                } while (!sent && !timeouted);

                if (timeouted) {
                    LOGGER.info("The time set to epsos.properties in order to retry sending the audit has passed");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (!sent) {
                if (auditLogSerializer != null) {
                    auditLogSerializer.writeObjectToFile(new SerializableMessage(auditmessage, facility, severity));
                } else {
                    LOGGER.info("Failed to send backuped audit message to OpenATNA. Retry later.");
                }
            }
        }
    }

    /**
     * This class is responsible for sending the audit message to the
     * repository. Creates UDP logs for every step.
     *
     * @param auditmsg
     * @param facility
     * @param severity
     * @return true/false depending of the success of sending the message
     */
    protected boolean sendMessage(String auditmsg, String facility, String severity) {

        boolean sent = false;
        String facsev = facility + severity;

        String host = ConfigurationManagerFactory.getConfigurationManager().getProperty(AUDIT_REPOSITORY_URL);
        int port = Integer.parseInt(ConfigurationManagerFactory.getConfigurationManager().getProperty(AUDIT_REPOSITORY_PORT));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set the security properties");
            LOGGER.debug(ConfigurationManagerFactory.getConfigurationManager().getProperty(KEYSTORE_FILE));
            LOGGER.debug(StringUtils.isNotBlank(ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.KEYSTORE_PWD.getValue())) ? "******" : "N/A");
            LOGGER.debug(ConfigurationManagerFactory.getConfigurationManager().getProperty(TRUSTSTORE));
            LOGGER.debug(StringUtils.isNotBlank(ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.TRUSTSTORE_PWD.getValue())) ? "******" : "N/A");
            LOGGER.debug(ConfigurationManagerFactory.getConfigurationManager().getProperty(KEY_ALIAS));
        }

        if (LOGGER.isTraceEnabled()) {

            InputStream stream = null;
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(Utils.fullStream(ConfigurationManagerFactory.getConfigurationManager().getProperty(KEYSTORE_FILE)),
                        ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.KEYSTORE_PWD.getValue()).toCharArray());
                X509Certificate cert = (X509Certificate) ks.getCertificate(ConfigurationManagerFactory.getConfigurationManager().getProperty(KEY_ALIAS));
                LOGGER.debug("KEYSTORE: {}", cert.toString());
                KeyStore ks1 = KeyStore.getInstance("JKS");
                stream = Utils.fullStream(ConfigurationManagerFactory.getConfigurationManager().getProperty(TRUSTSTORE));
                ks1.load(stream, ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.TRUSTSTORE_PWD.getValue()).toCharArray());
                Enumeration<String> enu = ks1.aliases();
                int i = 0;
                while (enu.hasMoreElements()) {
                    i++;
                    String alias = enu.nextElement();
                    LOGGER.debug("ALIAS " + i + " " + alias);
                    LOGGER.debug(ks1.getCertificate(alias).toString());
                }
            } catch (Exception e) {
                LOGGER.error("Error logging keystore file", e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        BufferedOutputStream bos = null;
        SSLSocket sslsocket = null;
        try {
            LOGGER.debug(auditmessage.getEventIdentification().getEventID().getCode() + " Initialize the SSL socket");
            File u = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty(TRUSTSTORE));
            KeystoreDetails trust = new KeystoreDetails(u.toString(), ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.TRUSTSTORE_PWD.getValue()),
                    ConfigurationManagerFactory.getConfigurationManager().getProperty(KEY_ALIAS));
            File uu = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty(KEYSTORE_FILE));
            KeystoreDetails key = new KeystoreDetails(uu.toString(), ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.KEYSTORE_PWD.getValue()),
                    ConfigurationManagerFactory.getConfigurationManager().getProperty(KEY_ALIAS), ConfigurationManagerFactory.getConfigurationManager().getProperty(Configuration.KEYSTORE_PWD.getValue()));
            AuthSSLSocketFactory f = new AuthSSLSocketFactory(key, trust);
            LOGGER.debug(auditmessage.getEventIdentification().getEventID().getCode() + " Create socket");

            sslsocket = (SSLSocket) f.createSecureSocket(host, port);
            LOGGER.debug(auditmessage.getEventIdentification().getEventID().getCode() + " Enabling protocols");
            sslsocket.setEnabledProtocols(enabledProtocols);

            String[] suites = sslsocket.getSupportedCipherSuites();
            sslsocket.setEnabledCipherSuites(suites);

            bos = new BufferedOutputStream(sslsocket.getOutputStream());
            // Syslog Header
            String hostName = sslsocket.getLocalAddress().getHostName();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date now = new Date();
            StringBuilder nowStr = new StringBuilder(sdf.format(now));
            if (nowStr.charAt(4) == '0') {
                nowStr.setCharAt(4, ' ');
            }

            String header = "<" + facsev + ">1 " + nowStr + " " + hostName + " - - - - ";

            // set body of syslog message.
            int length = header.getBytes().length + 3 + auditmsg.getBytes().length;
            bos.write((length + " ").getBytes());
            bos.write(header.getBytes());
            // Sets the bom for utf-8
            bos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            bos.flush();
            LOGGER.debug(auditmessage.getEventIdentification().getEventID().getCode() + " Write the object to bos");
            // Write the syslog message to repository
            bos.write(auditmsg.getBytes());
            LOGGER.info(auditmessage.getEventIdentification().getEventID().getCode() + " Message sent");
            sent = true;
        } catch (Exception e) {
            LOGGER.error(auditmessage.getEventIdentification().getEventID().getCode() + " Error sending message" + e.getMessage(), e);
        } finally {
            // closes the boom and the socket
            Utils.close(bos);
            try {
                if (sslsocket != null)
                    sslsocket.close();
            } catch (IOException e) {
                LOGGER.warn("Unable to close SSLSocket", e);
            }
        }
        return sent;
    }
}
