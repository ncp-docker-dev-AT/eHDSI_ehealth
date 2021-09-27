package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "network_access_points")
public class NetworkAccessPointEntity {

    @Id
    private Long id;

    private Integer version;

    private Short type;

    private String identifier;

    public Long getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    public Short getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
}
