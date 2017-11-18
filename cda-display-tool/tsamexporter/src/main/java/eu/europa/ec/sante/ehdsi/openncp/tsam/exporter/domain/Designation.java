package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain;

public class Designation {

    private String language;

    private String value;

    public Designation(String language, String value) {
        this.language = language;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
