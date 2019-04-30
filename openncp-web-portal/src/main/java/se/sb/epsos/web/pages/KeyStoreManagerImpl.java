package se.sb.epsos.web.pages;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.service.NcpServiceConfigManager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public final class KeyStoreManagerImpl implements KeyStoreManager {

    private static final String ASSERTION = "assertion";
    private final Logger logger = LoggerFactory.getLogger(KeyStoreManagerImpl.class);
    private KeyStore keyStore;
    private KeyStore trustStore;

    public KeyStoreManagerImpl() throws KeyStoreInitializationException {
        keyStore = getKeyStore();
        trustStore = getTrustStore();
    }

    private KeyStore getKeyStore() throws KeyStoreInitializationException {

        InputStream keystoreStream = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystoreStream = getClass().getClassLoader().getResourceAsStream(NcpServiceConfigManager.getPrivateKeystoreLocation(ASSERTION));
            if (keystoreStream == null) {
                keystoreStream = new FileInputStream(NcpServiceConfigManager.getPrivateKeystoreLocation(ASSERTION));
            }

            keyStore.load(keystoreStream, NcpServiceConfigManager.getPrivateKeystorePassword(ASSERTION).toCharArray());
            logger.debug("keystore loaded");
        } catch (Exception e) {
            throw new KeyStoreInitializationException("Failed to load keystore " + NcpServiceConfigManager.getPrivateKeystoreLocation(ASSERTION), e);
        } finally {
            IOUtils.closeQuietly(keystoreStream);
        }
        return keyStore;
    }

    private KeyStore getTrustStore() throws KeyStoreInitializationException {

        InputStream trustStoreStream = null;

        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStoreStream = getClass().getClassLoader().getResourceAsStream(NcpServiceConfigManager.getTruststoreLocation(ASSERTION));
            if (trustStoreStream == null) {
                trustStoreStream = new FileInputStream(NcpServiceConfigManager.getTruststoreLocation(ASSERTION));
            }
            trustStore.load(trustStoreStream, NcpServiceConfigManager.getTruststorePassword(ASSERTION).toCharArray());
            logger.debug("truststore loaded");
        } catch (Exception e) {
            throw new KeyStoreInitializationException("Failed to load keystore " + NcpServiceConfigManager.getTruststoreLocation(ASSERTION), e);
        } finally {
            IOUtils.closeQuietly(trustStoreStream);
        }
        return trustStore;
    }

    public KeyPair getPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key key = keyStore.getKey(NcpServiceConfigManager.getPrivateKeyAlias(ASSERTION), NcpServiceConfigManager.getPrivateKeystorePassword(ASSERTION).toCharArray());
        if (key instanceof PrivateKey) {
            Certificate cert = keyStore.getCertificate(NcpServiceConfigManager.getPrivateKeyAlias(ASSERTION));
            PublicKey publicKey = cert.getPublicKey();
            return new KeyPair(publicKey, (PrivateKey) key);
        }
        return null;
    }

    public X509Certificate getCertificate() throws KeyStoreException {
        return (X509Certificate) keyStore.getCertificate(NcpServiceConfigManager.getPrivateKeyAlias(ASSERTION));
    }
}
