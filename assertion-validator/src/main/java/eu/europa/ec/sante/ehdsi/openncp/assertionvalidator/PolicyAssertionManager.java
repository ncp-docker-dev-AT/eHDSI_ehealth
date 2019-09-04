package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

import org.opensaml.saml.saml2.core.Assertion;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;

public interface PolicyAssertionManager {

    void XSPASubjectValidatorForHCP(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPASubjectValidatorForTRC(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPARoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XSPAFunctionalRoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void OnBehalfOfValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void HealthcareFacilityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void PurposeOfUseValidator(Assertion assertion, String documentClass) throws MissingFieldException, InsufficientRightsException;

    void XSPALocalityValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException;

    void XCPDPermissionValidator(Assertion assertion) throws InsufficientRightsException;

    void XCAPermissionValidator(Assertion assertion, String documentClass) throws InsufficientRightsException, MissingFieldException;

    void XDRPermissionValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException, InsufficientRightsException;

    boolean isConsentGiven(String patientId, String countryId);
}
