package eu.epsos.util;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.OidUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

/**
 * @author karkaletsis
 */
public class CertificateUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(CertificateUtils.class);

    private CertificateUtils() {
    }

    public static String getServerCertificate(String country) {

        String oid = OidUtil.getHomeCommunityId(country);
        String endpoint = ConfigurationManagerFactory.getConfigurationManager().getProperty(country + ".PatientIdentificationService.WSE");
        LOGGER.info("OID IS '{}' for country '{}'", oid, country);
        LOGGER.info("ENDPOINT '{}'", endpoint);
        return HTTPUtil.getServerCertificate(endpoint);
    }

    public static X509Certificate getClientCertificate() throws KeyStoreException {

        X509Certificate cert = null;

        KeyStoreManager keyManager = new DefaultKeyStoreManager();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        File file = new File(Constants.NCP_SIG_KEYSTORE_PATH);

        try (FileInputStream stream = new FileInputStream(file)) {

            keyStore.load(stream, Constants.NCP_SIG_KEYSTORE_PASSWORD.toCharArray());
            cert = (X509Certificate) keyManager.getCertificate(Constants.NCP_SIG_PRIVATEKEY_ALIAS);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return cert;
    }
}
