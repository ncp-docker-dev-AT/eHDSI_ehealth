package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The NCP Signature Manager is a JAVA library for applying and verifying detached digital signatures on XML documents
 * and for applying and verifying enveloped signatures on SAML assertions and Audit Trail Messages
 *
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class SignatureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureManager.class);
    private KeyStoreManager keyManager;
    private String signatureAlgorithm;
    private String digestAlgorithm;

    public SignatureManager() {

        //Default constructor now defaults to the test keyStoreManager
        keyManager = new DefaultKeyStoreManager();
        init();
    }

    public SignatureManager(KeyStoreManager keyStoreManager) {

        //Constructor for unit testing. Sort of DI
        this.keyManager = keyStoreManager;
        init();
    }

    private void init() {

        signatureAlgorithm = CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256;
        digestAlgorithm = CryptographicConstant.ALGO_ID_DIGEST_SHA256;
    }

    /**
     * Verifies the enveloped SAML signature and checks that the Assertion is signed against that signature.
     * This method returns nothing when the signature is valid.
     * When the signature is not valid though, it throws an SMgrException with the Error Message that caused
     * the signature to fail validation.
     *
     * @param assertion The SAML Assertion that will be validated by the method.
     * @throws SMgrException When the validation of the signature fails
     */
    public String verifySAMLAssertion(Assertion assertion) throws SMgrException {

        String sigCountryCode = null;

        try {
            SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
            Signature sig = assertion.getSignature();
            try {
                profileValidator.validate(sig);
            } catch (SignatureException e) {
                // Indicates signature did not conform to SAML Signature profile
                LOGGER.error("ValidationException: '{}'", e.getMessage(), e);
                throw new SMgrException("SAML Signature Profile Validation: " + e.getMessage());
            }

            X509Certificate cert;
            //List<X509Certificate> certificates = KeyInfoHelper.getCertificates(sig.getKeyInfo());
            List<X509Certificate> certificates = KeyInfoSupport.getCertificates(sig.getKeyInfo());
            for (X509Certificate certificate : certificates) {
                LOGGER.info("Certificate: '{}'", certificate.getIssuerX500Principal().getName());
            }
            //List<X509Certificate> certificates = KeyInfoHelper.getCertificates(sig.getKeyInfo());
            if (certificates.size() == 1) {
                cert = certificates.get(0);
                // Mustafa: When not called through https, we can use the country code of the signature cert
                String certificateDN = cert.getSubjectDN().getName();
                sigCountryCode = certificateDN.substring(certificateDN.indexOf("C=") + 2, certificateDN.indexOf("C=") + 4);

            } else {
                throw new SMgrException("More than one certificate found in keyinfo");
            }

            BasicX509Credential verificationCredential = new BasicX509Credential(cert);
            //BasicX509Credential verificationCredential = new BasicX509Credential();
            //verificationCredential.setEntityCertificate(cert);
            //SignatureValidator sigValidator = new SignatureValidator(verificationCredential);
            LOGGER.info("[Security] SAML certificate validation");


            //verificationCredential.setEntityCertificate(cert);
            //SignatureValidator sigValidator = new SignatureValidator(verificationCredential);
            try {
                //sigValidator.validate(sig);
                SignatureValidator.validate(sig, verificationCredential);
            } catch (SignatureException e) {
                // Indicates signature was not cryptographically valid, or possibly a processing error
                LOGGER.error("SignatureException: '{}'", e.getMessage(), e);
                throw new SMgrException("Signature Validation: " + e.getMessage());
            }
            CertificateValidator cv = new CertificateValidator(keyManager.getTrustStore());
            cv.validateCertificate(cert);
        } catch (CertificateException ex) {
            LOGGER.error(null, ex);
        }

        return sigCountryCode;
    }

    /**
     * Verifies the enveloped XML signature and checks that the XML Document is signed against that signature.
     * This method returns nothing when the signature is valid. When the signature is not valid though,
     * it throws an SMgrException with the Error Message that caused the signature to fail validation.
     *
     * @param doc The XML Document that will be validated.
     * @throws SMgrException When the validation of the signature fails
     */
    public void verifyEnvelopedSignature(Document doc) throws SMgrException {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

            if (nl.getLength() == 0) {
                throw new SMgrException("Cannot find Signature element");
            } // and document context.

            CertificateValidator cv = new CertificateValidator(keyManager.getTrustStore());

            DOMValidateContext valContext = new DOMValidateContext(cv, nl.item(0));

            // Unmarshal the XMLSignature.
            XMLSignature signature = fac.unmarshalXMLSignature(valContext);

            // Validate the XMLSignature.
            boolean coreValidity = signature.validate(valContext);

            if (!coreValidity) {
                throw new SMgrException("Invalid Signature: Mathematical check failed");
            }

        } catch (XMLSignatureException | MarshalException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException("Signature Invalid: " + ex.getMessage(), ex);
        }

    }

    /**
     * Signs a Signable SAML Object using the private key with alias <i>keyAlias</i>.
     * Uses the OpenSAML2 library.
     *
     * @param as          The Signable SAML Object that is going to be signed. Usually a SAML Assertion
     * @param keyAlias    The NCP Trust Store Key Alias of the private key that will be used for signing.
     * @param keyPassword Password of the Signature certificate Key.
     * @throws SMgrException When signing fails
     * @see org.opensaml.saml.common.SignableSAMLObject
     */
    public void signSAMLAssertion(SignableSAMLObject as, String keyAlias, char[] keyPassword) throws SMgrException {

        KeyPair kp;
        X509Certificate cert;
        //check if we must use the default key
        if (keyAlias == null) {
            kp = keyManager.getDefaultPrivateKey();
            cert = (X509Certificate) keyManager.getDefaultCertificate();

        } else {
            kp = keyManager.getPrivateKey(keyAlias, keyPassword);
            cert = (X509Certificate) keyManager.getCertificate(keyAlias);
        }

        //Signature sig = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        Signature sig = (Signature) builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);

        //Credential signingCredential = SecurityHelper.getSimpleCredential(cert, kp.getPrivate());
        Credential signingCredential = CredentialSupport.getSimpleCredential(cert, kp.getPrivate());

        sig.setSigningCredential(signingCredential);
        sig.setSignatureAlgorithm(signatureAlgorithm);
        sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_WITH_COMMENTS);

