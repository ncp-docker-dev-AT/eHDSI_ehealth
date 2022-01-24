package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "codes")
public class Code {

    @Id
    private Long id;

    private String code;

    private String codeSystem;

    private String codeSystemName;

    private String displayName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
