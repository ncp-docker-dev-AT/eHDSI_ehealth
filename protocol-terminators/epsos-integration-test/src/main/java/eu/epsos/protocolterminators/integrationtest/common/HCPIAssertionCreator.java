package eu.epsos.protocolterminators.integrationtest.common;

import eu.epsos.exceptions.InvalidInput;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.PurposeOfUse;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML;
import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HCPIAssertionCreator {

    private static final Logger LOG = LoggerFactory.getLogger(HCPIAssertionCreator.class);

    private HCPIAssertionCreator() {
    }

    public static Assertion createHCPIAssertion(XSPARoleDeprecated role) {
        List<String> permissions = new ArrayList<>();
        permissions.add("4");
        permissions.add("6");
        permissions.add("10");
        permissions.add("46");

        return createHCPIAssertion(permissions, role);
    }

    public static Assertion createHCPIAssertion(List<String> permissions, XSPARoleDeprecated role) {
        if (permissions == null) {
            throw new InvalidInput("permissions == null");
        }
        XMLSignatureFactory factory;
        KeyStore keyStore;
        KeyPair keyPair;
        KeyInfo keyInfo;

        var saml = new SAML();
        var subject = saml.createSubject(role.toString().toLowerCase(), "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "sender-vouches");

        // Create assertion
        var assertion = saml.createAssertion(subject);
        var issuer = saml.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue("urn:tiani-spirit:sts");
        assertion.setIssuer(issuer);

        // Set version
        SAMLVersion version = SAMLVersion.VERSION_20;
        assertion.setVersion(version);

        // Set Conditions/AudienceRestriction
        var audienceRestrictionBuilder = new AudienceRestrictionBuilder();
        var audienceRestriction = audienceRestrictionBuilder.buildObject();
        var audienceBuilder = new AudienceBuilder();
        var audience = audienceBuilder.buildObject();
        audience.setURI("https://ihe.connecthaton.XUA/X-ServiceProvider-IHE-Connectathon");
        audienceRestriction.getAudiences().add(audience);
        assertion.getConditions().getAudienceRestrictions().add(audienceRestriction);

        // Set AuthnStatement
        var issueInstant = Instant.now();
        var authnStatement = saml.create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnInstant(issueInstant);
        authnStatement.setSessionNotOnOrAfter(issueInstant.plus(Duration.ofHours(2)));

        // Set AuthnStatement/AuthnContext
        var authnContext = saml.create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        var authnContextClassRef = saml.create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setURI("urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);

        // Create AttributeStatement
        var attributeStatement = saml.create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);

        // Namespaces
        var ns1 = new Namespace(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs");
        var ns2 = new Namespace(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");

        // Set HCP Identifier
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Subject");
            att.setName("urn:oasis:names:tc:xspa:1.0:subject:subject-id");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent(role.toString().toLowerCase() + " the");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Structural Role of the HCP
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Role");
            att.setName("urn:oasis:names:tc:xacml:2.0:subject:role");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent(role.toString());

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Speciality of the HCP
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("HITSP Clinical Speciality");
            att.setName("urn:epsos:names:wp3.4:subject:clinical-speciality");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("UNKNOWN");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Healthcare Professional Organisation
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Organization");
            att.setName("urn:oasis:names:tc:xspa:1.0:subject:organization");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("MemberState");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Healthcare Professional Organisation ID
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Organization Id");
            att.setName("urn:oasis:names:tc:xspa:1.0:subject:organization-id");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("urn:oid:1.2.3.4.5.6.7");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Type of HCPO
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("eHealth DSI Healthcare Facility Type");
            att.setName("urn:ehdsi:names:subject:healthcare-facility-type");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("Hospital");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Purpose of Use
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Purpose of Use");
            att.setName("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent(PurposeOfUse.TREATMENT.toString());

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Point of Care
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA Locality");
            att.setName("urn:oasis:names:tc:xspa:1.0:environment:locality");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("Vienna, Austria");

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set Permissions acc. to the legislation of the country of care
        if (true) {
            Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            att.setFriendlyName("XSPA permissions according with Hl7");
            att.setName("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
            att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

            for (var i = 3; i < 47; i++) {
                if (permissions.contains(String.valueOf(i))) {

                    XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
                    XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);

                    if (i == 10 || i == 16 || i == 32 || i == 33) {
                        attVal.setTextContent("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission:PRD-0" + i);
                    } else {
                        attVal.setTextContent("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission:PRD-00" + i);
                    }

                    attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
                    attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
                    QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
                    attVal.getUnknownAttributes().put(attributeName, "xs:string");

                    att.getAttributeValues().add(attVal);
                    attributeStatement.getAttributes().add(att);
                }
            }

            //add PPD-046 (Record Medication Administration Record) needed by security manager for XDR ED submit
            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission:PPD-046");
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");
            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);

            //add PPD-032 (New Consents and Authorizations) needed for Consent submit.
            builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
            attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
            attVal.setTextContent("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission:PPD-032");
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");
            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }

        // Set AttributeStatement
        assertion.getAttributeStatements().add(attributeStatement);

        // Set Signature
        try {
            // Set assertion.DOM
            var marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion);
            Element element = marshaller.marshall(assertion);
            assertion.setDOM(element);

            // Set factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            factory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).getDeclaredConstructor().newInstance());

            // Set keyStore
            try (var fileInputStream = new FileInputStream(Constants.SC_KEYSTORE_PATH)) {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(fileInputStream, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
            }

            var passwordProtection = new PasswordProtection(Constants.SC_PRIVATEKEY_PASSWORD.toCharArray());
            PrivateKeyEntry entry = (PrivateKeyEntry) keyStore.getEntry(Constants.SC_PRIVATEKEY_ALIAS, passwordProtection);

            // Set keyPair
            keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());

            // Set keyInfo
            var keyInfoFactory = factory.getKeyInfoFactory();
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyInfoFactory.newX509Data(Collections.singletonList(entry.getCertificate()))));

            // Create Signature/SignedInfo/Reference
            List<Transform> lst = new ArrayList<>();
            lst.add(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
            lst.add(factory.newTransform(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (TransformParameterSpec) null));
            var reference = factory.newReference("#" + assertion.getID(), factory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null), lst, null, null);

            // Set Signature/SignedInfo
            var signedInfo = factory.newSignedInfo(factory.newCanonicalizationMethod(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null), Collections.singletonList(reference));

            // Sign Assertion
            var xmlSignature = factory.newXMLSignature(signedInfo, keyInfo);
            var domSignContext = new DOMSignContext(keyPair.getPrivate(), assertion.getDOM());
            xmlSignature.sign(domSignContext);
        } catch (Exception e) {
            LOG.error("Signature element not created! '{}'", e.getLocalizedMessage(), e);
        }

        // Set Signature's place
        org.w3c.dom.Node signatureElement = assertion.getDOM().getLastChild();

        var foundIssuer = false;
        Node elementAfterIssuer = null;
        NodeList children = assertion.getDOM().getChildNodes();
        for (var c = 0; c < children.getLength(); ++c) {
            org.w3c.dom.Node child = children.item(c);

            if (foundIssuer) {
                elementAfterIssuer = child;
                break;
            }

            if (child.getNodeType() == Node.ELEMENT_NODE && StringUtils.equals(child.getLocalName(), "Issuer")) {
                foundIssuer = true;
            }
        }

        // Place after the Issuer, or as first element if no Issuer:
        if (!foundIssuer || elementAfterIssuer != null) {
            assertion.getDOM().removeChild(signatureElement);
            assertion.getDOM().insertBefore(signatureElement, foundIssuer ? elementAfterIssuer : assertion.getDOM().getFirstChild());
        }

        return assertion;
    }
}
