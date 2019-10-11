package eu.europa.ec.joinup.ecc.openstork.utils.datamodel;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPAFunctionalRole;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;

/**
 * This ENUM represents all the possible HCP roles in the concerned scope.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum HcpRole {

    PHYSICIAN("physician", XSPARole.LICENSED_HCP, XSPAFunctionalRole.MEDICAL_DOCTORS),
    PHARMACIST("pharmacist", XSPARole.LICENSED_HCP, XSPAFunctionalRole.PHARMACIST),
    NURSE("nurse", XSPARole.LICENSED_HCP, XSPAFunctionalRole.NURSE);

    private final String designation;
    private final XSPARole xspaRole;
    private final XSPAFunctionalRole xspaFunctionalRole;

    HcpRole(String s, XSPARole xspaRole, XSPAFunctionalRole xspaFunctionalRole) {
        designation = s;
        this.xspaRole = xspaRole;
        this.xspaFunctionalRole = xspaFunctionalRole;
    }

    @Override
    public String toString() {
        return designation;
    }

    /**
     * @return the XSPA Role
     */
    public XSPARole getXspaRole() {
        return xspaRole;
    }

    public XSPAFunctionalRole getXspaFunctionalRole() {
        return xspaFunctionalRole;
    }
}
