package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain;

public class CodeSystem {

    private Long id;

    private String name;

    public CodeSystem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
