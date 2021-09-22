package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old;

public class MessageParticipant {

    private Long id;

    private Participant participant;

    private boolean userIsRequestor;

    private NetworkAccessPoint networkAccessPoint;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public boolean isUserIsRequestor() {
        return userIsRequestor;
    }

    public void setUserIsRequestor(boolean userIsRequestor) {
        this.userIsRequestor = userIsRequestor;
    }

    public NetworkAccessPoint getNetworkAccessPoint() {
        return networkAccessPoint;
    }

    public void setNetworkAccessPoint(NetworkAccessPoint networkAccessPoint) {
        this.networkAccessPoint = networkAccessPoint;
    }
}
