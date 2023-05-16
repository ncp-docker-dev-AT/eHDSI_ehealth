package epsos.ccd.gnomon.auditmanager.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class AuthSSLSocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSSLSocketFactory.class);
    private final X509TrustManager defaultTrustManager;
    private KeystoreDetails details = null;
    private KeystoreDetails truststore = null;
    private SSLContext sslcontext = null;

    /**
     * @param details
     * @param truststore
     * @param defaultTrustManager
     * @throws IOException
     */
    public AuthSSLSocketFactory(KeystoreDetails details, KeystoreDetails truststore, X509TrustManager defaultTrustManager) {
        super();

        if (details != null) {
            this.details = details;
        }
        if (truststore != null) {
            this.truststore = truststore;
        }
        if (defaultTrustManager == null) {
            LOGGER.debug("Using SUN default trust manager");
            this.defaultTrustManager = KeystoreManager.getDefaultTrustManager();
        } else {
            this.defaultTrustManager = defaultTrustManager;
        }
    }

    /**
     * @param details
     * @param truststore
     * @throws IOException
     */
    public AuthSSLSocketFactory(KeystoreDetails details, KeystoreDetails truststore) {
        this(details, truststore, null);
    }

    /**
     * @param details
     * @param defaultTrustManager
     * @throws IOException
     */
    public AuthSSLSocketFactory(KeystoreDetails details, X509TrustManager defaultTrustManager) {
        this(details, null, defaultTrustManager);
    }

    /**
     * @param details
     * @throws IOException
     */
    public AuthSSLSocketFactory(KeystoreDetails details) {
        this(details, null, null);
    }

    /**
     * @param details
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    private static KeyStore createKeyStore(KeystoreDetails details)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        if (details.getKeystoreLocation() == null) {
            throw new IllegalArgumentException("Keystore location may not be null");
        }

        LOGGER.debug("Initializing key store");
        KeyStore keystore = KeyStore.getInstance(details.getKeystoreType());

        try (InputStream is = getKeystoreInputStream(details.getKeystoreLocation())) {

            if (is == null) {
                throw new IOException("Could not open stream to " + details.getKeystoreLocation());
            }
            String password = details.getKeystorePassword();
            keystore.load(is, password != null ? password.toCharArray() : null);
        }
        return keystore;
    }

    /**
     * @param location
     * @return
     */
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
     * @param keystore
     * @param details
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    private KeyManager[] createKeyManagers(final KeyStore keystore, KeystoreDetails details)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {

        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        LOGGER.debug("Initializing key manager");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(details.getAlgType());
        String password = details.getKeyPassword();
        keyManagerFactory.init(keystore, (password == null || password.length() == 0) ?
                details.getKeystorePassword().toCharArray() : password.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    /**
     * @param truststore
     * @param keystore
     * @param defaultTrustManager
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private TrustManager[] createTrustManagers(KeystoreDetails truststore, final KeyStore keystore, X509TrustManager defaultTrustManager)
            throws KeyStoreException, NoSuchAlgorithmException {

        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        for (TrustManager trustmanager : trustManagers) {

            if (trustmanager instanceof X509TrustManager) {
                return new TrustManager[]{
                        new AuthSSLX509TrustManager((X509TrustManager) trustmanager, defaultTrustManager, truststore.getAuthorizedDNs())};
            }
        }
        return trustManagers;
    }

    /**
     * @return
     * @throws IOException
     */
    private SSLContext createSSLContext() throws IOException {

        try {
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = null;
            if (this.details != null) {
                KeyStore keystore = createKeyStore(details);
                Enumeration aliases = keystore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate[] certs = keystore.getCertificateChain(alias);
                    if (certs != null) {
                        LOGGER.debug("Certificate chain: '{}'", alias);
                        for (int c = 0; c < certs.length; c++) {
                            if (certs[c] instanceof X509Certificate) {
                                X509Certificate cert = (X509Certificate) certs[c];
                                LOGGER.debug("Certificate '{}':", (c + 1));
                                LOGGER.debug("   Subject DN: '{}'", cert.getSubjectDN());
                                LOGGER.debug("   Serial Number: '{}'", cert.getSerialNumber());
                                LOGGER.debug("   Signature Algorithm: '{}'", cert.getSigAlgName());
                                LOGGER.debug("   Valid from: '{}'", cert.getNotBefore());
                                LOGGER.debug("   Valid until: '{}'", cert.getNotAfter());
                                LOGGER.debug("   Issuer: '{}'", cert.getIssuerDN());
                            }
                        }
                    }
                }
                keyManagers = createKeyManagers(keystore, details);
            }
            if (this.truststore != null) {
                KeyStore keystore = createKeyStore(truststore);
                Enumeration aliases = keystore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    LOGGER.debug("Trusted certificate: '{}':", alias);
                    Certificate trustedCert = keystore.getCertificate(alias);
                    if (trustedCert instanceof X509Certificate) {
                        X509Certificate cert = (X509Certificate) trustedCert;
                        LOGGER.debug("   Subject DN: '{}'", cert.getSubjectDN());
                        LOGGER.debug("   Serial Number: '{}'", cert.getSerialNumber());
                        LOGGER.debug("   Signature Algorithm: '{}'", cert.getSigAlgName());
                        LOGGER.debug("   Valid from: '{}'", cert.getNotBefore());
                        LOGGER.debug("   Valid until: '{}'", cert.getNotAfter());
                        LOGGER.debug("   Issuer: '{}'", cert.getIssuerDN());
                    }
                }
                trustManagers = createTrustManagers(truststore, keystore, defaultTrustManager);
            }
            if (trustManagers == null) {
                LOGGER.debug("Created Trust Managers from the default...");
                trustManagers = new TrustManager[]{defaultTrustManager};
            }

            SSLContext localSSLContext = SSLContext.getInstance("TLSv1.2");
            localSSLContext.init(keyManagers, trustManagers, null);
            return localSSLContext;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException: '{}'", e.getMessage(), e);
            throw new IOException("Unsupported algorithm exception: " + e.getMessage());
        } catch (KeyStoreException e) {
            LOGGER.error("KeyStoreException: '{}'", e.getMessage(), e);
            throw new IOException("Keystore exception: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            LOGGER.error("GeneralSecurityException: '{}'", e.getMessage(), e);
            throw new IOException("Key management exception: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
            throw new IOException("I/O error reading keystore/truststore file: " + e.getMessage());
        }
    }

    /**
     * @return
     * @throws IOException
     */
    public SSLContext getSSLContext() throws IOException {
        if (this.sslcontext == null) {
            this.sslcontext = createSSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    public Socket createSecureSocket(String host, int port) throws IOException {
        return getSSLContext().getSocketFactory().createSocket(host, port);
    }

    /**
     * @param port
     * @param mutualAuth
     * @return
     * @throws IOException
     */
    public ServerSocket createServerSocket(int port, boolean mutualAuth) throws IOException {
        ServerSocket ss = getSSLContext().getServerSocketFactory().createServerSocket(port);
        if (mutualAuth) {
            ((SSLServerSocket) ss).setNeedClientAuth(true);
        }
        return ss;
    }

    /**
     * @return
     */
    public boolean isSecured() {
        return true;
    }
}
