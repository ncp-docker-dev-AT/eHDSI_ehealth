package eu.europa.ec.joinup.ecc.trilliumsecurityutils.saml;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Utility class, with multiple helper methods for assertion extraction and manipulation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class AssertionUtils {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionUtils.class);

    /**
     * Private constructor to avoid instantiation.
     */
    private AssertionUtils() {
    }

    /**
     * Extracts a TRC Assertion, from the Security Header of a given SOAP Header.
     *
     * @param soapHeader - full SOAP Header
     * @return extracted TRC Assertion
     */
    public static Assertion extractTrcAssertion(final Element soapHeader) {

        if (soapHeader == null) {
            LOGGER.error("SOAP Header element is null.");
            return null;
        }

        final NodeList assertionList;
        Element assertionElement;
        Assertion assertion;
        Assertion result = null;

        assertionList = extractAssertionLIst(soapHeader);

        if (assertionList == null || assertionList.getLength() == 0) {
            LOGGER.error("Assertion List is Empty.");
        } else {
            for (var i = 0; i < assertionList.getLength(); i++) {
                assertionElement = (Element) assertionList.item(i);
                try {
                    assertion = (Assertion) SAML.fromElement(assertionElement);
                    if (isTrcAssertion(assertion)) {
                        result = assertion;
                        break;
                    }
                } catch (UnmarshallingException ex) {
                    LOGGER.error("An error has occurred during the assertion extraction.", ex);
                }
            }
        }

        return result;
    }

    /**
     * Extracts a ID Assertion form the Security Header, present in the SOAP
     * Header.
     *
     * @param soapHeader - the full SOAP Header
     * @return
     */
    public static Assertion extractIdAssertion(final Element soapHeader) {

        if (soapHeader == null) {
            LOGGER.error("SOAP Header element is null.");
            return null;
        }

        final NodeList assertionList;
        Element assertionElement;
        Assertion assertion;
        Assertion result = null;

        assertionList = extractAssertionLIst(soapHeader);

        if (assertionList == null || assertionList.getLength() == 0) {
            LOGGER.error("Assertion List is Empty.");
        } else {
            for (var i = 0; i < assertionList.getLength(); i++) {
                assertionElement = (Element) assertionList.item(i);
                try {
                    assertion = (Assertion) SAML.fromElement(assertionElement);
                    if (isIdAssertion(assertion)) {
                        result = assertion;
                        break;
                    }
                } catch (UnmarshallingException ex) {
                    LOGGER.error("An error has occurred during the assertion extraction.", ex);
                }
            }
        }

        return result;
    }

    /**
     * This method extracts the assertion list from a full SOAP Header, the list can have the 0..* cardinality.
     *
     * @param soapHeader - the full SOAP Header.
     * @return the assertion list.
     */
    public static NodeList extractAssertionLIst(final Element soapHeader) {

        if (soapHeader == null) {
            LOGGER.error("SOAP Header element is null.");
            return null;
        }

        final var ASSERTIONS_NS = "urn:oasis:names:tc:SAML:2.0:assertion";
        final var ASSERTION = "Assertion";

        Element securityHeader;
        NodeList result = null;

        securityHeader = extractSecurityHeader(soapHeader);
        if (securityHeader != null) {
            result = securityHeader.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
        }

        return result;
    }

    /**
     * This method extracts the Security Header from the full SOAP Header.
     *
     * @param soapHeader - the full SOAP Header.
     * @return the Security Header.
     */
    public static Element extractSecurityHeader(final Element soapHeader) {

        if (soapHeader == null) {
            LOGGER.error("SOAP Header element is null.");
            return null;
        }
        Element result = null;
        NodeList securityList;

        securityList = soapHeader.getElementsByTagNameNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

        if (securityList.getLength() == 0) {
            LOGGER.error("Security Header is Missing.");
        } else {
            result = (Element) securityList.item(0);
        }
        return result;
    }

    public static String getRoleFromKPAssertion(final Assertion assertion) {

        return "doctor";
    }

    /**
     * This method checks if a given Assertion is an Identity Assertion.
     *
     * @param assertion - the Assertion to check.
     * @return a Boolean result, declaring if the assertion is an Identity assertion.
     */
    public static boolean isIdAssertion(final Assertion assertion) {

        if (assertion == null) {
            LOGGER.error("Provided Assertion is null.");
            return false;
        }

        return StringUtils.equals(assertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp");
    }

    /**
     * This method checks if a given Assertion is a Treatment Relationship Confirmation Assertion.
     *
     * @param assertion - the Assertion to check.
     * @return a Boolean result, declaring if the assertion is a Treatment Relationship Confirmation Assertion.
     */
    public static boolean isTrcAssertion(final Assertion assertion) {

        if (assertion == null) {
            LOGGER.error("Provided Assertion is null.");
            return false;
        }

        return StringUtils.equals(assertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:trc");
    }

    @SuppressWarnings("deprecation")
    public static void signSAMLAssertion(SignableSAMLObject as, String keyAlias, char[] keyPassword) throws Exception {

        String ncpSigKeystorePath = Constants.NCP_SIG_KEYSTORE_PATH;
        String ncpSigKeystorePassword = Constants.NCP_SIG_KEYSTORE_PASSWORD;
        String sigPrivateKeyAlias = Constants.NCP_SIG_PRIVATEKEY_ALIAS;
        String sigPrivatekeyPassword = Constants.NCP_SIG_PRIVATEKEY_PASSWORD;

        KeyStoreManager keyManager = new DefaultKeyStoreManager();
        X509Certificate cert;
        // check if we must use the default key
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        if (keyAlias == null) {
            cert = (X509Certificate) keyManager.getDefaultCertificate();
        } else {
            var keyStore = KeyStore.getInstance("JKS");
            var classLoader = Thread.currentThread().getContextClassLoader();
            var file = new File(ncpSigKeystorePath);
            keyStore.load(new FileInputStream(file), ncpSigKeystorePassword.toCharArray());
            privateKey = (PrivateKey) keyStore.getKey(sigPrivateKeyAlias, sigPrivatekeyPassword.toCharArray());
            X509Certificate cert1 = (X509Certificate) keyStore.getCertificate(sigPrivateKeyAlias);
            publicKey = cert1.getPublicKey();
            cert = (X509Certificate) keyManager.getCertificate(keyAlias);
        }

        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);
        Credential signingCredential = CredentialSupport.getSimpleCredential(cert, privateKey);

        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        signature.setCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
        var keyInfo = (KeyInfo) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        signature.setKeyInfo(keyInfo);
        as.setSignature(signature);
        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(as).marshall(as);
        } catch (MarshallingException e) {
            throw new SMgrException(e.getMessage(), e);
        }
        try {
            Signer.signObject(signature);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    public static String getDocumentAsXml(org.w3c.dom.Document doc, boolean header) {

        String resp = "";
        try {
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            String omit;
            if (header) {
                omit = "no";
            } else {
                omit = "yes";
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omit);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            // we want to pretty format the XML output
            // note : this is broken in jdk1.5 beta!
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            transformer.transform(domSource, streamResult);
            resp = stringWriter.toString();
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return resp;
    }

}
