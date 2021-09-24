package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.GatewayProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {
    }

    public static SSLContext createSSLContext() {

        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();

        PrivateKeyStrategy privatek = (map, socket) -> configurationManager.getProperty(GatewayProperties.GTW_TLS_CLIENT_CERT_ALIAS);

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = null;
        try {
            //must be the same as SC_KEYSTORE_PASSWORD
            sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(new File(configurationManager.getProperty(GatewayProperties.GTW_TLS_CLIENT_KEYSTORE_PATH)),
                            configurationManager.getProperty(GatewayProperties.GTW_TLS_CLIENT_KEYSTORE_PWD).toCharArray(),
                            configurationManager.getProperty(GatewayProperties.GTW_TLS_CLIENT_CERT_PWD).toCharArray(),
                            privatek)
                    .loadTrustMaterial(new File(configurationManager.getProperty(GatewayProperties.GTW_TRUSTSTORE_PATH)),
                            configurationManager.getProperty(GatewayProperties.GTW_TRUSTSTORE_PWD).toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("NoSuchAlgorithmException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (KeyStoreException ex) {
            LOGGER.error("KeyStoreException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (CertificateException ex) {
            LOGGER.error("CertificateException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            LOGGER.error("IOException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (KeyManagementException ex) {
            LOGGER.error("KeyManagementException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (UnrecoverableKeyException ex) {
            LOGGER.error("UnrecoverableKeyException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        return sslcontext;
    }
}
