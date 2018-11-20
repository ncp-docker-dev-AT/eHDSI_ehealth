package tr.com.srdc.epsos.util.http;

import eu.epsos.util.proxy.CustomProxySelector;
import eu.epsos.util.proxy.ProxyCredentials;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.X500Name;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

            try {
                result = ((X500Name) certs[0].getSubjectDN()).getCommonName();
            } catch (IOException e) {
                result = "Warning!: No Client certificate found!";
            }
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

    public static String getTlsCertificateCommonName(String host) {

        Certificate[] certificates = getSSLPeerCertificate(host, false);
        if (certificates != null && certificates.length > 0) {
            X509Certificate cert = (X509Certificate) certificates[0];
            try {
                return ((X500Name) cert.getSubjectDN()).getCommonName();
            } catch (IOException e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
            }
        }
        return "Warning!: No Server certificate found!";
    }

    public static Certificate[] getSSLPeerCertificate(String host, boolean sslValidation) {

        HttpsURLConnection con = null;

        if (!sslValidation) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                }
            }
            };
            // Install the all-trusting trust manager
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLSv1.2");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
            }
            try {
                URL url;
                url = new URL(host);
                con = (HttpsURLConnection) url.openConnection();
                //TODO: not sustainable solution: EHNCP-1363
                con.setHostnameVerifier((hostname, session) -> true);
                // End EHNCP-1363
                con.setSSLSocketFactory(sc.getSocketFactory());
                con.connect();
                return con.getServerCertificates();
            } catch (IOException e) {
                LOGGER.error("IOException: '{}'", e.getMessage(), e);
            } finally {

                if (con != null) {
                    con.disconnect();
                }
            }
        }
        return new Certificate[]{};
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
                    result = ((X500Name) cert.getSubjectDN()).getCommonName();
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

        Certificate cert;
        String keystorePath;
        if (isProvider) {
            keystorePath = Constants.SP_KEYSTORE_PATH;
        } else {
            keystorePath = Constants.SC_KEYSTORE_PATH;
        }

        try (FileInputStream inputStream = new FileInputStream(keystorePath)) {

            if (isProvider) {
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(inputStream, Constants.SP_KEYSTORE_PASSWORD.toCharArray());
                cert = keystore.getCertificate(Constants.SP_PRIVATEKEY_ALIAS);
            } else {

                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(inputStream, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
                cert = keystore.getCertificate(Constants.SC_PRIVATEKEY_ALIAS);
            }
            if (cert instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) cert;
                Principal principal = x509Certificate.getSubjectDN();
                return ((X500Name) principal).getCommonName();
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        return "";
    }

    /**
     * @return
     */
    public static boolean isProxyAuthenticationMandatory() {

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

        CustomProxySelector ps;
        if (isProxyAuthenticationMandatory()) {
            ProxyCredentials proxyCredentials = getProxyCredentials();
            ps = new CustomProxySelector(ProxySelector.getDefault(), proxyCredentials);
            return ps;
        }
        return null;
    }
}
