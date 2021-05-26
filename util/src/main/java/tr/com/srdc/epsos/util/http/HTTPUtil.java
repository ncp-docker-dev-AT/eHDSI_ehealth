package tr.com.srdc.epsos.util.http;

import eu.epsos.util.proxy.CustomProxySelector;
import eu.epsos.util.proxy.ProxyCredentials;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import org.cryptacular.util.CertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HTTPUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtil.class);

    public static String getHostIpAddress(String host) {
        try {
            //return IPUtil.getPrivateServerIp();
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return "Server IP Unknown";
        }
    }

    /**
     * @param request
     * @return
     */
    public static String getClientCertificate(HttpServletRequest request) {

        LOGGER.info("Trying to find certificate from : '{}'", request.getRequestURI());
        String result;
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            result = getCommonName(certs[0]);
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
            return getCommonName(cert);
        }
        return "Warning!: No Server certificate found!";
    }

    private static Certificate[] getSSLPeerCertificate(String host, boolean sslValidation) {

        HttpsURLConnection urlConnection = null;

        if (!sslValidation) {

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                }
            }
            };

            try (InputStream is = getKeystoreInputStream(Constants.SC_KEYSTORE_PATH)) {

                // Install the all-trusting trust manager
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(is, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(keyStore, Constants.SC_KEYSTORE_PASSWORD.toCharArray());

                // Install the all-trusting trust manager
                SSLContext sslContext;
                sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

                URL url;
                url = new URL(host);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setHostnameVerifier((hostname, session) -> true);
                urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                urlConnection.connect();
                return urlConnection.getServerCertificates();

            } catch (IOException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
                    | KeyManagementException | CertificateException e) {
                LOGGER.error("Error: '{}'", e.getMessage(), e);
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return new Certificate[]{};
    }

    private static InputStream getKeystoreInputStream(String location) {

        try {
            File file = new File(location);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            URL url = new URL(location);
            return url.openStream();

        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }

        LOGGER.warn("Could not open stream to: '{}'", location);
        return null;
    }

    /**
     * @param endpoint
     * @return
     */
    public static String getServerCertificate(String endpoint) {

        LOGGER.debug("Trying to find certificate from : '{}'", endpoint);
        String result = "";
        HttpsURLConnection urlConnection = null;

        try {
            if (!endpoint.startsWith("https")) {
                result = "Warning!: No Server certificate found!";
            } else {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

                URL url;
                url = new URL(endpoint);
                urlConnection = (HttpsURLConnection) url.openConnection();
                //TODO: not sustainable solution: EHNCP-1363
                urlConnection.setHostnameVerifier((hostname, session) -> true);
                // End EHNCP-1363
                urlConnection.setSSLSocketFactory(sslsocketfactory);
                urlConnection.connect();
                Certificate[] certs = urlConnection.getServerCertificates();

                // Get the first certificate
                if (certs != null && certs.length > 0) {
                    X509Certificate cert = (X509Certificate) certs[0];
                    result = getCommonName(cert);
                } else {
                    result = "Warning!: No Server certificate found!";
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
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
                return getCommonName(x509Certificate);
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        return "";
    }

    /**
     * @return
     */
    public static boolean isBehindProxy() {

        return Boolean.parseBoolean(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_USED));
    }

    /**
     * @return
     */
    public static ProxyCredentials getProxyCredentials() {

        ProxyCredentials credentials = new ProxyCredentials();
        ConfigurationManager configService = ConfigurationManagerFactory.getConfigurationManager();
        credentials.setProxyAuthenticated(Boolean.parseBoolean(configService.getProperty(StandardProperties.HTTP_PROXY_USED)));
        credentials.setHostname(configService.getProperty(StandardProperties.HTTP_PROXY_HOST));
        credentials.setPassword(configService.getProperty(StandardProperties.HTTP_PROXY_PASSWORD));
        credentials.setPort(configService.getProperty(StandardProperties.HTTP_PROXY_PORT));
        credentials.setUsername(configService.getProperty(StandardProperties.HTTP_PROXY_USERNAME));
        return credentials;
    }

    /**
     * @return
     */
    public CustomProxySelector setCustomProxyServerForURLConnection() {

        CustomProxySelector ps;
        if (isBehindProxy()) {
            ProxyCredentials proxyCredentials = getProxyCredentials();
            ps = new CustomProxySelector(ProxySelector.getDefault(), proxyCredentials);
            return ps;
        }
        return null;
    }

    private static String getCommonName(java.security.cert.X509Certificate cert){
        return CertUtil.subjectCN(cert);
    }
}
