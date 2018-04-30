package epsos.ccd.netsmart.securitymanager.key;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class LAMKeystoreManager implements KeyStoreManager {

    private KeyStore keyStore;
    private KeyStore trustStore;
    private PrivateKey key;
    private X509Certificate cert;

    public LAMKeystoreManager(KeyStore ks, KeyStore ts, String keyAlias, String password)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

        this.keyStore = ks;
        this.trustStore = ts;

        key = (PrivateKey) keyStore.getKey(keyAlias, password.toCharArray());
        cert = (X509Certificate) keyStore.getCertificate(keyAlias);

    }

    @Override
    public KeyPair getDefaultPrivateKey() {
        return new KeyPair(cert.getPublicKey(), key);
    }

    @Override
    public Certificate getDefaultCertificate() {
        return cert;
    }

    @Override
    public KeyPair getPrivateKey(String alias, char[] password) throws SMgrException {

        try {
            PrivateKey mykey = (PrivateKey) keyStore.getKey(alias, password);
            X509Certificate mycert = (X509Certificate) keyStore
                    .getCertificate(alias);
            return new KeyPair(mycert.getPublicKey(), mykey);
        } catch (Exception e) {
            throw new SMgrException(e.getMessage());
        }
    }

    @Override
    public Certificate getCertificate(String alias) throws SMgrException {

        X509Certificate mycert;
        try {
            mycert = (X509Certificate) keyStore
                    .getCertificate(alias);
            return mycert;

        } catch (KeyStoreException e) {
            throw new SMgrException(e.getMessage());
        }
    }

    @Override
    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public KeyStore getTrustStore() {
        return this.trustStore;
    }
}
