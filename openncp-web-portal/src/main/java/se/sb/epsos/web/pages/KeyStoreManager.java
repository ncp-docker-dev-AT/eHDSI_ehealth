package se.sb.epsos.web.pages;

import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

public interface KeyStoreManager {

    KeyPair getPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException;

    X509Certificate getCertificate() throws KeyStoreException;
}
