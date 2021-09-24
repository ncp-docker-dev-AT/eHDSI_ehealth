package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "message_participants")
public class MessageParticipantEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private ParticipantEntity participant;

    private Boolean userIsRequestor;

    @OneToOne(fetch = FetchType.LAZY)
    private NetworkAccessPointEntity networkAccessPoint;

    public Long getId() {
        return id;
    }

    public ParticipantEntity getParticipant() {
        return participant;
    }

    public Boolean getUserIsRequestor() {
        return userIsRequestor;
    }

    public NetworkAccessPointEntity getNetworkAccessPoint() {
        return networkAccessPoint;
    }
}
