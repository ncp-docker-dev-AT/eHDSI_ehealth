package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "participants")
public class ParticipantEntity {

    @Id
    private Long id;

    private Integer version;

    private String userId;

    private String alternativeUserId;

    private String userName;

    @ManyToMany
    @JoinTable(
            name = "participants_codes",
            joinColumns = @JoinColumn(name = "ParticipantEntity_id"),
            inverseJoinColumns = @JoinColumn(name = "participantTypeCodes_id")
    )
    private Set<Code> participantTypes = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    public String getUserId() {
        return userId;
    }

    public String getAlternativeUserId() {
        return alternativeUserId;
    }

    public String getUserName() {
        return userName;
    }

    public Set<Code> getParticipantTypes() {
        return participantTypes;
    }
}
