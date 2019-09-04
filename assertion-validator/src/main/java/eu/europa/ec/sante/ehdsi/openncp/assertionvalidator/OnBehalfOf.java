package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

/**
 * From WP3.4 Deliverable D.3.4.2
 * Delegated Rights
 * FriendlyName:  OnBehalfOf
 * Name:  urn:epsos:names:wp3.4:subject:on-behalf-of
 */
public enum OnBehalfOf {

    MIDWIVES("Midwives"),
    PHARMACIST("Pharmacist"),
    PHYSICIAN("Physician"),
    REGISTERED_NURSE("Registered Nurse"),
    DEPRECATED_NURSE("nurse"),
    DEPRECATED_NURSE_MIDWIFE("nurse midwife");

    private final String role;

    OnBehalfOf(final String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
