package epsos.ccd.gnomon.configmanager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

/**
 * This code was copied&pasted, apologizes for not refactoring.
 * <p>
 * This methods validates a signature. Note that in the setKeySaved, we should
 * authenticate the key TODO TODO TODO
 *
 * @author max
 */
public class SMPSignatureValidator {

    private final Logger logger = LoggerFactory.getLogger(SMPSignatureValidator.class);

    private X509Certificate savedKey;

    private static final Object LOCK_1 = new Object() {
    };

    private static final Object LOCK_2 = new Object() {
    };

    public void validateSignature(Element sigPointer) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Create a DOMValidateContext and specify a KeySelector and document
        // context.
        DOMValidateContext valContext = new DOMValidateContext(new SMPSignatureValidator.X509KeySelector(), sigPointer);

        valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        // Unmarshal the XMLSignature.
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);
        // Validate the XMLSignature.
        boolean coreValidity = signature.validate(valContext);

        for (Object o : signature.getSignedInfo().getReferences()) {
            InputStream is = ((Reference) o).getDigestInputStream();
            logger.info(IOUtils.toString(is, StandardCharsets.UTF_8));
        }

        // Check core validation status.
        if (!coreValidity) {
            printErrorDetails(valContext, signature);
            throw new RuntimeException("+++ Signature not valild +++");
        } else {
            logger.info("+++ Signature passed core validation +++");
        }

    }

    private void printErrorDetails(DOMValidateContext valContext, XMLSignature signature) throws XMLSignatureException {
        logger.info("Signature failed core validation");
        boolean sv = signature.getSignatureValue().validate(valContext);
        logger.info("signature validation status: {}", sv);
        if (!sv) {
            // Check the validation status of each Reference.
            Iterator i1 = signature.getSignedInfo().getReferences().iterator();
            int j = 0;
            while (i1.hasNext()) {
                boolean refValid = ((Reference) i1.next()).validate(valContext);
                logger.info("ref[{}] validity status: {}", j, refValid);
                j++;
            }
        }
    }

    public X509Certificate getKeySaved() {
        synchronized (LOCK_2) {
            return savedKey;
        }
    }

    private void setKeySaved(X509Certificate key) {
        synchronized (LOCK_1) {
            this.savedKey = key;
        }
    }

    private class X509KeySelector extends KeySelector {

        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method,
                                        XMLCryptoContext context) throws KeySelectorException {

            for (Object o1 : keyInfo.getContent()) {
                XMLStructure info = (XMLStructure) o1;
                if (!(info instanceof X509Data))
                    continue;
                X509Data x509Data = (X509Data) info;
                for (Object o : x509Data.getContent()) {
                    if (!(o instanceof X509Certificate))
                        continue;
                    final PublicKey key = ((X509Certificate) o).getPublicKey();
                    setKeySaved((X509Certificate) o);
                    // Make sure the algorithm is compatible
                    // with the method.
                    if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                        return () -> key;
                    }
                }
            }
            throw new KeySelectorException(
                    "No key found! Either the namespaces are wrong, or there is a algorithm which is not sha 256.");
        }

        // Massi + Jerome + Joao 13/4/2017. Allow for RSA_SHA1 signatures
        // because of the gazelle CA
        boolean algEquals(String algURI, String algName) {
            return isRsa(algURI, algName) || isDsa(algURI, algName);
        }

        private boolean isRsa(String algUri, String algName) {
            return "RSA".equalsIgnoreCase(algName) &&
                    (SignatureMethod.RSA_SHA1.equalsIgnoreCase(algUri) ||
                            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".equalsIgnoreCase(algUri));
        }

        private boolean isDsa(String algUri, String algName) {
            return "DSA".equalsIgnoreCase(algName) && SignatureMethod.DSA_SHA1.equalsIgnoreCase(algUri);
        }
    }
}