package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class SignatureValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureValidator.class);

    public static void validateSignature(Element sigPointer) throws Exception {

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Create a DOMValidateContext and specify a KeySelector and document context.
        DOMValidateContext valContext = new DOMValidateContext(new SignatureValidator.X509KeySelector(), sigPointer);
        valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        // Unmarshal the XMLSignature.
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);
        // Validate the XMLSignature.
        boolean coreValidity = signature.validate(valContext);

        // Check core validation status.
        if (!coreValidity) {
            printErrorDetails(valContext, signature);
            throw new RuntimeException("+++ Signature not valild +++");
        } else {
            LOGGER.debug("+++ Signature passed core validation +++");
        }
    }

    private static void printErrorDetails(DOMValidateContext valContext, XMLSignature signature) throws XMLSignatureException {

        LOGGER.debug("Signature failed core validation");
        boolean sv = signature.getSignatureValue().validate(valContext);
        LOGGER.debug("signature validation status: '{}'", sv);
        if (!sv) {
            // Check the validation status of each Reference.
            Iterator i1 = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i1.hasNext(); j++) {
                boolean refValid = ((Reference) i1.next()).validate(valContext);
                LOGGER.debug("ref[{}] validity status: '{}'", j, refValid);
            }
        }
    }

    private static class X509KeySelector extends KeySelector {

        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method,
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

        boolean algEquals(String algURI, String algName) {

            return (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase("http://www.w3.org/2009/xmldsig11#dsa-sha256"))
                    || (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"));
        }
    }
}
