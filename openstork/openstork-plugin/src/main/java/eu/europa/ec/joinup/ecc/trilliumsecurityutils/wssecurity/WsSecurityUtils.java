package eu.europa.ec.joinup.ecc.trilliumsecurityutils.wssecurity;

import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.util.XMLUtils;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.xerces.dom.DocumentImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.soap.wssecurity.KeyIdentifier;
import org.opensaml.soap.wssecurity.SecurityTokenReference;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.impl.KeyInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.util.Constants;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class with helper methods for WS-Security
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class WsSecurityUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsSecurityUtils.class);

    private WsSecurityUtils() {
    }

    public static Element timeStampBuilder() {

        final int TIME_TO_LIVE = 300; // 300 seconds = 5 minutes
        final WSSecTimestamp timeStampBuilder;
        final Element result;

        timeStampBuilder = new WSSecTimestamp();
        timeStampBuilder.setTimeToLive(TIME_TO_LIVE);
        timeStampBuilder.prepare(new DocumentImpl());
        result = timeStampBuilder.getElement();

        return result;
    }

    public static KeyInfo keyInfoSecurityTokenRefBuilder(final String assertionId) {

        final KeyInfoBuilder keyInfoBuilder;
        final KeyInfo keyInfo;

        keyInfoBuilder = new KeyInfoBuilder();
        keyInfo = keyInfoBuilder.buildObject();

        SecurityTokenReference securityTokenReference = new SAML().create(SecurityTokenReference.class, SecurityTokenReference.ELEMENT_NAME);
        securityTokenReference.getUnknownAttributes().put(new QName("TokenType"), "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");

        KeyIdentifier keyIdentifier = new SAML().create(KeyIdentifier.class, KeyIdentifier.ELEMENT_NAME);
        keyIdentifier.setValueType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID");
        keyIdentifier.setValue(assertionId);

        securityTokenReference.getUnknownXMLObjects().add(keyIdentifier);

        keyInfo.getXMLObjects().add(securityTokenReference);

        return keyInfo;

    }

    public static Element signSecurityHeader(SOAPHeaderBlock securityHeader, Assertion assertion) {

        Element result = null;
        final Element securityTokeReference;

        try (FileInputStream is = new FileInputStream(Constants.SC_KEYSTORE_PATH.split(Constants.EPSOS_PROPS_PATH)[0])) {
            XMLSignatureFactory factory;
            KeyStore keyStore;
            KeyPair keyPair;

            // Set factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            factory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

            // Set keyStore
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
            KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(Constants.SC_PRIVATEKEY_PASSWORD.toCharArray());
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(Constants.SC_PRIVATEKEY_ALIAS, pp);

            // Set keyPair
            keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());

            // Create Signature/SignedInfo/Reference
            List<Transform> lst = new ArrayList<>();
            lst.add(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
            lst.add(factory.newTransform(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (TransformParameterSpec) null));
            Reference ref = factory.newReference("", factory.newDigestMethod(CryptographicConstant
                    .ALGO_ID_DIGEST_SHA256, null), lst, null, null);

            SignedInfo signedInfo = factory.newSignedInfo(factory.newCanonicalizationMethod(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null), Collections.singletonList(ref));

            // Sign Assertion
            DOMSignContext signContext = new DOMSignContext(keyPair.getPrivate(), XMLUtils.toDOM(securityHeader));
            XMLSignature signature = factory.newXMLSignature(signedInfo, null);
            signature.sign(signContext);

            result = (Element) signContext.getParent().getLastChild();

            securityTokeReference = SAML.addToElement(keyInfoSecurityTokenRefBuilder(assertion.getID()), (Element) signContext.getParent().getLastChild());

            result.appendChild(securityTokeReference);

        } catch (Exception ex) {
            LOGGER.error("An error has occurred during the SECURITY HEADER signing process.", ex);
        }
        return result;
    }
}
