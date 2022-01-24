package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old;

public class NetworkAccessPoint {

    private Long id;

    private Short type;

    private String identifier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
