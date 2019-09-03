package eu.europa.ec.joinup.ecc.openstork.utils.datamodel;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;

/**
 * This ENUM represents all the possible HCP roles in the concerned scope.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum HcpRole {

    PHYSICIAN("physician", XSPARole.DEPRECATED_PHYSICIAN),
    PHARMACIST("pharmacist", XSPARole.DEPRECATED_PHARMACIST),
    NURSE("nurse", XSPARole.DEPRECATED_NURSE),
    PATIENT("patient", XSPARole.DEPRECATED_PATIENT),
    ADMINISTRATOR("administrator", XSPARole.ADMISSION_CLERK);

    private final String designation;
    private final XSPARole xspaRole;

    HcpRole(String s, XSPARole xspaRole) {
        designation = s;
        this.xspaRole = xspaRole;
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
}
