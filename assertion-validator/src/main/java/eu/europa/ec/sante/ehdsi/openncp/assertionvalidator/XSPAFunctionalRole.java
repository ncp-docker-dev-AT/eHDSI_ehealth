package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

import org.apache.commons.lang3.StringUtils;

/**
 * Structural Role of the HCP - From eHDSI SAML Profile
 * FriendlyName: XSPA Functional Role
 * Name: urn:oasis:names:tc:xspa:1.0:subject:functional-role
 */
public enum XSPAFunctionalRole {

    AUDIOLOGIST("Audiologists and speech therapists"),
    DENTIST("Dentists"),
    MEDICAL_DOCTORS("Medical Doctors"),
    MEDICAL_TECHNICIAN("Medical imaging and therapeutic equipment technicians"),
    MIDWIFE("Midwifery professionals"),
    NURSE("Nursing professionals"),
    NUTRITIONIST("Dieticians and nutritionists"),
    OPTICIAN("Optometrists and ophthalmic opticians"),
    OTHER_CLINICIAN("Health professionals not elsewhere classified"),
    OTHER_CLERICAL("Other Clerical Support Workers"),
    OTHER("Other"),
    PHARMACIST("Pharmacists"),
    PHARMACIST_ASSISTANT("Pharmaceutical technicians and assistants"),
    PHYSIOTHERAPIST("Physiotherapists");

    private final String role;

    XSPAFunctionalRole(String role) {
        this.role = role;
    }

    public static boolean containsLabel(String label) {

        for (XSPAFunctionalRole e : values()) {
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
