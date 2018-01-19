package epsos.ccd.netsmart.securitymanager.key.impl;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
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
public class NSTestKeyStoreManager implements KeyStoreManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NSTestKeyStoreManager.class);

    private static final String TEST_KEYSTORE_LOCATION = "keystores/epsosTestKeystore.jks";
    private static final String TEST_TRUSTSTORE_LOCATION = "keystores/epsosTestTrustStore.jks";
    private static final String TEST_KEYSTORE_PASSWORD = "epsos123";
    private static final String TEST_TRUSTSTORE_PASSWORD = "epsos123";

    private static final String TEST_PRIVATEKEY_ALIAS = "testncp";
    private static final String TEST_PRIVATEKEY_PASSWORD = "epsos123";

    private KeyStore keyStore;
    private KeyStore trustStore;

    public NSTestKeyStoreManager() {
        // For testing purposes...
        if (keyStore == null) {
            keyStore = getTestKeyStore();
            trustStore = getTestTrustStore();
        }
    }


    public KeyPair getPrivateKey(String alias, char[] password) throws SMgrException {

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
        } catch (UnrecoverableKeyException e) {
            LOGGER.error(null, e);
            throw new SMgrException("Key with alias:" + alias + " is unrecoverable", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(null, e);
            throw new SMgrException("Key with alias:" + alias + " uses an incompatible algorithm", e);
        } catch (KeyStoreException e) {
            LOGGER.error(null, e);
            throw new SMgrException("Key with alias:" + alias + " not found", e);
        }
        return null;
    }

    private KeyStore getTestKeyStore() {

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = ClassLoader.getSystemResourceAsStream(TEST_KEYSTORE_LOCATION);
            keyStore.load(keystoreStream, TEST_KEYSTORE_PASSWORD.toCharArray());

            return keyStore;

        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public KeyStore getTrustStore() {
        return trustStore;

    }

    public Certificate getCertificate(String alias) throws SMgrException {

        try {
            return keyStore.getCertificate(alias);

        } catch (KeyStoreException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException("Certificate with alias:" + alias + " not found in keystore", ex);
        }
    }

    private KeyStore getTestTrustStore() {

        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = ClassLoader.getSystemResourceAsStream(TEST_TRUSTSTORE_LOCATION);
            trustStore.load(keystoreStream, TEST_TRUSTSTORE_PASSWORD.toCharArray());
            return trustStore;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    public KeyPair getDefaultPrivateKey() throws SMgrException {
        return getPrivateKey(TEST_PRIVATEKEY_ALIAS, TEST_PRIVATEKEY_PASSWORD.toCharArray());
    }

    public Certificate getDefaultCertificate() throws SMgrException {
        return getCertificate(TEST_PRIVATEKEY_ALIAS);
    }
}
