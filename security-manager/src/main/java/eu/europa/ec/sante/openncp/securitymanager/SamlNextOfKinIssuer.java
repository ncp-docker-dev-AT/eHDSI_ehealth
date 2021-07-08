package eu.europa.ec.sante.openncp.securitymanager;

import epsos.ccd.netsmart.securitymanager.SignatureManager;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SamlNextOfKinIssuer {

    private final Logger logger = LoggerFactory.getLogger(SamlNextOfKinIssuer.class);
    KeyStoreManager keyStoreManager;
    HashMap<String, String> auditDataMap;

    public SamlNextOfKinIssuer() {

        keyStoreManager = new DefaultKeyStoreManager();
        auditDataMap = new HashMap<>();
    }

    /**
     * @param keyStoreManager
     */
    public SamlNextOfKinIssuer(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager;
    }

    /**
     * Helper Function that makes it easy to create a new OpenSAML Object, using the default namespace prefixes.
     *
     * @param <T>   The Type of OpenSAML Class that will be created
     * @param cls   the openSAML Class
     * @param qname The QName of the Represented XML element.
     * @return the new OpenSAML object of type T
     */
    public static <T> T create(Class<T> cls, QName qname) {
        return (T) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname).buildObject(qname);
    }

    public Assertion issueNextOfKinToken(final Assertion hcpIdentityAssertion, String doctorId, String idaReference,
                                         List<Attribute> attrValuePair) throws SMgrException {

        // Initializing the Map
        auditDataMap.clear();
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        //  Doing an indirect copy so, because when cloning, signatures are lost.
        var signatureManager = new SignatureManager(keyStoreManager);

        try {
            signatureManager.verifySAMLAssertion(hcpIdentityAssertion);
        } catch (SMgrException ex) {
            throw new SMgrException("SAML Assertion Validation Failed: " + ex.getMessage());
        }
        var issuanceInstant = Instant.now();
        logger.info("Assertion validity: '{}' - '{}'", hcpIdentityAssertion.getConditions().getNotBefore(),
                hcpIdentityAssertion.getConditions().getNotOnOrAfter());
        if (hcpIdentityAssertion.getConditions().getNotBefore().isAfter(issuanceInstant)) {
            String msg = "Identity Assertion with ID " + hcpIdentityAssertion.getID() + " can't be used before " +
                    hcpIdentityAssertion.getConditions().getNotBefore() + ". Current UTC time is " + issuanceInstant;
            logger.error("SecurityManagerException: '{}'", msg);
            throw new SMgrException(msg);
        }
        if (hcpIdentityAssertion.getConditions().getNotOnOrAfter().isBefore(issuanceInstant)) {
            String msg = "Identity Assertion with ID " + hcpIdentityAssertion.getID() + " can't be used after " +
                    hcpIdentityAssertion.getConditions().getNotOnOrAfter() + ". Current UTC time is " + issuanceInstant;
            logger.error("SecurityManagerException: '{}'", msg);
            throw new SMgrException(msg);
        }

        auditDataMap.put("hcpIdAssertionID", hcpIdentityAssertion.getID());

        // Create the assertion
        Assertion assertion = create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setIssueInstant(issuanceInstant);
        assertion.setID("_" + UUID.randomUUID());
        assertion.setVersion(SAMLVersion.VERSION_20);

        // Create and add the Subject
        Subject subject = create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        assertion.setSubject(subject);
        var issuer = new IssuerBuilder().buildObject();
        String countryCode = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
        String confIssuer = "urn:initgw:" + countryCode + ":countryB";
        issuer.setValue(confIssuer);
        issuer.setNameQualifier("urn:ehdsi:assertions:nok");
        assertion.setIssuer(issuer);

        NameID nameid = create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameid.setFormat(NameID.UNSPECIFIED);
        nameid.setValue(doctorId);

        assertion.getSubject().setNameID(nameid);

        //Create and add Subject Confirmation
        SubjectConfirmation subjectConf = create(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConf.setMethod(SubjectConfirmation.METHOD_SENDER_VOUCHES);
        assertion.getSubject().getSubjectConfirmations().add(subjectConf);

        //Create and add conditions
        Conditions conditions = create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(issuanceInstant);
        conditions.setNotOnOrAfter(issuanceInstant.plus(Duration.ofHours(2)));
        assertion.setConditions(conditions);

        //Create and add Advice
        Advice advice = create(Advice.class, Advice.DEFAULT_ELEMENT_NAME);
        assertion.setAdvice(advice);

        //Create and add AssertionIDRef
        AssertionIDRef aIdRef = create(AssertionIDRef.class, AssertionIDRef.DEFAULT_ELEMENT_NAME);
        aIdRef.setValue(idaReference);
        advice.getAssertionIDReferences().add(aIdRef);

        //Add and create the authentication statement
        AuthnStatement authStmt = create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authStmt.setAuthnInstant(issuanceInstant);
        assertion.getAuthnStatements().add(authStmt);

        //Create and add AuthnContext
        AuthnContext ac = create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setURI(AuthnContext.PREVIOUS_SESSION_AUTHN_CTX);
        ac.setAuthnContextClassRef(authnContextClassRef);
        authStmt.setAuthnContext(ac);

        // Create the Saml Attribute Statement
        AttributeStatement attrStmt = create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
        assertion.getStatements().add(attrStmt);

        //Creating the Attribute that holds the Patient ID
//        Attribute attrPID = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
//        attrPID.setFriendlyName("XSPA Subject");
//        attrPID.setName("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
//        attrPID.setNameFormat(Attribute.URI_REFERENCE);

        //Create and add the Attribute Value
        // var stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
//        XSString attrValPID = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
//        attrValPID.setValue(patientID);
//        attrPID.getAttributeValues().add(attrValPID);
//        attrStmt.getAttributes().add(attrPID);

        // Set Next of Kin attributes:
//        Attribute attributeNextOfKinId = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
//        attributeNextOfKinId.setFriendlyName("XSPA Purpose Of Use");
//
//        attributeNextOfKinId.setName("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
//        attributeNextOfKinId.setNameFormat(Attribute.URI_REFERENCE);
//        if (nextOfKinId == null) {
//            attributeNextOfKinId = SamlIssuerHelper.createAttribute(nextOfKinId, "Purpose Of Use", Attribute.NAME_FORMAT_ATTRIB_NAME,
//                    "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
//            if (attributeNextOfKinId == null) {
//                throw new SMgrException("Purpose of use not found in the assertion and is not passed as a parameter");
//            }
//        } else {
//            XSString attrValNextOfKinId = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
//            attrValNextOfKinId.setValue(nextOfKinId);
//            attributeNextOfKinId.getAttributeValues().add(attrValNextOfKinId);
//        }
        // attrStmt.getAttributes().add(attributeNextOfKinId);
        for (Attribute attribute : attrValuePair) {
            attrStmt.getAttributes().add(attribute);
        }

        signatureManager.signSAMLAssertion(assertion);
        if (logger.isDebugEnabled()) {
            logger.debug("Assertion generated at '{}'", assertion.getIssueInstant().toString());
        }

        return assertion;
    }

    /**
     * @return
     */
    public String getPointOfCare() {

        return auditDataMap.get("pointOfCare");
    }

    /**
     * @return
     */
    public String getPointOfCareID() {

        return auditDataMap.get("pointOfCareID");
    }

    /**
     * @return
     */
    public String getHumanRequestorNameId() {

        return auditDataMap.get("humanRequestorNameID");
    }

    /**
     * @return
     */
    public String getHumanRequestorSubjectId() {

        return auditDataMap.get("humanRequestorSubjectID");
    }

    /**
     * @return
     */
    public String getHRRole() {

        return auditDataMap.get("humanRequestorRole");
    }

    public String getFunctionalRole() {

        return auditDataMap.get("humanRequesterFunctionalRole");
    }

    /**
     * @return
     */
    public String getFacilityType() {

        return auditDataMap.get("facilityType");
    }
}
