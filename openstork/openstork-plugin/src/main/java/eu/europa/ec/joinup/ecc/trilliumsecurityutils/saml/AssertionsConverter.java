package eu.europa.ec.joinup.ecc.trilliumsecurityutils.saml;

import epsos.ccd.netsmart.securitymanager.SamlTRCIssuer;
import epsos.ccd.netsmart.securitymanager.SignatureManager;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.sts.client.TRCAssertionRequest;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionConstants;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionHelper;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.namespace.QName;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * This class contains several conversion methods to convert epSOS Specific SAML Assertions into KP exchange required assertions.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class AssertionsConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionsConverter.class);

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            LOGGER.error("InitializationException: '{}'", e.getMessage());
        }
    }

    private AssertionsConverter() {
    }

    private static <T> T create(Class<T> cls, QName qname) {

        return (T) (XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname)).buildObject(qname);
    }

    public static Assertion createPlainTRCA(String purpose, Assertion idAs, String patientId) throws SMgrException {

        var samlTRCIssuer = new SamlTRCIssuer();
        return samlTRCIssuer.issueTrcToken(idAs, patientId, purpose, null);
    }

    public static Assertion createTRCA(String purpose, Assertion idAs, String root, String extension) throws Exception {

        Assertion trc;
        LOGGER.debug("Try to create TRCA for patient : '{}'", extension);
        String patientId = extension + "^^^&" + root + "&ISO";
        LOGGER.info("TRCA Patient ID : '{}'", patientId);
        LOGGER.info("Assertion ID: '{}'", idAs.getID());
        LOGGER.info("SECMAN URL: '{}'", ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.sts.url"));
        TRCAssertionRequest req1 = new TRCAssertionRequest.Builder(idAs, patientId).purposeOfUse(purpose).build();
        trc = req1.request();
        LOGGER.debug("TRCA CREATED: '{}'", trc.getID());
        LOGGER.debug("TRCA WILL BE STORED TO SESSION: '{}'", trc.getID());
        return trc;
    }

    public static Assertion issueTrcToken(final Assertion hcpIdentityAssertion, String patientID, String purposeOfUse,
                                          List<Attribute> attrValuePair) throws SMgrException {

        KeyStoreManager ksm;
        HashMap<String, String> auditDataMap;

        ksm = new DefaultKeyStoreManager();
        auditDataMap = new HashMap<>();

        try {
            // Initializing the map
            auditDataMap.clear();
            XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

            // Doing an indirect copy so, because when cloning, signatures are lost.
            var signatureManager = new SignatureManager(ksm);

            try {
                signatureManager.verifySAMLAssertion(hcpIdentityAssertion);
            } catch (SMgrException ex) {
                throw new SMgrException("SAML Assertion Validation Failed: " + ex.getMessage());
            }
            if (hcpIdentityAssertion.getConditions().getNotBefore().isAfter(Instant.now())) {
                String msg = "Identity Assertion with ID " + hcpIdentityAssertion.getID() + " can't ne used before " + hcpIdentityAssertion.getConditions().getNotBefore();
                LOGGER.error("SMgrException: '{}'", msg);
                throw new SMgrException(msg);
            }
            if (hcpIdentityAssertion.getConditions().getNotOnOrAfter().isBefore(Instant.now())) {
                String msg = "Identity Assertion with ID " + hcpIdentityAssertion.getID() + " can't be used after " + hcpIdentityAssertion.getConditions().getNotOnOrAfter();
                LOGGER.error("SMgrException: '{}'", msg);
                throw new SMgrException(msg);
            }

            auditDataMap.put("hcpIdAssertionID", hcpIdentityAssertion.getID());

            // Create the assertion
            Assertion trc = create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
            if (patientID == null) {
                throw new SMgrException("Patient ID cannot be null");
            }
            auditDataMap.put("patientID", patientID);
            var issueInstant = Instant.now();

            trc.setIssueInstant(issueInstant);
            trc.setID("_" + UUID.randomUUID());
            auditDataMap.put("trcAssertionID", trc.getID());

            trc.setVersion(SAMLVersion.VERSION_20);

            // Create and add the Subject
            Subject subject = create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);

            trc.setSubject(subject);
            var issuer = new IssuerBuilder().buildObject();

            String confIssuer = ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.trc.endpoint");

            if (confIssuer.isEmpty()) {

                String countryCode = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
                confIssuer = "urn:initgw:" + countryCode + ":countryB";
                ConfigurationManagerFactory.getConfigurationManager().setProperty("secman.trc.endpoint", confIssuer);
            }
            issuer.setValue(confIssuer);
            trc.setIssuer(issuer);

            var nameid = getXspaSubjectFromAttributes(hcpIdentityAssertion.getAttributeStatements());
            trc.getSubject().setNameID(nameid);
            auditDataMap.put("humanRequestorNameID", hcpIdentityAssertion.getSubject().getNameID().getValue());
            auditDataMap.put("humanRequestorSubjectID", nameid.getValue());

            // Create and add Subject Confirmation
            SubjectConfirmation subjectConf = create(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
            subjectConf.setMethod(SubjectConfirmation.METHOD_SENDER_VOUCHES);
            trc.getSubject().getSubjectConfirmations().add(subjectConf);

            // Create and add conditions
            Conditions conditions = create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
            conditions.setNotBefore(issueInstant);
            conditions.setNotOnOrAfter(issueInstant.plus(Duration.ofHours(2)));
            trc.setConditions(conditions);

            //  Create and add Advice
            Advice advice = create(Advice.class, Advice.DEFAULT_ELEMENT_NAME);
            trc.setAdvice(advice);

            // Create and add AssertionIDRef
            AssertionIDRef aIdRef = create(AssertionIDRef.class, AssertionIDRef.DEFAULT_ELEMENT_NAME);
            aIdRef.setValue(hcpIdentityAssertion.getID());
            advice.getAssertionIDReferences().add(aIdRef);

            // Add and create the authentication statement
            AuthnStatement authStmt = create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
            authStmt.setAuthnInstant(issueInstant);
            trc.getAuthnStatements().add(authStmt);

            // Create and add AuthnContext
            AuthnContext authnContext = create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
            AuthnContextClassRef authnContextClassRef = create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
            authnContextClassRef.setURI(AuthnContext.PREVIOUS_SESSION_AUTHN_CTX);
            authnContext.setAuthnContextClassRef(authnContextClassRef);
            authStmt.setAuthnContext(authnContext);

            //  Create the Saml Attribute Statement
            AttributeStatement attrStmt = create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
            trc.getStatements().add(attrStmt);

            // Creating the Attribute that holds the Patient ID
            Attribute attrPID = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            attrPID.setFriendlyName("XSPA Subject");

            // TODO: Is there a constant for that urn??
            attrPID.setName("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
            attrPID.setNameFormat(Attribute.URI_REFERENCE);

            // Create and add the Attribute Value
            XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
            XSString attrValPID = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attrValPID.setValue(patientID);
            attrPID.getAttributeValues().add(attrValPID);
            attrStmt.getAttributes().add(attrPID);

            // Creating the Attribute that holds the Purpose of Use
            Attribute attrPoU = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            attrPoU.setFriendlyName("XSPA Purpose Of Use");

            // TODO: Is there a constant for that urn??
            attrPoU.setName("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
            attrPoU.setNameFormat(Attribute.URI_REFERENCE);
            if (purposeOfUse == null) {
                attrPoU = findStringInAttributeStatement(hcpIdentityAssertion.getAttributeStatements(),
                        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
                if (attrPoU == null) {
                    throw new SMgrException("Purpose of use not found in the assertion and is not passed as a parameter");
                }
            } else {
                XSString attrValPoU = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                attrValPoU.setValue(purposeOfUse);
                attrPoU.getAttributeValues().add(attrValPoU);
            }
            attrStmt.getAttributes().add(attrPoU);

            String poc = ((XSString) Objects.requireNonNull(findStringInAttributeStatement(hcpIdentityAssertion.getAttributeStatements(),
                    "urn:oasis:names:tc:xspa:1.0:subject:organization")).getAttributeValues().get(0)).getValue();

            LOGGER.info("Point of Care: {}", poc);
            auditDataMap.put("pointOfCare", poc);

            String pocId = ((XSString) Objects.requireNonNull(findURIInAttributeStatement(hcpIdentityAssertion.getAttributeStatements(),
                    "urn:oasis:names:tc:xspa:1.0:subject:organization-id")).getAttributeValues().get(0)).getValue();

            LOGGER.info("Point of Care: {}", poc);
            auditDataMap.put("pointOfCareID", pocId);

            String hrRole = ((XSString) Objects.requireNonNull(findStringInAttributeStatement(hcpIdentityAssertion.getAttributeStatements(),
                    "urn:oasis:names:tc:xacml:2.0:subject:role")).getAttributeValues().get(0)).getValue();

            LOGGER.info("HR Role {}", hrRole);
            auditDataMap.put("humanRequestorRole", hrRole);

            String facilityType = ((XSString) Objects.requireNonNull(findStringInAttributeStatement(hcpIdentityAssertion.getAttributeStatements(),
                    "urn:epsos:names:wp3.4:subject:healthcare-facility-type")).getAttributeValues().get(0)).getValue();

            LOGGER.info("Facility Type {}", facilityType);
            auditDataMap.put("facilityType", facilityType);

            signatureManager.signSAMLAssertion(trc);
            return trc;
        } catch (Exception ex) {
            throw new SMgrException(ex.getMessage());
        }
    }

    protected static Attribute findURIInAttributeStatement(List<AttributeStatement> statements, String attrName) {

        LOGGER.info("Size:{}", statements.size());
        for (AttributeStatement stmt : statements) {

            for (Attribute attribute : stmt.getAttributes()) {

                if (attribute.getName().equals(attrName)) {

                    LOGGER.info("Attribute Name:{}", attribute.getName());
                    Attribute attr = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);

                    attr.setFriendlyName(attribute.getFriendlyName());
                    attr.setName(attribute.getNameFormat());
                    attr.setNameFormat(attribute.getNameFormat());

                    XMLObjectBuilder uriBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
                    XSURI attrVal = (XSURI) uriBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSURI.TYPE_NAME);

                    attrVal.setURI(((XSURI) attribute.getAttributeValues().get(0)).getURI());
                    attr.getAttributeValues().add(attrVal);

                    return attr;
                }
            }
        }
        return null;
    }

    protected static NameID findProperNameID(Subject subject) {

        String format = subject.getNameID().getFormat();
        LOGGER.info("is email?: {}", format.equals(NameID.EMAIL));
        LOGGER.info("is x509 subject?: {}", format.equals(NameID.X509_SUBJECT));
        LOGGER.info("is Unspecified?: {}", format.equals(NameID.UNSPECIFIED));

        NameID nameID = create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat(format);
        nameID.setValue(subject.getNameID().getValue());
        return nameID;
    }

    private static Assertion createEpsosAssertion(String username, String role, String organization, String organizationId,
                                                  String facilityType, String purposeOfUse, String xspaLocality,
                                                  List<String> permissions) {
        // assertion
        Assertion assertion = null;
        try {

            XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

            // Create the NameIdentifier
            var nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
            var nameId = (NameID) nameIdBuilder.buildObject();
            nameId.setValue(username);
            nameId.setFormat(NameID.UNSPECIFIED);

            assertion = create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);

            String assId = "_" + UUID.randomUUID();
            assertion.setID(assId);
            assertion.setVersion(SAMLVersion.VERSION_20);
            var issueInstant = Instant.now();
            assertion.setIssueInstant(issueInstant);

            Subject subject = create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
            assertion.setSubject(subject);
            subject.setNameID(nameId);

            // Create and add Subject Confirmation
            SubjectConfirmation subjectConf = create(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
            subjectConf.setMethod(SubjectConfirmation.METHOD_SENDER_VOUCHES);
            assertion.getSubject().getSubjectConfirmations().add(subjectConf);

            // Create and add conditions
            Conditions conditions = create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
            conditions.setNotBefore(issueInstant);
            conditions.setNotOnOrAfter(issueInstant.plus(Duration.ofHours(4)));
            assertion.setConditions(conditions);

            String countryCode = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
            var issuer = new IssuerBuilder().buildObject();
            issuer.setValue("urn:idp:" + countryCode + ":countryB");
            issuer.setNameQualifier("urn:ehdsi:assertions:hcp");
            assertion.setIssuer(issuer);

            // Add and create the authentication statement
            AuthnStatement authStmt = create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
            authStmt.setAuthnInstant(issueInstant);
            assertion.getAuthnStatements().add(authStmt);

            // Create and add AuthnContext
            AuthnContext ac = create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
            AuthnContextClassRef authnContextClassRef = create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
            authnContextClassRef.setURI(AuthnContext.PASSWORD_AUTHN_CTX);
            ac.setAuthnContextClassRef(authnContextClassRef);
            authStmt.setAuthnContext(ac);

            AttributeStatement attrStmt = create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);

            // XSPA Subject
            var attrPID = createAttribute(builderFactory, "XSPA Subject",
                    "urn:oasis:names:tc:xspa:1.0:subject:subject-id", username, "", "");
            attrStmt.getAttributes().add(attrPID);
            // XSPA Role
            var attrPID_1 = createAttribute(builderFactory, "XSPA Role",
                    "urn:oasis:names:tc:xacml:2.0:subject:role", role, "", "");
            attrStmt.getAttributes().add(attrPID_1);
            // HITSP Clinical Speciality
            /*
             * Attribute attrPID_2 =
             * createAttribute(builderFactory,"HITSP Clinical Speciality",
             * "urn:epsos:names:wp3.4:subject:clinical-speciality",role,"","");
             * attrStmt.getAttributes().add(attrPID_2);
             */
            // XSPA Organization
            var attrPID_3 = createAttribute(builderFactory, "XSPA Organization",
                    "urn:oasis:names:tc:xspa:1.0:subject:organization", organization, "", "");
            attrStmt.getAttributes().add(attrPID_3);
            // XSPA Organization ID
            var attrPID_4 = createAttribute(builderFactory, "XSPA Organization ID",
                    "urn:oasis:names:tc:xspa:1.0:subject:organization-id", organizationId, "", "");
            attrStmt.getAttributes().add(attrPID_4);

            // // On behalf of
            // Attribute attrPID_4 =
            // createAttribute(builderFactory,"OnBehalfOf",
            // "urn:epsos:names:wp3.4:subject:on-behalf-of",organizationId,role,"");
            // attrStmt.getAttributes().add(attrPID_4);
            // eHealth DSI Healthcare Facility Type
            var attrPID_5 = createAttribute(builderFactory, "eHealth DSI Healthcare Facility Type",
                    "urn:epsos:names:wp3.4:subject:healthcare-facility-type", facilityType, "", "");
            attrStmt.getAttributes().add(attrPID_5);
            // XSPA Purpose of Use
            var attrPID_6 = createAttributePurposeOfUse(builderFactory, "XSPA Purpose Of Use",
                    "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", purposeOfUse, "", "");
            attrStmt.getAttributes().add(attrPID_6);
            // XSPA Locality
            var attrPID_7 = createAttribute(builderFactory, "XSPA Locality",
                    "urn:oasis:names:tc:xspa:1.0:environment:locality", xspaLocality, "", "");
            attrStmt.getAttributes().add(attrPID_7);
            // HL7 Permissions
            var attrPID_8 = createAttribute(builderFactory,
                    "Hl7 Permissions",
                    "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
            for (Object permission : permissions) {
                attrPID_8 = AddAttributeValue(builderFactory, attrPID_8, permission.toString(), "", "");
            }
            attrStmt.getAttributes().add(attrPID_8);

            assertion.getStatements().add(attrStmt);

        } catch (Exception e) {
            LOGGER.error("ConfigurationException: '{}'", e.getMessage(), e);
        }
        return assertion;
    }

    public static Assertion convertIdAssertion(Assertion epsosHcpAssertion, PatientId patientId) {

        if (epsosHcpAssertion == null) {
            LOGGER.error("Provided Assertion is null.");
            return null;
        }

        final var X509_SUBJECT_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName";

        Assertion result;

        final HCPIAssertionBuilder assertionBuilder;
        final String hcpRole;
        final String hcpId;
        final String hcpSpecialty;
        final String orgId;
        final String orgName;
        final String healthCareFacilityType;
        final String purposeOfUse;
        final String pointOfCare;
        final List<String> permissions;

        LOGGER.info("Converting Assertion.");

        // Initialize Assertions Builder with minimum initial parameters.
        assertionBuilder = new HCPIAssertionBuilder("UID=medical doctor", X509_SUBJECT_FORMAT, "sender-vouches")
                .issuer("O=European HCP,L=Europe,ST=Europe,C=EU", X509_SUBJECT_FORMAT)
                .audienceRestrictions("https://ihe.connecthaton.XUA/X-ServiceProvider-IHE-Connectathons")
                .notOnOrAfter(4);

        // MANDATORY: HCP ID and HCP Role
        try {
            hcpRole = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
            hcpId = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_SUBJECT_ID);
            assertionBuilder.hcpIdentifier(hcpId).hcpRole(hcpRole);

        } catch (MissingFieldException ex) {
            LOGGER.error("One or more required attributes were not found in the original assertion: '{}'", ex.getMessage(), ex);
            return null;
        }

        // OPTIONAL: HCP Specialty
        try {
            hcpSpecialty = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, "urn:epsos:names:wp3.4:subject:clinical-speciality");
            assertionBuilder.hcpSpecialty(hcpSpecialty);

        } catch (MissingFieldException ex) {
            LOGGER.info("Optional attribute not found, proceeding with conversion (HCP Specialty).", ex);
        }

        // Required: HCP Organization ID and HCP Organization Name
        try {
            orgId = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, "urn:oasis:names:tc:xspa:1.0:subject:organization-id");
            orgName = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, "urn:oasis:names:tc:xspa:1.0:subject:organization");
            assertionBuilder.healthCareProfessionalOrganisation(orgId, orgName);

        } catch (MissingFieldException ex) {
            LOGGER.error("Required attribute not found, proceeding with conversion ( HCP Organization ID and HCP Organization Name.", ex);
        }

        // MANDATORY: HealthCare Facility Type
        try {
            healthCareFacilityType = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, "urn:epsos:names:wp3.4:subject:healthcare-facility-type");
            assertionBuilder.healthCareFacilityType(healthCareFacilityType);

        } catch (MissingFieldException ex) {
            LOGGER.error("One or more required attributes were not found in the original assertion", ex);
            return null;
        }

        // MANDATORY: Purpose of Use
        try {
            purposeOfUse = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_PURPOSEOFUSE);
            assertionBuilder.purposeOfUse(purposeOfUse);

        } catch (MissingFieldException ex) {
            LOGGER.error("One or more required attributes were not found in the original assertion", ex);
            return null;

        }

        // MANDATORY: Point Of Care
        try {
            pointOfCare = AssertionHelper.getAttributeFromAssertion(epsosHcpAssertion, "urn:oasis:names:tc:xspa:1.0:environment:locality");
            assertionBuilder.pointOfCare(pointOfCare);

        } catch (MissingFieldException ex) {
            LOGGER.error("One or more required attributes were not found in the original assertion", ex);
            return null;
        }

        // MANDATORY: Patient ID (For eHealth Exchange)
        if (patientId != null && patientId.getFullId() != null && !patientId.getFullId().isEmpty()) {
            assertionBuilder.patientId(patientId.getFullId());

        } else {
            LOGGER.error("One or more required attributes were not found (Patient Id).");
            return null;
        }
        // MANDATORY: Home Community Id (For eHealth Exchange)
        if (Constants.HOME_COMM_ID != null && !Constants.HOME_COMM_ID.isEmpty()) {
            assertionBuilder.homeCommunityId(Constants.HOME_COMM_ID);

        } else {
            LOGGER.error("One or more required attributes were not found (Home Community Id).");
            return null;
        }

        //  OPTIONAL (0..*): Permissions
        try {
            permissions = convertPermissions(AssertionHelper.getPermissionValuesFromAssertion(epsosHcpAssertion));
            assertionBuilder.permissions(permissions);

        } catch (InsufficientRightsException ex) {
            LOGGER.error("An Insufficient Rights error was found while extracting permissions from the assertion.", ex);
            return null;
        }

        // BUILD Assertion
        result = assertionBuilder.build();

        return result;
    }

    public static Assertion convertTrcAssertion(Assertion epsosHcpAssertion) {

        if (epsosHcpAssertion == null) {
            LOGGER.error("Provided Assertion is null.");
            return null;
        }
        final Assertion result;

        LOGGER.info("Conversion not implemented: mirroring TRC Assertion");
        result = epsosHcpAssertion;

        return result;
    }

    private static List<String> convertPermissions(final List<XMLObject> permissions) {

        if (permissions == null || permissions.isEmpty()) {
            LOGGER.error("Provided list is null or empty.");
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();

        for (XMLObject permission : permissions) {
            result.add(permission.getDOM().getTextContent());
        }

        return result;
    }

    private static Attribute createAttribute(XMLObjectBuilderFactory builderFactory, String friendlyName, String oasisName) {

        Attribute attrPID = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attrPID.setFriendlyName(friendlyName);
        attrPID.setName(oasisName);
        attrPID.setNameFormat(Attribute.URI_REFERENCE);

        return attrPID;
    }

    private static Attribute AddAttributeValue(XMLObjectBuilderFactory builderFactory, Attribute attribute, String value,
                                               String namespace, String xmlschema) {

        XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
        XSString attrValPID = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrValPID.setValue(value);
        attribute.getAttributeValues().add(attrValPID);

        return attribute;
    }

    private static Attribute createAttribute(XMLObjectBuilderFactory builderFactory, String FriendlyName, String oasisName,
                                             String value, String namespace, String xmlschema) {

        Attribute attrPID = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attrPID.setFriendlyName(FriendlyName);
        attrPID.setName(oasisName);
        attrPID.setNameFormat(Attribute.URI_REFERENCE);
        // Create and add the Attribute Value

        XMLObjectBuilder stringBuilder;

        if (namespace.equals("")) {

            XSString attrValPID;
            stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
            attrValPID = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attrValPID.setValue(value);
            attrPID.getAttributeValues().add(attrValPID);
        } else {

            XSURI attrValPID;
            stringBuilder = builderFactory.getBuilder(XSURI.TYPE_NAME);
            attrValPID = (XSURI) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSURI.TYPE_NAME);
            attrValPID.setURI(value);
            attrPID.getAttributeValues().add(attrValPID);
        }

        return attrPID;
    }

    private static Attribute createAttributePurposeOfUse(XMLObjectBuilderFactory builderFactory, String FriendlyName, String oasisName,
                                             String value, String namespace, String xmlschema) {

        Attribute attrPID = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attrPID.setFriendlyName(FriendlyName);
        attrPID.setName(oasisName);
        attrPID.setNameFormat(Attribute.URI_REFERENCE);
        // Create and add the Attribute Value

        XMLObjectBuilder stringBuilder;

        if (namespace.equals("")) {

            XSString attrValPID;
            stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
            attrValPID = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attrValPID.setValue(value);
            attrPID.getAttributeValues().add(attrValPID);
        } else {

            XSURI attrValPID;
            stringBuilder = builderFactory.getBuilder(XSURI.TYPE_NAME);
            attrValPID = (XSURI) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSURI.TYPE_NAME);
            attrValPID.setURI(value);
            attrPID.getAttributeValues().add(attrValPID);
        }

        return attrPID;
    }


    protected static Attribute findStringInAttributeStatement(List<AttributeStatement> statements, String attrName) {

        LOGGER.info("Size:{}", statements.size());

        for (AttributeStatement stmt : statements) {
            for (Attribute attribute : stmt.getAttributes()) {
                if (attribute.getName().equals(attrName)) {

                    LOGGER.info("Attribute Name: {}", attribute.getName());
                    Attribute attr = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
                    attr.setFriendlyName(attribute.getFriendlyName());
                    attr.setName(attribute.getNameFormat());
                    attr.setNameFormat(attribute.getNameFormat());

                    XMLObjectBuilder stringBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
                    XSString attrVal = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                    attrVal.setValue(((XSString) attribute.getAttributeValues().get(0)).getValue());
                    attr.getAttributeValues().add(attrVal);

                    return attr;
                }
            }
        }
        return null;
    }

    protected static NameID getXspaSubjectFromAttributes(List<AttributeStatement> attributeStatementList) {

        var xspaSubjectAttribute = findStringInAttributeStatement(attributeStatementList, "urn:oasis:names:tc:xspa:1.0:subject:subject-id");

        NameID nameID = create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat(NameID.UNSPECIFIED);
        if (xspaSubjectAttribute != null) {
            nameID.setValue(((XSString) xspaSubjectAttribute.getAttributeValues().get(0)).getValue());
        }

        return nameID;
    }
}