//        BasicSecurityConfiguration securityConfiguration = (BasicSecurityConfiguration) Configuration.getGlobalSecurityConfiguration();
//        securityConfiguration.setSignatureReferenceDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256);
        org.opensaml.xmlsec.signature.KeyInfo keyInfo = (org.opensaml.xmlsec.signature.KeyInfo) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(org.opensaml.xmlsec.signature.KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(org.opensaml.xmlsec.signature.KeyInfo.DEFAULT_ELEMENT_NAME);
        org.opensaml.xmlsec.signature.X509Data data = (org.opensaml.xmlsec.signature.X509Data) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(org.opensaml.xmlsec.signature.X509Data.DEFAULT_ELEMENT_NAME).buildObject(org.opensaml.xmlsec.signature.X509Data.DEFAULT_ELEMENT_NAME);
        org.opensaml.xmlsec.signature.X509Certificate x509Certificate = (org.opensaml.xmlsec.signature.X509Certificate) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME).buildObject(org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME);

        String value;
        try {
            value = org.apache.xml.security.utils.Base64.encode(((BasicX509Credential) signingCredential).getEntityCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            LOGGER.error("CertificateEncodingException: '{}'", e.getMessage(), e);
            throw new SMgrException(e.getMessage(), e);
        }
        x509Certificate.setValue(value);
        data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(data);
        sig.setKeyInfo(keyInfo);

        as.setSignature(sig);
        try {
            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            marshallerFactory.getMarshaller(as).marshall(as);
        } catch (MarshallingException e) {
            LOGGER.error("MarshallingException: '{}'", e.getMessage(), e);
            throw new SMgrException(e.getMessage(), e);
        }
        try {
            Signer.signObject(sig);
        } catch (SignatureException ex) {
            throw new SMgrException(ex.getMessage(), ex);
        }
    }

    /**
     * Signs an XML document using the default private key as it is configured in the Configuration Manager.
     * Uses enveloped XML Signatures
     *
     * @param doc The Document that is going to be signed. be used for signing.
     * @throws SMgrException When signing fails
     */
    public void signXMLWithEnvelopedSig(Document doc) throws SMgrException {
        signXMLWithEnvelopedSig(doc, null, null);
    }

    /**
     * Signs an XML document using the private key with alias <i>keyAlias</i>.
     * Uses enveloped XML Signatures
     *
     * @param doc         The Document that is going to be signed.
     * @param keyAlias    The NCP Trust Store Key Alias of the private key that
     *                    will be used for signing.
     * @param keyPassword
     * @throws SMgrException When signing fails
     */
    public void signXMLWithEnvelopedSig(Document doc, String keyAlias, char[] keyPassword) throws SMgrException {

        KeyPair kp;
        X509Certificate cert;

        if (keyAlias == null) {
            kp = keyManager.getDefaultPrivateKey();
            cert = (X509Certificate) keyManager.getDefaultCertificate();
        } else {
            kp = keyManager.getPrivateKey(keyAlias, keyPassword);
            cert = (X509Certificate) keyManager.getCertificate(keyAlias);

        }

        try {
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

            Reference ref = fac.newReference("", fac.newDigestMethod(digestAlgorithm, null), Collections.singletonList(
                    fac.newTransform(Transform.ENVELOPED, (XMLStructure) null)), null, null);

            SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CryptographicConstant.ALGO_ID_C14N_EXCL_WITH_COMMENTS,
                    (C14NMethodParameterSpec) null), fac.newSignatureMethod(signatureAlgorithm, null),
                    Collections.singletonList(ref));

            KeyInfoFactory kif = fac.getKeyInfoFactory();

            List<Serializable> x509Content = new ArrayList<>();
            x509Content.add(cert.getSubjectX500Principal().getName(X500Principal.RFC1779));
            x509Content.add(cert);
            X509Data xd = kif.newX509Data(x509Content);

            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement());
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchAlgorithmException
                | InvalidAlgorithmParameterException | MarshalException | XMLSignatureException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException(ex.getMessage(), ex);
        }
    }

    /**
     * Signs a Signable SAML Object using the default key that is configured in the Configuration Manager.
     * Uses the OpenSAML2 library.
     *
     * @param trc The Signable SAML Object that is going to be signed. Usually a SAML Assertion.
     * @throws SMgrException When signing fails
     * @see org.opensaml.saml.common.SignableSAMLObject
     */
    public void signSAMLAssertion(Assertion trc) throws SMgrException {
        signSAMLAssertion(trc, null, null);
    }
}
