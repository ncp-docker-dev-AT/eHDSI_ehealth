package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml;

import epsos.ccd.netsmart.securitymanager.SignatureManager;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.PolicyAssertionManager;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.XSDValidationException;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.common.xml.SAMLSchemaBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SAML2Validator {

    private static final ServiceLoader<PolicyAssertionManager> serviceLoader = ServiceLoader.load(PolicyAssertionManager.class);
    private static final String OASIS_WSSE_SCHEMA_LOC = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final Logger LOGGER = LoggerFactory.getLogger(SAML2Validator.class);
    private static PolicyAssertionManager policyManager;

    static {
        try {
            LOGGER.info("Loading National implementation of PolicyManagerInterface...");
            policyManager = serviceLoader.iterator().next();
            LOGGER.info("Successfully loaded PolicyManager");
        } catch (Exception e) {
            LOGGER.error("Failed to load implementation of PolicyManagerInterface: " + e.getMessage(), e);
        }
    }

    private SAML2Validator() {
    }

    public static String validateXCPDHeader(Element soapHeader) throws MissingFieldException, InsufficientRightsException,
            InvalidFieldException, XSDValidationException, SMgrException {

        LOGGER.debug("[SAML] Validating XCPD Header.");
        String sigCountryCode = null;

        NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");
        Element security;
        if (securityList.getLength() > 0) {
            security = (Element) securityList.item(0);
        } else {
            throw (new MissingFieldException("Security element is required."));
        }

        NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
        Element hcpAss;
        Assertion hcpAssertion = null;
        try {
            if (assertionList.getLength() > 0) {
                for (var i = 0; i < assertionList.getLength(); i++) {
                    hcpAss = (Element) assertionList.item(i);
                    // Validate Assertion according to SAML XSD
                    var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                    schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(hcpAss));

                    hcpAssertion = (Assertion) SAML.fromElement(hcpAss);
                    if (org.apache.commons.lang.StringUtils.equals(hcpAssertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp")) {
                        break;
                    }
                }
            }
            if (hcpAssertion == null) {
                throw (new MissingFieldException(EhdsiErrorCode.EHDSI_ERROR_HPI_AUTHENTICATION_NOT_RECEIVED, "HCP Assertion element is required."));
            }

            sigCountryCode = checkHCPAssertion(hcpAssertion, null);
            //TODO: Next of Kin assertion should be checked
            policyManager.XCPDPermissionValidator(hcpAssertion);

        } catch (IOException | UnmarshallingException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage());
        } catch (SAXException e) {
            throw new XSDValidationException(e.getMessage());
        }

        return sigCountryCode;
    }

    public static String validateXCAHeader(Element soapHeader, String classCode) throws InsufficientRightsException,
            MissingFieldException, InvalidFieldException, SMgrException {

        LOGGER.debug("[SAML] Validating XCA Header.");
        String sigCountryCode;

        try {
            // Since the XCA Simulator sends this value wrong, we are trying it as follows for now
            NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");
            Element security;
            if (securityList.getLength() > 0) {
                security = (Element) securityList.item(0);
            } else {
                throw (new MissingFieldException("Security element is required."));
            }

            NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
            Element assertionElement;
            Assertion hcpAssertion = null;
            Assertion trcAssertion = null;

            if (assertionList.getLength() > 0) {
                for (var i = 0; i < assertionList.getLength(); i++) {
                    assertionElement = (Element) assertionList.item(i);
                    if (assertionElement.getAttribute("ID").startsWith("urn:uuid:")) {
                        LOGGER.debug("ncname found!!!");
                        assertionElement.setAttribute("ID", "_" + assertionElement.getAttribute("ID").substring(9));
                    }
                    if (assertionElement.getAttribute("ID").startsWith("urn:uuid:")) {
                        LOGGER.debug("ncname still exist!!!");
                    } else {
                        LOGGER.debug("ncname fixed!!!");
                    }

                    // Validate Assertion according to SAML XSD
                    var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                    schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(assertionElement));
                    var anAssertion = (Assertion) SAML.fromElement(assertionElement);
                    if (org.apache.commons.lang.StringUtils.equals(anAssertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp")) {
                        hcpAssertion = (Assertion) SAML.fromElement(assertionElement);
                    } else {
                        trcAssertion = (Assertion) SAML.fromElement(assertionElement);
                    }
                }
            }
            if (hcpAssertion == null) {
                throw (new MissingFieldException("HCP Assertion element is required."));
            }
            if (trcAssertion == null) {
                throw (new MissingFieldException("TRC Assertion element is required."));
            }

            sigCountryCode = checkHCPAssertion(hcpAssertion, classCode);
            policyManager.XCAPermissionValidator(hcpAssertion, classCode);
            checkTRCAssertion(trcAssertion, classCode);
            checkTRCAdviceIdReferenceAgainstHCPId(trcAssertion, hcpAssertion);
            //TODO: Next of Kin assertion should be checked
        } catch (IOException | UnmarshallingException | SAXException e) {
            LOGGER.error("", e);
            throw new InsufficientRightsException();
        }

        return sigCountryCode;
    }

    public static String validateXDRHeader(Element soapHeader, String classCode) throws InsufficientRightsException,
            MissingFieldException, InvalidFieldException, SMgrException {

        LOGGER.debug("[SAML] Validating XDR Header.");
        String sigCountryCode;

        try {
            NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");
            Element security;
            if (securityList.getLength() > 0) {
                security = (Element) securityList.item(0);
            } else {
                throw (new MissingFieldException("Security element is required."));
            }

            NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
            Element assertionElement;
            Assertion hcpAssertion = null;
            Assertion trcAssertion = null;

            if (assertionList.getLength() > 0) {
                for (var i = 0; i < assertionList.getLength(); i++) {
                    assertionElement = (Element) assertionList.item(i);
                    if (assertionElement.getAttribute("ID").startsWith("urn:uuid:")) {
                        LOGGER.debug("ncname found!!!");
                        assertionElement.setAttribute("ID", "_" + assertionElement.getAttribute("ID").substring(9));
                    }
                    if (assertionElement.getAttribute("ID").startsWith("urn:uuid:")) {
                        LOGGER.debug("ncname still exist!!!");
                    } else {
                        LOGGER.debug("ncname fixed!!!");
                    }

                    // Validate Assertion according to SAML XSD
                    var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                    schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(assertionElement));
                    var anAssertion = (Assertion) SAML.fromElement(assertionElement);
                    if (org.apache.commons.lang.StringUtils.equals(anAssertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp")) {
                        hcpAssertion = (Assertion) SAML.fromElement(assertionElement);
                    } else {
                        trcAssertion = (Assertion) SAML.fromElement(assertionElement);
                    }
                }
            }
            if (hcpAssertion == null) {
                throw (new MissingFieldException("HCP Assertion element is required."));
            }
            if (trcAssertion == null) {
                throw (new MissingFieldException("TRC Assertion element is required."));
            }

            sigCountryCode = checkHCPAssertion(hcpAssertion, classCode);
            policyManager.XDRPermissionValidator(hcpAssertion, classCode);
            checkTRCAssertion(trcAssertion, classCode);
            checkTRCAdviceIdReferenceAgainstHCPId(trcAssertion, hcpAssertion);
            //TODO: Next of Kin assertion should be checked
        } catch (IOException | UnmarshallingException | SAXException e) {
            LOGGER.error("", e);
            throw new InsufficientRightsException();
        }

        return sigCountryCode;
    }

    private static void checkTRCAdviceIdReferenceAgainstHCPId(Assertion trcAssertion, Assertion hcpAssertion) throws InsufficientRightsException {

        try {
            String trcFirstReferenceId = trcAssertion.getAdvice().getAssertionIDReferences().get(0).getValue();

            if (trcFirstReferenceId != null && trcFirstReferenceId.equals(hcpAssertion.getID())) {
                LOGGER.info("Assertion id reference equals to id.");
                return /* Least one of TRC Advice IdRef equals to HCP Ids */;
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to resolve first id reference: '{}'", ex.getMessage(), ex);
        }

        LOGGER.info("checkTRCAdviceIdReferenceAgainstHCPId: ReferenceId does not match. Throw InsufficientRightsException.");
        throw new InsufficientRightsException();
    }

    /**
     * Check if consent is given for patient
     *
     * @param patientId patient ID
     * @param countryId country ID
     * @return true if consent is given, else false.
     */
    public static boolean isConsentGiven(String patientId, String countryId) {

        return policyManager.isConsentGiven(patientId, countryId);
    }

    public static List<Assertion> getAssertions(Element soapHeader) {

        LOGGER.info("Retrieving SAML tokens from SOAP Header");
        NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");

        Element security = (Element) securityList.item(0);
        NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
        List<Assertion> result = new ArrayList<>();

        for (var i = 0; i < assertionList.getLength(); i++) {
            Element ass = (Element) assertionList.item(i);

            if (ass.getAttribute("ID").startsWith("urn:uuid:")) {
                ass.setAttribute("ID", "_" + ass.getAttribute("ID").substring("urn:uuid:".length()));
            }

            try {
                // Validate Assertion according to SAML XSD
                var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(ass));
                result.add((Assertion) SAML.fromElement(ass));

            } catch (UnmarshallingException | IOException | SAXException ex) {
                LOGGER.error(null, ex);
            }
        }
        return result;
    }

    private static String checkHCPAssertion(Assertion assertion, String classCode) throws MissingFieldException,
            InvalidFieldException, InsufficientRightsException, SMgrException {

        String sigCountryCode;

        RequiredFieldValidators.validateVersion(assertion);
        RequiredFieldValidators.validateID(assertion);
        RequiredFieldValidators.validateIssueInstant(assertion);
        RequiredFieldValidators.validateIssuer(assertion);
        RequiredFieldValidators.validateSubject(assertion);
        RequiredFieldValidators.validateNameID(assertion);
        RequiredFieldValidators.validateFormat(assertion);
        RequiredFieldValidators.validateSubjectConfirmation(assertion);
        RequiredFieldValidators.validateMethod(assertion);
        RequiredFieldValidators.validateConditions(assertion);
        RequiredFieldValidators.validateNotBefore(assertion);
        RequiredFieldValidators.validateNotOnOrAfter(assertion);
        RequiredFieldValidators.validateAuthnStatement(assertion);
        RequiredFieldValidators.validateAuthnInstant(assertion);
        RequiredFieldValidators.validateAuthnContext(assertion);
        RequiredFieldValidators.validateAuthnContextClassRef(assertion);
        RequiredFieldValidators.validateAttributeStatement(assertion);
        RequiredFieldValidators.validateSignature(assertion);

        FieldValueValidators.validateVersionValue(assertion);
        FieldValueValidators.validateIssuerValue(assertion);
        FieldValueValidators.validateNameIDValue(assertion);
        FieldValueValidators.validateNotBeforeValue(assertion);
        FieldValueValidators.validateNotOnOrAfterValue(assertion);
        FieldValueValidators.validateTimeSpanForHCP(assertion);
        FieldValueValidators.validateAuthnContextClassRefValueForHCP(assertion);

        policyManager.XSPASubjectValidatorForHCP(assertion, classCode);
        policyManager.XSPARoleValidator(assertion, classCode);
        policyManager.HealthcareFacilityValidator(assertion, classCode);
        policyManager.PurposeOfUseValidator(assertion, classCode);
        if (StringUtils.equals(classCode, Constants.EDD_CLASSCODE)) {
            policyManager.XSPAOrganizationIdValidator(assertion, classCode);
        }
        policyManager.XSPALocalityValidator(assertion, classCode);

        //TODO: [Mustafa, 2012.07.05] The original security manager was extended to return the two-letter country code
        // from the signature, but now in order not to change the security manager in Google Code repo, this is reverted back.
        // Konstantin: committed changes to security manager, in order to provide better support XCA and XDR implementations
        //TODO: Improve Exception management.
        sigCountryCode = new SignatureManager().verifySAMLAssertion(assertion);

        //TODO EHEALTH-6693 See if needed to incapsulate? EHDSI_ERROR_HPI_GENERIC, EHDSI_WARNING_HPI_GENERIC, EHDSI_ERROR_HPI_INSUFFICIENT_INFORMATION...

        return sigCountryCode;
    }

    private static void checkTRCAssertion(Assertion assertion, String classCode) throws MissingFieldException,
            InvalidFieldException, InsufficientRightsException {

        RequiredFieldValidators.validateVersion(assertion);
        RequiredFieldValidators.validateID(assertion);
        RequiredFieldValidators.validateIssuer(assertion);
        RequiredFieldValidators.validateIssueInstant(assertion);
        RequiredFieldValidators.validateSubject(assertion);
        RequiredFieldValidators.validateNameID(assertion);
        RequiredFieldValidators.validateFormat(assertion);
        RequiredFieldValidators.validateSubjectConfirmation(assertion);
        RequiredFieldValidators.validateMethod(assertion);
        RequiredFieldValidators.validateConditions(assertion);
        RequiredFieldValidators.validateNotBefore(assertion);
        RequiredFieldValidators.validateNotOnOrAfter(assertion);
        RequiredFieldValidators.validateAdvice(assertion);
        RequiredFieldValidators.validateAssertionIdRef(assertion);
        RequiredFieldValidators.validateAuthnStatement(assertion);
        RequiredFieldValidators.validateAuthnInstant(assertion);
        RequiredFieldValidators.validateAuthnContext(assertion);
        RequiredFieldValidators.validateAuthnContextClassRef(assertion);
        RequiredFieldValidators.validateAttributeStatement(assertion);
        RequiredFieldValidators.validateSignature(assertion);

        FieldValueValidators.validateVersionValue(assertion);
        FieldValueValidators.validateIssuerValue(assertion);
        FieldValueValidators.validateNameIDValue(assertion);
        FieldValueValidators.validateMethodValue(assertion);
        FieldValueValidators.validateNotBeforeValue(assertion);
        FieldValueValidators.validateNotOnOrAfterValue(assertion);
        FieldValueValidators.validateTimeSpanForTRC(assertion);
        FieldValueValidators.validateAuthnContextClassRefValueForTRC(assertion);

        policyManager.PurposeOfUseValidatorForTRC(assertion, classCode);
        policyManager.XSPASubjectValidatorForTRC(assertion, classCode);
    }

    public static String getCountryCodeFromHCPAssertion(Element soapHeader) throws MissingFieldException, XSDValidationException, SMgrException {

        String sigCountryCode = null;

        NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");
        Element security;
        if (securityList.getLength() > 0) {
            security = (Element) securityList.item(0);
        } else {
            throw (new MissingFieldException("Security element is required."));
        }

        NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
        Element hcpAss;
        Assertion hcpAssertion = null;
        try {
            if (assertionList.getLength() > 0) {
                for (var i = 0; i < assertionList.getLength(); i++) {
                    hcpAss = (Element) assertionList.item(i);
                    // Validate Assertion according to SAML XSD
                    var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                    schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(hcpAss));

                    hcpAssertion = (Assertion) SAML.fromElement(hcpAss);
                    if (org.apache.commons.lang.StringUtils.equals(hcpAssertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp")) {
                        break;
                    }
                }
            }
            if (hcpAssertion == null) {
                throw (new MissingFieldException("HCP Assertion element is required."));
            }

            sigCountryCode = new SignatureManager().verifySAMLAssertion(hcpAssertion);

        } catch (IOException | UnmarshallingException e) {
            LOGGER.error("{}: '{}'", e.getMessage(), e);
        } catch (SAXException e) {
            throw new XSDValidationException(e.getMessage());
        }

        return sigCountryCode;
    }
}
