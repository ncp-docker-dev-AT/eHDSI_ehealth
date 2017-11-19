package tr.com.srdc.epsos.util.http;

import eu.epsos.util.proxy.CustomProxySelector;
import eu.epsos.util.proxy.ProxyCredentials;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *
 */
public class HTTPUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtil.class);

    /**
     * @param request
     * @return
     */
    public static String getClientCertificate(HttpServletRequest request) {

        LOGGER.info("Trying to find certificate from : '{}'", request.getRequestURI());
        String result;
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {

            result = certs[0].getSubjectDN().getName();
        } else {
            if ("https".equals(request.getScheme())) {
                LOGGER.warn("This was an HTTPS request, " + "but no client certificate is available");
            } else {
                LOGGER.warn("This was not an HTTPS request, " + "so no client certificate is available");
            }
            result = "Warning!: No Client certificate found!";
        }
        LOGGER.debug("Client Certificate: '{}'", result);
        return result;
    }

    /**
     * @param endpoint
     * @return
     */
    public static String getServerCertificate(String endpoint) {

        LOGGER.info("Trying to find certificate from : '{}'", endpoint);
        String result = "";
        HttpsURLConnection con = null;

        try {
            if (!endpoint.startsWith("https")) {
                result = "Warning!: No Server certificate found!";
            } else {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

                URL url;
                url = new URL(endpoint);
                con = (HttpsURLConnection) url.openConnection();
                //TODO: not sustainable solution: EHNCP-1363
                con.setHostnameVerifier((hostname, session) -> true);
                // End EHNCP-1363
                con.setSSLSocketFactory(sslsocketfactory);
                con.connect();
                Certificate[] certs = con.getServerCertificates();

                // Get the first certificate
                if (certs != null && certs.length > 0) {
                    X509Certificate cert = (X509Certificate) certs[0];
                    result = cert.getSubjectDN().getName();
                } else {
                    result = "Warning!: No Server certificate found!";
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        LOGGER.debug("Server Certificate: '{}'", result);
        return result;

    }

    /**
     * @param isProvider
     * @return
     */
    public static String getSubjectDN(boolean isProvider) {

        FileInputStream inputStream = null;
        Certificate cert;

        try {
            if (isProvider) {
                inputStream = new FileInputStream(Constants.SP_KEYSTORE_PATH);
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(inputStream, Constants.SP_KEYSTORE_PASSWORD.toCharArray());
                cert = keystore.getCertificate(Constants.SP_PRIVATEKEY_ALIAS);
            } else {
                inputStream = new FileInputStream(Constants.SC_KEYSTORE_PATH);
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(inputStream, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
                cert = keystore.getCertificate(Constants.SC_PRIVATEKEY_ALIAS);
            }
            if (cert instanceof X509Certificate) {
                X509Certificate x509cert = (X509Certificate) cert;
                Principal principal = x509cert.getSubjectDN();
                return principal.getName();
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return "";
    }

    /**
     * @return
     */
    public static boolean isProxyAnthenticationMandatory() {

        ConfigurationManager configService = ConfigurationManagerFactory.getConfigurationManager();

        return Boolean.parseBoolean(configService.getProperty("APP_BEHIND_PROXY"));
    }

    /**
     * @return
     */
    public static ProxyCredentials getProxyCredentials() {
        ProxyCredentials credentials = new ProxyCredentials();
        ConfigurationManager configService = ConfigurationManagerFactory.getConfigurationManager();
        credentials.setProxyAuthenticated(Boolean.parseBoolean(configService.getProperty("APP_BEHIND_PROXY")));
        credentials.setHostname(configService.getProperty("APP_PROXY_HOST"));
        credentials.setPassword(configService.getProperty("APP_PROXY_PASSWORD"));
        credentials.setPort(configService.getProperty("APP_PROXY_PORT"));
        credentials.setUsername(configService.getProperty("APP_PROXY_USERNAME"));
        return credentials;
    }

    /**
     * @return
     */
    public CustomProxySelector setCustomProxyServerForURLConnection() {
        CustomProxySelector ps = null;
        if (isProxyAnthenticationMandatory()) {
            ProxyCredentials proxyCredentials = getProxyCredentials();
            ps = new CustomProxySelector(ProxySelector.getDefault(), proxyCredentials);
            return ps;
        }
        return null;
    }
}
