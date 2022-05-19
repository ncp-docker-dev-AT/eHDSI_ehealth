package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import org.opensaml.saml.saml2.core.Assertion;

public interface PolicyAssertionManager {

    void HealthcareFacilityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    boolean isConsentGiven(String patientId, String countryId);

    void OnBehalfOfValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void PurposeOfUseValidator(Assertion assertion, String documentClass) throws MissingFieldException, InsufficientRightsException;

    void XCAPermissionValidator(Assertion assertion, String documentClass) throws InsufficientRightsException, MissingFieldException;

    void XCPDPermissionValidator(Assertion assertion) throws InsufficientRightsException;

    void XDRPermissionValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException, InsufficientRightsException;

    void XSPAFunctionalRoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void PurposeOfUseValidatorForTRC(Assertion assertion, String documentClass) throws MissingFieldException, InsufficientRightsException;

    void XSPALocalityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPAOrganizationIdValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPARoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPASubjectValidatorForHCP(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPASubjectValidatorForTRC(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;
}
