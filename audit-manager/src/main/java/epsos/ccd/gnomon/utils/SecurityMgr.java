package epsos.ccd.gnomon.utils;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Helper methods for signing documents and extraction of certificates
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */

public class SecurityMgr {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityMgr.class);

    private static String KEY_STORE_TYPE = "JKS";
    private static String KEY_STORE_NAME = "C://mesa//test_keystore_server1.jks";
    private static String KEY_STORE_PASS = "spirit";
    private static String PRIVATE_KEY_PASS = "spirit";
    private static String KEY_ALIAS = "server1";

    private SecurityMgr() {
    }

    public static void init_variables() {

        KEY_STORE_NAME = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PATH");
        KEY_STORE_PASS = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PASSWORD");
        PRIVATE_KEY_PASS = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_PASSWORD");
        KEY_ALIAS = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_ALIAS");
    }

    public static String getSignedDocumentAsString(Document doc) {

        ByteArrayOutputStream bas;
        String signed = "";
        try {
            bas = new ByteArrayOutputStream();
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer trans = factory.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(bas));
            signed = bas.toString();
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
            init_variables();

            // prepare signature factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");

            final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).newInstance());

            Node sigParent;
            List<Transform> transforms;

            sigParent = doc.getDocumentElement();
            transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            // Retrieve signing key
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            try (FileInputStream stream = new FileInputStream(KEY_STORE_NAME)) {
                keyStore.load(stream, KEY_STORE_PASS.toCharArray());

                PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, PRIVATE_KEY_PASS.toCharArray());

                X509Certificate cert = (X509Certificate) keyStore.getCertificate(KEY_ALIAS);
                PublicKey publicKey = cert.getPublicKey();

                // Create a Reference to the enveloped document
                Reference ref = sigFactory.newReference("", sigFactory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null),
                        transforms, null, null);

                // Create the SignedInfo
                SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null),
                        sigFactory.newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null),
                        Collections.singletonList(ref));

                // Create a KeyValue containing the RSA PublicKey
                KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
                KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

                // Create a KeyInfo and add the KeyValue to it
                KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

                // Create a DOMSignContext and specify the RSA PrivateKey and location of the resulting XMLSignature's
                // parent element
                DOMSignContext dsc = new DOMSignContext(privateKey, sigParent);

                // Create the XMLSignature (but don't sign it yet)
                XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);

                // Marshal, generate (and sign) the enveloped signature
                signature.sign(dsc);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return doc;
    }

    public static String signDocumentDetached(String inputFile) {

        ByteArrayOutputStream bas;
        String signed = "";
        try {
            init_variables();
            // Instantiate the document to be signed
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            Document doc = dbFactory.newDocumentBuilder().newDocument();

            // prepare signature factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).newInstance());

            List<Transform> transforms;
            transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            // Retrieve signing key
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            try (FileInputStream stream = new FileInputStream(KEY_STORE_NAME)) {
                keyStore.load(stream, KEY_STORE_PASS.toCharArray());

                PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, PRIVATE_KEY_PASS.toCharArray());

                X509Certificate cert = (X509Certificate) keyStore.getCertificate(KEY_ALIAS);
                PublicKey publicKey = cert.getPublicKey();

                // Create a Reference to the enveloped document
                Reference ref = sigFactory.newReference("", sigFactory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null),
                        transforms, null, null);

                // Create the SignedInfo
                SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null), sigFactory
                        .newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null), Collections.singletonList(ref));

                sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(
                        CryptographicConstant.ALGO_ID_C14N_INCL_WITH_COMMENTS, (C14NMethodParameterSpec) null), sigFactory
                        .newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_DSA_SHA256, null), Collections.singletonList(ref));

                // Create a KeyValue containing the RSA PublicKey
                KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
                KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

                // Create a KeyInfo and add the KeyValue to it
                KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

                // Create a DOMSignContext and specify the RSA PrivateKey and
                // location of the resulting XMLSignature's parent element
                new DOMSignContext(privateKey, doc);

                // Create the XMLSignature (but don't sign it yet)
                XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);

                // Create the Document that will hold the resulting XMLSignature

                // Create a DOMSignContext and set the signing Key to the DSA
                // PrivateKey and specify where the XMLSignature should be inserted
                // in the target document (in this case, the document root)
                DOMSignContext signContext = new DOMSignContext(privateKey, doc);

                // Marshal, generate (and sign) the enveloped signature
                signature.sign(signContext);

                // output the resulting document
                bas = new ByteArrayOutputStream();
                TransformerFactory factory = TransformerFactory.newInstance();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                Transformer trans = factory.newTransformer();
                trans.transform(new DOMSource(doc), new StreamResult(bas));
                signed = bas.toString();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return signed;
    }
}
