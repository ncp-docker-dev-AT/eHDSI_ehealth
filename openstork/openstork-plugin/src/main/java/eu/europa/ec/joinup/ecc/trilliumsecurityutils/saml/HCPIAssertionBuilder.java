package eu.europa.ec.joinup.ecc.trilliumsecurityutils.saml;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML;
import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
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
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {Insert Class Description Here}
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class HCPIAssertionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HCPIAssertionBuilder.class);

    private final Subject subject;
    private final SAML saml;
    private final Assertion assertion;
    private final AttributeStatement attributeStatement;
    private final Namespace ns1;
    private final Namespace ns2;
    private XMLSignatureFactory factory;
    private KeyStore keyStore;
    private KeyPair keyPair;
    private KeyInfo keyInfo;

    public HCPIAssertionBuilder(final String subjectUsername, final String subjectFormat, final String subjectConfirmationMethod) {
        saml = new SAML();
        // Subject
        subject = saml.createSubject(subjectUsername, subjectFormat, subjectConfirmationMethod);
        // Assertion structure
        assertion = saml.createAssertion(subject);

        // FIXED ATTRIBUTES
        // Version
        SAMLVersion version = SAMLVersion.VERSION_20;
        assertion.setVersion(version);
        // Create AttributeStatement
        attributeStatement = saml.create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);

        // Namespaces
        ns1 = new Namespace(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs");
        ns2 = new Namespace(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
    }

    public HCPIAssertionBuilder issuer(final String value, final String format) {

        var issuer = saml.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(value);
        issuer.setFormat(format);
        assertion.setIssuer(issuer);
        return this;
    }

    public HCPIAssertionBuilder audienceRestrictions(final String uri) {

        var audienceRestrictionBuilder = new AudienceRestrictionBuilder();
        var audienceRestriction = audienceRestrictionBuilder.buildObject();
        var audienceBuilder = new AudienceBuilder();
        var audience = audienceBuilder.buildObject();
        audience.setURI(uri);
        audienceRestriction.getAudiences().add(audience);
        assertion.getConditions().getAudienceRestrictions().add(audienceRestriction);
        return this;
    }

    public HCPIAssertionBuilder notOnOrAfter(int numberOfHours) {

        // Set AuthnStatement
        var issueInstant = Instant.now();
        var authnStatement = saml.create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnInstant(issueInstant);
        authnStatement.setSessionNotOnOrAfter(issueInstant.plus(Duration.ofHours(numberOfHours)));
        // Set AuthnStatement/AuthnContext
        var authnContext = saml.create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        var authnContextClassRef = saml.create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setURI("urn:oasis:names:tc:SAML:2.0:ac:classes:X509");
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);
        return this;
    }

    public HCPIAssertionBuilder hcpIdentifier(final String hcpIdentifier) {

        var attribute = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setFriendlyName("XSPA Subject");
        attribute.setName("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(hcpIdentifier);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        attribute.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(attribute);
        return this;
    }

    public HCPIAssertionBuilder hcpRole(final String hcpRole) {

        var attribute = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setFriendlyName("XSPA Role");
        attribute.setName("urn:oasis:names:tc:xacml:2.0:subject:role");
        attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(hcpRole);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        attribute.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(attribute);
        return this;
    }

    public HCPIAssertionBuilder hcpSpecialty(final String hcpSpecialty) {

        Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("HITSP Clinical Speciality");
        att.setName("urn:epsos:names:wp3.4:subject:clinical-speciality");
        att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(hcpSpecialty);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        att.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(att);
        return this;
    }

    public HCPIAssertionBuilder healthCareProfessionalOrganisation(final String hcpOrgId, final String hcpOrgName) {

        Attribute att;
        XMLObjectBuilder<?> builder;
        XSAny attVal;
        QName attributeName;

        // Set Healthcare Professional Organisation Name
        att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("XSPA Organization");
        att.setName("urn:oasis:names:tc:xspa:1.0:subject:organization");
        att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(hcpOrgName);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        att.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(att);

        // Set Healthcare Professional Organisation ID
        att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("XSPA Organization Id");
        att.setName("urn:oasis:names:tc:xspa:1.0:subject:organization-id");
        att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(hcpOrgId);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:anyURI");

        att.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(att);

        return this;
    }

    public HCPIAssertionBuilder healthCareFacilityType(final String HealthCareFacilityType) {

        // Set Type of HCPO
        var attribute = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setFriendlyName("eHealth DSI Healthcare Facility Type");
        attribute.setName("urn:epsos:names:wp3.4:subject:healthcare-facility-type");
        attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(HealthCareFacilityType);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        attribute.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(attribute);

        return this;
    }

    public HCPIAssertionBuilder purposeOfUse(final String purposeOfUse) {

        Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("XSPA Purpose of Use");
        att.setName("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
        att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent(purposeOfUse);

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        att.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(att);
        return this;
    }

    public HCPIAssertionBuilder pointOfCare(final String pointOfCare) {

        Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("XSPA Locality");
        att.setName("urn:oasis:names:tc:xspa:1.0:environment:locality");

        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attVal.setTextContent("Aveiro, Portugal");

        attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        attVal.getUnknownAttributes().put(attributeName, "xs:string");

        att.getAttributeValues().add(attVal);
        attributeStatement.getAttributes().add(att);
        return this;
    }

    public HCPIAssertionBuilder onBehalfOf(final String role, final String representeeId) {

        Attribute attribute = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setFriendlyName("OnBehalfOf");
        attribute.setName("urn:epsos:names:wp3.4:subject:on-behalf-of");
        attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        XMLObjectBuilder<?> resourceIdBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        // Add Representee  Role
        XSAny resourceIdVal = (XSAny) resourceIdBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        resourceIdVal.setTextContent(role);

        resourceIdVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        resourceIdVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName resourceIdributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        resourceIdVal.getUnknownAttributes().put(resourceIdributeName, "xs:string");

        attribute.getAttributeValues().add(resourceIdVal);

        // Add Representee Id
        XSAny representeeIdVal = (XSAny) resourceIdBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        representeeIdVal.setTextContent(representeeId);

        representeeIdVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        representeeIdVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        resourceIdributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        representeeIdVal.getUnknownAttributes().put(resourceIdributeName, "xs:string");

        attribute.getAttributeValues().add(representeeIdVal);

        // Add information to Assertion (global) attribute statement
        attributeStatement.getAttributes().add(attribute);

        return this;
    }

    public HCPIAssertionBuilder permissions(final List<String> permissions) {
        Attribute att = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        att.setFriendlyName("XSPA permissions according with Hl7");
        att.setName("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
        att.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");

        for (String s : permissions) {
            XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

            XSAny attVal = (XSAny) builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);

            attVal.setTextContent("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission:" + s);

            attVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
            attVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
            QName attributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
            attVal.getUnknownAttributes().put(attributeName, "xs:string");

            att.getAttributeValues().add(attVal);
            attributeStatement.getAttributes().add(att);
        }
        return this;
    }

    public HCPIAssertionBuilder patientId(final String patientId) {

        Attribute resourceId = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        resourceId.setFriendlyName("Resource Id");
        resourceId.setName("urn:oasis:names:tc:xacml:2.0:resource:resource-id");

        XMLObjectBuilder<?> resourceIdBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny resourceIdVal = (XSAny) resourceIdBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        resourceIdVal.setTextContent(patientId);

        resourceIdVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        resourceIdVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName resourceIdributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        resourceIdVal.getUnknownAttributes().put(resourceIdributeName, "xs:string");

        resourceId.getAttributeValues().add(resourceIdVal);
        attributeStatement.getAttributes().add(resourceId);
        return this;
    }

    public HCPIAssertionBuilder homeCommunityId(final String homeCommunityId) {

        Attribute homeCommunityIdAttr = saml.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        homeCommunityIdAttr.setFriendlyName("Home Community Id");
        homeCommunityIdAttr.setName("urn:nhin:names:saml:homeCommunityId");

        XMLObjectBuilder<?> homeCommunityIdBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);

        XSAny homeCommunityIdVal = (XSAny) homeCommunityIdBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        homeCommunityIdVal.setTextContent(homeCommunityId);

        homeCommunityIdVal.getNamespaceManager().registerNamespaceDeclaration(ns1);
        homeCommunityIdVal.getNamespaceManager().registerNamespaceDeclaration(ns2);
        QName homeCommunityIdributeName = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", "xsi");
        homeCommunityIdVal.getUnknownAttributes().put(homeCommunityIdributeName, "xs:string");

        homeCommunityIdAttr.getAttributeValues().add(homeCommunityIdVal);
        attributeStatement.getAttributes().add(homeCommunityIdAttr);
        return this;
    }

    // Build Assertion
    public Assertion build() {

        // Set AttributeStatement
        assertion.getAttributeStatements().add(attributeStatement);

        // Set Signature
        try {
            // Set assertion.DOM
            var marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion);
            Element elem = marshaller.marshall(assertion);
            assertion.setDOM(elem);

            // Set factory
            String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            factory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).getDeclaredConstructor().newInstance());

            try (var fileInputStream = new FileInputStream(Constants.SC_KEYSTORE_PATH.split(Constants.EPSOS_PROPS_PATH)[0])) {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(fileInputStream, Constants.SC_KEYSTORE_PASSWORD.toCharArray());
            }

            var passwordProtection = new KeyStore.PasswordProtection(Constants.SC_PRIVATEKEY_PASSWORD.toCharArray());
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(Constants.SC_PRIVATEKEY_ALIAS, passwordProtection);

            // Set keyPair
            keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());

            // Set keyInfo
            KeyInfoFactory kFactory = factory.getKeyInfoFactory();
            keyInfo = kFactory.newKeyInfo(Collections.singletonList(kFactory.newX509Data(Collections.singletonList(entry.getCertificate()))));

            // Create Signature/SignedInfo/Reference
            List<Transform> lst = new ArrayList<>();
            lst.add(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
            lst.add(factory.newTransform(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (TransformParameterSpec) null));
            Reference ref = factory.newReference("#" + assertion.getID(), factory.newDigestMethod(CryptographicConstant.ALGO_ID_DIGEST_SHA256, null), lst, null, null);

            // Set Signature/SignedInfo
            SignedInfo signedInfo = factory.newSignedInfo(factory.newCanonicalizationMethod(CryptographicConstant.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(CryptographicConstant.ALGO_ID_SIGNATURE_RSA_SHA256, null), Collections.singletonList(ref));

            // Sign Assertion
            XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
            DOMSignContext signContext = new DOMSignContext(keyPair.getPrivate(), assertion.getDOM());
            signature.sign(signContext);
        } catch (MarshallingException | ClassNotFoundException | InstantiationException | IllegalAccessException
                | KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
                | UnrecoverableEntryException | InvalidAlgorithmParameterException | MarshalException
                | XMLSignatureException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Signature element not created: '{}'", e.getLocalizedMessage(), e);
        }

        // Set Signature's place
        Node signatureElement = assertion.getDOM().getLastChild();

        var foundIssuer = false;
        Node elementAfterIssuer = null;
        NodeList children = assertion.getDOM().getChildNodes();
        for (var c = 0; c < children.getLength(); ++c) {
            Node child = children.item(c);

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
