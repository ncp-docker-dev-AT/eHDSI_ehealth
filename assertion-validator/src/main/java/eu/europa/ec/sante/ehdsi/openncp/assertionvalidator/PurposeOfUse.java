package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

public enum PurposeOfUse {

    EMERGENCY("EMERGENCY"),
    TREATMENT("TREATMENT");

    private final String purpose;

    PurposeOfUse(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public String toString() {
        return purpose;
    }
}
