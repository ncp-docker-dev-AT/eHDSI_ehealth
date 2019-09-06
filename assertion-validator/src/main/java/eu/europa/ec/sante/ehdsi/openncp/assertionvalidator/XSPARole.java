package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

import org.apache.commons.lang3.StringUtils;

/**
 * Structural Role of the HCP - From eHDSI SAML Profile
 * FriendlyName:  XSPA Role
 * Name:  urn:oasis:names:tc:xacml:2.0:subject:role
 */
public enum XSPARole {

    LICENSED_HCP("Licensed Health Care Providers"),
    NOT_LICENSED_HCP("Non-Licensed Health Care Providers"),
    ANCILLARY_SERVICES("Ancillary Services"),
    CLINICAL_SERVICES("Clinical Services"),
    // Deprecated roles
    DEPRECATED_ADMISSION_CLERK("Admission Clerk"),
    DEPRECATED_NURSE("nurse"),
    DEPRECATED_PHARMACIST("pharmacist"),
    DEPRECATED_PHYSICIAN("physician"),
    DEPRECATED_MIDWIFE("nurse midwife"),
    //  Not supported in eHDSI - PAC Service Role
    DEPRECATED_PATIENT("patient");

    private final String role;

    XSPARole(final String role) {
        this.role = role;
    }

    public static boolean containsLabel(String label) {
        for (XSPARole e : values()) {
            if (StringUtils.equalsIgnoreCase(e.role, label)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return role;
    }
}

