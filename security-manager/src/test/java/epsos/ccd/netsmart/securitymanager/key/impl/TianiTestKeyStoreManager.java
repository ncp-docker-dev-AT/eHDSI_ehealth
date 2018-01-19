package epsos.ccd.netsmart.securitymanager.key.impl;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * @author jerouris
 */
public class TianiTestKeyStoreManager implements KeyStoreManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TianiTestKeyStoreManager.class);

    private static final String TEST_KEYSTORE_LOCATION = "keystores/tiani/test_keystore_server1.jks";
    private static final String TEST_TRUSTSTORE_LOCATION = "keystores/tiani/test_truststore1.jks";
    private static final String TEST_KEYSTORE_PASSWORD = "spirit";

    private static final String TEST_PRIVATEKEY_ALIAS = "server1";
    private static final String TEST_PRIVATEKEY_PASSWORD = "spirit";

    private KeyStore keyStore;
    private KeyStore trustStore;

    public TianiTestKeyStoreManager() {
        // For testing purposes...
        if (keyStore == null) {
            keyStore = getTestKeyStore();
            trustStore = getTestTrustStore();
        }
    }

    @Override
    public KeyPair getPrivateKey(String alias, char[] password) {

        try {

            // Get private key
            Key key = keyStore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                java.security.cert.Certificate cert = keyStore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            LOGGER.error(null, e);
        }
        return null;
    }

    private KeyStore getTestKeyStore() {

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = ClassLoader.getSystemResourceAsStream(TEST_KEYSTORE_LOCATION);
            keyStore.load(keystoreStream, TEST_KEYSTORE_PASSWORD.toCharArray());

            return keyStore;

        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    @Override
    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public KeyStore getTrustStore() {
        return trustStore;

    }

    private KeyStore getTestTrustStore() {

        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = ClassLoader.getSystemResourceAsStream(TEST_TRUSTSTORE_LOCATION);
            trustStore.load(keystoreStream, TEST_KEYSTORE_PASSWORD.toCharArray());

            return trustStore;

        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    @Override
    public Certificate getCertificate(String alias) {

        try {
            return keyStore.getCertificate(alias);

        } catch (KeyStoreException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    @Override
    public KeyPair getDefaultPrivateKey() {
        return getPrivateKey(TEST_PRIVATEKEY_ALIAS, TEST_PRIVATEKEY_PASSWORD.toCharArray());
    }

    @Override
    public Certificate getDefaultCertificate() {
        return getCertificate(TEST_PRIVATEKEY_ALIAS);
    }
}
