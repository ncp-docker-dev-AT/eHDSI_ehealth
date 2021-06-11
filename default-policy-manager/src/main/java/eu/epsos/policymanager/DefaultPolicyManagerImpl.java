package eu.epsos.policymanager;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.*;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import java.util.List;

import static eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionHelper.getAttributeFromAssertion;

/**
 * Default Policy Manager implementation compliant with IHE profiles and eHDSI specifications.
 */
public class DefaultPolicyManagerImpl implements PolicyAssertionManager {

    private static final String ERROR_ASSERTION_MISSING_FIELD_ROLE = "A MissingFieldException was caught. The assertion role could not be obtained: '{}'";
    private final Logger logger = LoggerFactory.getLogger(DefaultPolicyManagerImpl.class);

    /**
     * Validates Health Care Facility Type SAML attribute implemented by default according the eHDSI SAML Profile document.
     *
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void HealthcareFacilityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String facilityType = getAttributeFromAssertion(assertion, AssertionConstants.URN_EPSOS_NAMES_WP3_4_SUBJECT_HEALTHCARE_FACILITY_TYPE);
        if (StringUtils.equalsIgnoreCase(facilityType, HealthcareFacilityType.HOSPITAL.toString())
                || StringUtils.equalsIgnoreCase(facilityType, HealthcareFacilityType.RESIDENT_PHYSICIAN.toString())
                || StringUtils.equalsIgnoreCase(facilityType, HealthcareFacilityType.PHARMACY.toString())
                || StringUtils.equalsIgnoreCase(facilityType, HealthcareFacilityType.OTHER.toString())) {

            logger.debug("HCP Identity Assertion Healthcare Facility Type: '{}'", facilityType);
        } else {
            logger.warn("InvalidFieldException: eHealth DSI Healthcare Facility Type 'urn:epsos:names:wp3.4:subject:healthcare-facility-type' attribute in assertion should be one of followings {'Hospital', 'Resident Physician', 'Pharmacy', 'Other'}.");
            throw new InvalidFieldException("eHealth DSI Healthcare Facility Type 'urn:epsos:names:wp3.4:subject:healthcare-facility-type' attribute in assertion should be one of followings {'Hospital', 'Resident Physician', 'Pharmacy', 'Other'}.");
        }
    }

    /**
     * Validates the OnBehalf attribute when an user is acting on behalf a clinician and its role is "Clerical and Administrative Personnel".
     *
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void OnBehalfOfValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String onBehalfOfRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_EPSOS_NAMES_WP3_4_SUBJECT_ON_BEHALF_OF);
        if (XSPAFunctionalRole.containsLabel(onBehalfOfRole)) {

            logger.debug("HCP Identity Assertion OnBehalfOf: '{}'", onBehalfOfRole);
        } else {
            throw new InvalidFieldException("OnBehalfOf 'urn:epsos:names:wp3.4:subject:on-behalf-of' attribute in assertion should be one of the element from XSPA Functional Role");
        }
    }

    /**
     * Validates the HCP Assertions provided by the end-user, a pair of structural and functional roles must be provided.
     * Previous roles used in epSOS and eHDSI Wave 1-2 are now deprecated, they might be considered as an ERROR in a future release
     * of the DefaultPolicyManager.
     *
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XSPARoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String structuralRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
        logger.debug("HCP Identity Assertion XSPA Structural Role: '{}'", structuralRole);

        if (XSPARole.containsLabel(structuralRole)) {

            if (structuralRole.equals(XSPARole.CLERICAL_ADMINISTRATIVE.toString())) {

                OnBehalfOfValidator(assertion, documentClass);
            }
            XSPAFunctionalRoleValidator(assertion, documentClass);
        } else {

            logger.error("XSPA Role 'urn:oasis:names:tc:xacml:2.0:subject:role' attribute in assertion should be one of the authorized value!");
            throw new InvalidFieldException("The user role is invalid. It shall be one of authorized value!");
        }
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XSPAFunctionalRoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String functionalRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_FUNCTIONAL_ROLE);
        logger.debug("XSPA Functional Role: '{}'", functionalRole);

        if (!XSPAFunctionalRole.containsLabel(functionalRole)) {
            throw new InvalidFieldException("The functional Role " + functionalRole + " of the user is invalid.");
        }
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XSPASubjectValidatorForHCP(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String subjectId = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_1_0_SUBJECT_SUBJECT_ID);
        if (StringUtils.isEmpty(subjectId)) {
            throw new InvalidFieldException("XSPA Subject 'urn:oasis:names:tc:xacml:1.0:subject:subject-id' attribute in assertion should be filled.");
        }
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XSPASubjectValidatorForTRC(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String resourceId = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_1_0_RESOURCE_RESOURCE_ID);
        if (StringUtils.isBlank(resourceId)) {
            throw new InvalidFieldException("XSPA Subject 'urn:oasis:names:tc:xacml:1.0:resource:resource-id' attribute in assertion should be filled.");
        }
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException       - User's assertion attribute is missing.
     * @throws InsufficientRightsException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void PurposeOfUseValidator(Assertion assertion, String documentClass) throws MissingFieldException, InsufficientRightsException {

        String resourceId = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_PURPOSEOFUSE);
        if (!StringUtils.equals(resourceId, PurposeOfUse.EMERGENCY.toString())
                && !StringUtils.equals(resourceId, PurposeOfUse.TREATMENT.toString())) {
            logger.error("InsufficientRightsException: Purpose of Use provided is not supported");
            throw new InsufficientRightsException();

        }
        logger.debug("HCP Identity Assertion XSPA Purpose of Use: '{}'", resourceId);
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException - User's assertion attribute is missing.
     * @throws InvalidFieldException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XSPALocalityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String environmentLocality = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_ENVIRONMENT_LOCALITY);
        if (StringUtils.isBlank(environmentLocality)) {
            throw new InvalidFieldException("XSPA Locality 'urn:oasis:names:tc:xspa:1.0:environment:locality' attribute in assertion should be filled.");
        }
        logger.debug("HCP Identity Assertion XSPA Locality: '{}", environmentLocality);
    }

    /**
     * @param assertion
     * @param documentClass
     * @throws MissingFieldException
     * @throws InvalidFieldException
     */
    @Override
    public void XSPAOrganizationIdValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

