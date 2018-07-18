package epsos.ccd.netsmart.securitymanager.keyselector;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * A <code>KeySelector</code> that returns {@link PublicKey}s of trusted
 * {@link X509Certificate}s stored in a {@link KeyStore}.
 * <p>
 * <p>
 * This <code>KeySelector</code> uses the specified <code>KeyStore</code> to find a trusted <code>X509Certificate</code>
 * that matches information specified in the {@link KeyInfo} passed to the {@link #select} method.
 * The public key from the first match is returned. If no match, <code>null</code> is returned. See the <code>select</code> method for more information.
 *
 * @author Sean Mullan
 */
public class SMgrX509KeySelector extends KeySelector {

    private static boolean algEquals(String algURI, String algName) {

        return (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase("http://www.w3.org/2009/xmldsig11#dsa-sha256"))
                || (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"));
    }

    public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
                                    XMLCryptoContext context) throws KeySelectorException {

        for (Object o1 : keyInfo.getContent()) {

            XMLStructure info = (XMLStructure) o1;
            if (!(info instanceof X509Data)) {
                continue;
            }
            X509Data x509Data = (X509Data) info;
            for (Object o : x509Data.getContent()) {

                if (!(o instanceof X509Certificate)) {
                    continue;
                }
                final PublicKey key = ((X509Certificate) o).getPublicKey();
                // Make sure the algorithm is compatible with the method.
                if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                    return () -> key;
                }
            }
        }
        throw new KeySelectorException("No key found!");
    }
}
