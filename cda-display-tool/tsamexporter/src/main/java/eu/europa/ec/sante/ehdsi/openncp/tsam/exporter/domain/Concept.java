package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain;

public class Concept {

    private Long id;

    private String code;

    private String valueSetOid;

    private String valueSetName;

    public Concept(Long id, String code, String valueSetOid, String valueSetName) {
        this.id = id;
        this.code = code;
        this.valueSetOid = valueSetOid;
        this.valueSetName = valueSetName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValueSetOid() {
        return valueSetOid;
    }

    public void setValueSetOid(String valueSetOid) {
        this.valueSetOid = valueSetOid;
    }

    public String getValueSetName() {
        return valueSetName;
    }

    public void setValueSetName(String valueSetName) {
        this.valueSetName = valueSetName;
    }
}
