package eu.europa.ec.sante.ehdsi.openncp.pt.common;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class ClientSocketFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSocketFactory.class);

    private static final String KEY_STORE_PASSWORD;

    static {
        ConfigurationManager cm = ConfigurationManagerFactory.getConfigurationManager();
        KEY_STORE_PASSWORD = cm.getProperty("NCP_SIG_KEYSTORE_PASSWORD");
    }

    private ClientSocketFactory() {
    }

    public static void setServiceClientConfig(ServiceClient serviceClient, String[] supportedProtocol) throws AxisFault {
        try {
            KeyStoreManager ksm = new DefaultKeyStoreManager();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ksm.getKeyStore(), KEY_STORE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ksm.getTrustStore());
            SSLContext sslContext = SSLContexts.createDefault();
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    supportedProtocol,
                    null,
                    new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
            serviceClient.getServiceContext().getConfigurationContext().setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            serviceClient.getServiceContext().getConfigurationContext().setProperty(HTTPConstants.REUSE_HTTP_CLIENT, false);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            throw AxisFault.makeFault(e);
        }
    }
}
