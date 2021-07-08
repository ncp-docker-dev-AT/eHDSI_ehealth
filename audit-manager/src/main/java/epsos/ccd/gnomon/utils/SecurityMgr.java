package epsos.ccd.gnomon.utils;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Helper methods for signing documents and extraction of certificates.
 *
 * @author Kostas Karkaletsis
 */
public class SecurityMgr {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityMgr.class);

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_NAME;
    private static final String KEY_STORE_PASSWORD;
    private static final String PRIVATE_KEY_PASSWORD;
    private static final String PRIVATE_KEY_ALIAS;

    static {

        KEY_STORE_NAME = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PATH");
        KEY_STORE_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PASSWORD");
        PRIVATE_KEY_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_PASSWORD");
        PRIVATE_KEY_ALIAS = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_ALIAS");
    }

    private SecurityMgr() {
    }

    public static String getSignedDocumentAsString(Document doc) {

        ByteArrayOutputStream byteArrayOutputStream;
        var signed = "";
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer trans = factory.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(byteArrayOutputStream));
            signed = byteArrayOutputStream.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return signed;
    }

    /**
     * This method signs the input Document
     *
     * @param doc the document we want to be signed
     * @return the same document signed
     */
    public static Document signDocumentEnveloped(Document doc) {

        try {

            // prepare signature factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            final var xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).getDeclaredConstructor().newInstance());

            Node sigParent = doc.getDocumentElement();
            List<Transform> transforms = Collections.singletonList(xmlSignatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            // Retrieve signing key
            var keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            try (var stream = new FileInputStream(KEY_STORE_NAME)) {

                keyStore.load(stream, KEY_STORE_PASSWORD.toCharArray());
                var privateKey = (PrivateKey) keyStore.getKey(PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASSWORD.toCharArray());
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(PRIVATE_KEY_ALIAS);
                var publicKey = cert.getPublicKey();

                // Create a Reference to the enveloped document
                var reference = xmlSignatureFactory.newReference("",
                        xmlSignatureFactory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null),
                        transforms, null, null);

                // Create the SignedInfo
                var signedInfo = xmlSignatureFactory.newSignedInfo(xmlSignatureFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null),
                        xmlSignatureFactory.newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null),
                        Collections.singletonList(reference));

                // Create a KeyValue containing the RSA PublicKey
                var keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
                var keyValue = keyInfoFactory.newKeyValue(publicKey);

                // Create a KeyInfo and add the KeyValue to it
                var keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

                // Create a DOMSignContext and specify the RSA PrivateKey and location of the resulting XMLSignature's
                // parent element
                var domSignContext = new DOMSignContext(privateKey, sigParent);

                // Create the XMLSignature (but don't sign it yet)
                var signature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);

                // Marshal, generate (and sign) the enveloped signature
                signature.sign(domSignContext);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return doc;
    }

    public static String signDocumentDetached(String inputFile) {

        ByteArrayOutputStream byteArrayOutputStream;
        var signed = "";
        try {

            // Instantiate the document to be signed
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            Document doc = dbFactory.newDocumentBuilder().newDocument();

            // prepare signature factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            final var xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).getDeclaredConstructor().newInstance());

            List<Transform> transforms;
            transforms = Collections.singletonList(xmlSignatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            // Retrieve signing key
            var keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            try (var stream = new FileInputStream(KEY_STORE_NAME)) {
                keyStore.load(stream, KEY_STORE_PASSWORD.toCharArray());

                var privateKey = (PrivateKey) keyStore.getKey(PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASSWORD.toCharArray());

                X509Certificate cert = (X509Certificate) keyStore.getCertificate(PRIVATE_KEY_ALIAS);
                var publicKey = cert.getPublicKey();

                // Create a Reference to the enveloped document
                var reference = xmlSignatureFactory.newReference("",
                        xmlSignatureFactory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null),
                        transforms, null, null);

                // Create the SignedInfo
                var signedInfo = xmlSignatureFactory.newSignedInfo(xmlSignatureFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null), xmlSignatureFactory
                        .newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null), Collections.singletonList(reference));

                xmlSignatureFactory.newSignedInfo(xmlSignatureFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null), xmlSignatureFactory
                        .newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_DSA_SHA256, null), Collections.singletonList(reference));

                // Create a KeyValue containing the RSA PublicKey
                var keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
                var keyValue = keyInfoFactory.newKeyValue(publicKey);

                // Create a KeyInfo and add the KeyValue to it
                var keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

                // Create a DOMSignContext and specify the RSA PrivateKey and
                // location of the resulting XMLSignature's parent element
                new DOMSignContext(privateKey, doc);

                // Create the XMLSignature (but don't sign it yet)
                var signature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);

                // Create the Document that will hold the resulting XMLSignature

                // Create a DOMSignContext and set the signing Key to the DSA
                // PrivateKey and specify where the XMLSignature should be inserted
                // in the target document (in this case, the document root)
                var signContext = new DOMSignContext(privateKey, doc);

                // Marshal, generate (and sign) the enveloped signature
                signature.sign(signContext);

                // output the resulting document
                byteArrayOutputStream = new ByteArrayOutputStream();
                TransformerFactory factory = TransformerFactory.newInstance();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                Transformer transformer = factory.newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(byteArrayOutputStream));
                signed = byteArrayOutputStream.toString();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return signed;
    }
}
