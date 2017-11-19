package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain;

public class ValueSet {

    private Long id;

    private String oid;

    private String name;

    public ValueSet(Long id, String oid, String name) {
        this.id = id;
        this.oid = oid;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