        String organizationId = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_ORGANIZATION_ID);
        if (StringUtils.isBlank(organizationId)) {
            throw new InvalidFieldException("XSPA Organization ID 'urn:oasis:names:tc:xspa:1.0:subject:organization-id' attribute in assertion should be filled.");
        }
        logger.debug("HCP Identity Assertion XSPA Organization ID: '{}", organizationId);
    }

    /**
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    @Override
    public void XCPDPermissionValidator(Assertion assertion) throws InsufficientRightsException {

        List<XMLObject> permissions = AssertionHelper.getPermissionValuesFromAssertion(assertion);
        for (XMLObject permission : permissions) {
            if (permission.getDOM() != null) {
                logger.debug("HCP Identity Assertion XCPD Permission: '{}'", permission.getDOM().getTextContent());
                if (permission.getDOM().getTextContent().equals(AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_006)) {
                    logger.debug("Found permission for PRD-006 (Patient Identification and Lookup)");
                    return;
                }
            }
        }
        logger.error("InsufficientRightsException: Permission '{}' not found!", AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_006);
        throw new InsufficientRightsException();
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException       - User's assertion attribute is missing.
     * @throws InsufficientRightsException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XCAPermissionValidator(Assertion assertion, String documentClass) throws InsufficientRightsException, MissingFieldException {

        switch (documentClass) {
            case Constants.PS_CLASSCODE:
                XCAPermissionValidatorPS(assertion);
                break;
            case Constants.EP_CLASSCODE:
                XCAPermissionValidatorEP(assertion);
                break;
            case Constants.MRO_CLASSCODE:
                XCAPermissionValidatorMro(assertion);
                break;
            case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
            case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
            case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
            case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                XCAPermissionValidatorOrCD(assertion);
            default:
                String errorMsg = "Invalid document class code: " + documentClass;
                logger.error(errorMsg);
                throw new MissingFieldException(errorMsg);
        }
    }

    /**
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XCAPermissionValidatorPS(Assertion assertion) throws InsufficientRightsException {

        var medicalHistory = false;
        var vitalSign = false;
        var patientMedications = false;
        var reviewProblem = false;


        List<XMLObject> permissions = AssertionHelper.getPermissionValuesFromAssertion(assertion);
        String permissionValue;
        String role;
        String functionalRole;

        //Check allowed roles
        try {
            role = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
            functionalRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_FUNCTIONAL_ROLE);
        } catch (MissingFieldException ex) {
            logger.error(ERROR_ASSERTION_MISSING_FIELD_ROLE, ex.getMessage(), ex);
            throw new InsufficientRightsException();
        }
        if (!XSPARole.containsLabel(role) && !XSPAFunctionalRole.containsLabel(functionalRole)) {

            logger.error("InsufficientRightsException - Unsupported role (named: '{}') tried to access Patient Summary documents.", role);
            throw new InsufficientRightsException();
        }

        //Check required permissions
        for (XMLObject permission : permissions) {
            if (permission.getDOM() != null) {
                permissionValue = permission.getDOM().getTextContent();

                switch (permissionValue) {
                    case AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_003:
                        medicalHistory = true;
                        logger.debug("Found permission for PRD-003 (Review Medical History)");
                        break;
                    case AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_005:
                        vitalSign = true;
                        logger.debug("Found permission for PRD-005 (Review Vital Signs/Patient Measurements)");
                        break;
                    case AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_010:
                        patientMedications = true;
                        logger.debug("Found permission for PRD-010 (Review Patient Medications)");
                        break;
                    case AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_016:
                        reviewProblem = true;
                        logger.debug("Found permission for PRD-016 (Review Problems)");
                        break;
                    default:
                        logger.warn("No permission found!!!");
                        break;
                }
            }
        }

        if (medicalHistory && vitalSign && patientMedications && reviewProblem) {
            return;
        }
        logger.error("InsufficientRightsException for Cross-Community Access to Patient Summary request");
        throw new InsufficientRightsException();
    }

    /**
     * XCA for order service (ePrescription)
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XCAPermissionValidatorEP(Assertion assertion) throws InsufficientRightsException {

        var reviewExistingOrders = false;
        var patientMedications = false;

        List<XMLObject> permissions = AssertionHelper.getPermissionValuesFromAssertion(assertion);
        String role;
        String functionalRole;

        //Check allowed roles
        try {
            role = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
            functionalRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_FUNCTIONAL_ROLE);
        } catch (MissingFieldException ex) {
            logger.error(ERROR_ASSERTION_MISSING_FIELD_ROLE, ex.getMessage(), ex);
            throw new InsufficientRightsException();
        }
        if (!XSPARole.containsLabel(role) && !XSPAFunctionalRole.containsLabel(functionalRole)) {

            logger.error("InsufficientRightsException - Unsupported (named: '{}'/'{}') role tried to access ePrescriptions documents.", role, functionalRole);
            throw new InsufficientRightsException();
        }

        //Check required permissions
        for (XMLObject permission : permissions) {
            if (permission.getDOM() != null) {

                if (permission.getDOM().getTextContent().equals(AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_004)) {
                    reviewExistingOrders = true;
                    logger.debug("Found permission for PRD-004 (Review Existing Orders)");
                } else if (permission.getDOM().getTextContent().equals(AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PRD_010)) {
                    patientMedications = true;
                    logger.debug("Found permission for PRD-010 (Review Patient Medications)");
                }
            }
        }

        if (reviewExistingOrders && patientMedications) {
            return;
        }
        logger.error("InsufficientRightsException for Cross-Community Access to ePrescription request");
        throw new InsufficientRightsException();
    }

    /**
     * XCA validator for MRO service, currently using the same validator for eP.
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XCAPermissionValidatorMro(Assertion assertion) throws InsufficientRightsException {

        XCAPermissionValidatorEP(assertion);
    }

    /**
     * XCA validator for OrCD service.
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XCAPermissionValidatorOrCD(Assertion assertion) throws InsufficientRightsException {
        //TODO to be reviewed. For the moment, the same validation is used as for PS.
        XCAPermissionValidatorPS(assertion);
    }

    /**
     * @param assertion     - SAML user assertion.
     * @param documentClass - Type of clinical document requested by the user (if available).
     * @throws MissingFieldException       - User's assertion attribute is missing.
     * @throws InsufficientRightsException - User's assertion attribute is not correct according the specification.
     */
    @Override
    public void XDRPermissionValidator(Assertion assertion, String documentClass) throws InsufficientRightsException, MissingFieldException {

        switch (documentClass) {
            //  eDispensation document
            case Constants.ED_CLASSCODE:
            case Constants.EDD_CLASSCODE:
                XDRPermissionValidatorSubmitDocument(assertion);
                break;
            //  HCER is not supported currently in eHDSI project.
            case Constants.HCER_CLASSCODE:
                XDRPermissionValidatorEncounterReport(assertion);
                break;
            // CONSENT is not supported currently in eHDSI project.
            case Constants.CONSENT_CLASSCODE:
                XDRPermissionValidatorConsent(assertion);
                break;
            default:
                logger.error("Invalid document class code: '{}'", documentClass);
                throw new MissingFieldException("Invalid document class code: " + documentClass);
        }
    }

    /**
     * XDR validation of Submit Document service (Dispense or Discard Medication).
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XDRPermissionValidatorSubmitDocument(Assertion assertion) throws InsufficientRightsException {

        var recordMedicationAdministrationRecord = false;
        List<XMLObject> permissions = AssertionHelper.getPermissionValuesFromAssertion(assertion);
        String role;
        String functionalRole;

        //Check allowed roles
        try {
            role = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
            functionalRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_FUNCTIONAL_ROLE);

        } catch (MissingFieldException ex) {
            logger.error(ERROR_ASSERTION_MISSING_FIELD_ROLE, ex.getMessage(), ex);
            throw new InsufficientRightsException();
        }
        if (!XSPARole.containsLabel(role) && !XSPAFunctionalRole.containsLabel(functionalRole)) {

            logger.error("InsufficientRightsException - Unsupported role (named: '{}') tried to submit eDispensations or HCER documents.", role);
            throw new InsufficientRightsException();
        }

        //Check required permissions
        for (XMLObject permission : permissions) {
            if (permission.getDOM() != null && permission.getDOM().getTextContent().equals(AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PPD_046)) {
                recordMedicationAdministrationRecord = true;
                logger.debug("Found permission for PPD-046 (Record Medication Administration Record)");
            }
        }

        if (!recordMedicationAdministrationRecord) {
            logger.error("InsufficientRightsException for Cross-Community Document Reliable to eDispense request");
            throw new InsufficientRightsException();
        }
    }

    /**
     * XDR for dispensation service HCER service
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XDRPermissionValidatorEncounterReport(Assertion assertion) throws InsufficientRightsException {

        XDRPermissionValidatorSubmitDocument(assertion);
    }

    /**
     * XDR for patient consent
     *
     * @param assertion - SAML user assertion.
     * @throws InsufficientRightsException - User doesn't have enough privileges.
     */
    private void XDRPermissionValidatorConsent(Assertion assertion) throws InsufficientRightsException {

        var recordMedicationAdministrationRecord = false;

        List<XMLObject> permissions = AssertionHelper.getPermissionValuesFromAssertion(assertion);
        String role;
        String functionalRole;

        //Check allowed roles
        try {
            role = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XACML_2_0_SUBJECT_ROLE);
            functionalRole = getAttributeFromAssertion(assertion, AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_FUNCTIONAL_ROLE);

        } catch (MissingFieldException ex) {
            logger.error(ERROR_ASSERTION_MISSING_FIELD_ROLE, ex.getMessage(), ex);
            throw new InsufficientRightsException();
        }
        if (!XSPARole.containsLabel(role) && !XSPAFunctionalRole.containsLabel(functionalRole)) {
            logger.error("InsufficientRightsException - Unsupported role (named: '{}') tried to submit consent documents.", role);
            throw new InsufficientRightsException();
        }

        //  Check required permissions
        for (XMLObject permission : permissions) {
            if (permission.getDOM() != null && StringUtils.equals(permission.getDOM().getTextContent(), AssertionConstants.URN_OASIS_NAMES_TC_XSPA_1_0_SUBJECT_HL7_PERMISSION_PPD_032)) {
                recordMedicationAdministrationRecord = true;
                logger.debug("Found permission for PPD-032 (New Consents and Authorizations)");
            }
        }

        if (!recordMedicationAdministrationRecord) {
            logger.error("InsufficientRightsException for Cross-Community Document Reliable to eConsent request");
            throw new InsufficientRightsException();
        }
    }

    /**
     * Validates if a patient has provided his consent. Default implementation always returns TRUE as the system is using
     * a mocked National Infrastructure.
     *
     * @param patientId - Patient Identifier provided during the XCPD request (traits identifier).
     * @param countryId - ISO code from the patient country of origin.
     * @return true||false according Patient Consent Management system from the National Infrastructure.
     */
    @Override
    public boolean isConsentGiven(String patientId, String countryId) {

        // Default policy always return TRUE.
        return true;
    }
}
