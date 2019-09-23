package eu.europa.ec.sante.ehdsi.openncp.policymanager;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.PolicyAssertionManager;
import org.opensaml.saml.saml2.core.Assertion;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;

public class DefaultPolicyManagerImpl implements PolicyAssertionManager {

    @Override
    public void XSPASubjectValidatorForHCP(final Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XSPASubjectValidatorForTRC(final Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XSPARoleValidator(final Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XSPAFunctionalRoleValidator(Assertion assertion, String documentClass) throws MissingFieldException, InvalidFieldException {

    }

    @Override
    public void OnBehalfOfValidator(final Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void HealthcareFacilityValidator(final Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void PurposeOfUseValidator(final Assertion assertion, String documentClass)
            throws MissingFieldException, InsufficientRightsException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XSPALocalityValidator(Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XCPDPermissionValidator(Assertion assertion) throws InsufficientRightsException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XCAPermissionValidator(Assertion assertion, String documentClass)
            throws InsufficientRightsException, MissingFieldException {
        // TODO Auto-generated method stub

    }

    @Override
    public void XDRPermissionValidator(Assertion assertion, String documentClass)
            throws MissingFieldException, InvalidFieldException, InsufficientRightsException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isConsentGiven(String patientId, String countryId) {
        // TODO Auto-generated method stub
        return false;
    }
}
