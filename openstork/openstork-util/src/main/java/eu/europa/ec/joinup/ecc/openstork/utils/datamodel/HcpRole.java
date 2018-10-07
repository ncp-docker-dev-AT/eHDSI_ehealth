package eu.europa.ec.joinup.ecc.openstork.utils.datamodel;

import eu.epsos.assertionvalidator.XSPARole;

/**
 * This ENUM represents all the possible HCP roles in the concerned scope.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum HcpRole {

    PHYSICIAN("physician", XSPARole.PHYSICIAN),
    PHARMACIST("pharmacist", XSPARole.PHARMACIST),
    NURSE("nurse", XSPARole.NURSE),
    PATIENT("patient", XSPARole.PATIENT),
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
