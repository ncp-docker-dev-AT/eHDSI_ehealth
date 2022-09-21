package eu.europa.ec.sante.ehdsi.constant.codesystem;

public enum CodeSystem {
    ATC("2.16.840.1.113883.6.73"),
    EDQM("0.4.0.127.0.16.1.1.2.1"),
    HL7_CONFIDENTIALITY("2.16.840.1.113883.5.25"),
    ISO_COUNTRY_CODES("1.0.3166.1"),
    LOINC("2.16.840.1.113883.6.1");

    private String oid;

    CodeSystem(String oid) {
        this.oid = oid;
    }

    public String getOID() {
        return oid;
    }
}
