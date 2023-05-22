package eu.europa.ec.joinup.ecc.openstork.utils.datamodel;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;

/**
 * This ENUM represents all the possible HCP roles in the concerned scope.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum HcpRole {

    PHYSICIAN("physician", XSPARole.MEDICAL_DOCTORS),
    PHARMACIST("pharmacist", XSPARole.PHARMACIST),
    NURSE("nurse", XSPARole.NURSE);

    private final String designation;

    private final XSPARole xspaRole;

    HcpRole(String designation, XSPARole xspaRole) {
        this.designation = designation;
        this.xspaRole = xspaRole;
    }

    @Override
    public String toString() {
        return designation;
    }

    public XSPARole getXspaRole() {
        return xspaRole;
    }
}
