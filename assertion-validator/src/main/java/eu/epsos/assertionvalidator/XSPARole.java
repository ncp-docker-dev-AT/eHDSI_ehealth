package eu.epsos.assertionvalidator;

/**
 * Structural Role of the HCP - From WP3.4 Deliberable D.3.4.2
 * FriendlyName:  XSPA Role
 * Name:  urn:oasis:names:tc:xacml:2.0:subject:role
 */
public enum XSPARole {

    //EPSOS_DOCTOR("medical doctor"),
    DENTIST("dentist"),
    NURSE("nurse"),
    PHARMACIST("pharmacist"),
    PHYSICIAN("physician"),
    NURSE_MIDWIFE("nurse midwife"),
    ADMISSION_CLERK("admission clerk"),
    ANCILLARY_SERVICES("ancillary services"),
    CLINICAL_SERVICES("clinical services"),
    //  PAC Service Role
    PATIENT("patient");

    private final String role;

    XSPARole(final String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
