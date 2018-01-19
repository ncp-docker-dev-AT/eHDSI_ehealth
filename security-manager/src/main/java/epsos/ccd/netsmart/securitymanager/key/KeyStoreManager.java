package epsos.ccd.netsmart.securitymanager.key;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * This is a helper Interface that manages the keystores, truststores and their respective keys and certificates.
 *
 * @author Jerry Dimitriou
 */
public interface KeyStoreManager {

    /**
     * Tries to fetch the default KeyPair from the default keystore, as it is configured in the
     * Configuration manager.
     *
     * @return the KeyPair that contains the default Private Key as it is configured in the
     * configuration manager
     * @throws SMgrException
     */
    KeyPair getDefaultPrivateKey() throws SMgrException;

    /**
     * Tries to fetch the default Certificate from the default keystore, as it is configured in the
     * Configuration manager.
     *
     * @return the Certificate that contains the default Public Key as it is configured in the
     * configuration manager
     * @throws SMgrException
     */
    Certificate getDefaultCertificate() throws SMgrException;

    /**
     * Tries to fetch the Private Key with alias <i>alias</i>, from the default keystore as it is configured in the
     * Configuration manager.
     *
     * @param alias    the Key Alias
     * @param password the private key password.
     * @return the Certificate that matches the alias
     * @throws SMgrException
     */
    KeyPair getPrivateKey(String alias, char[] password) throws SMgrException;

    Certificate getCertificate(String alias) throws SMgrException;

    KeyStore getKeyStore();

    KeyStore getTrustStore();
}
