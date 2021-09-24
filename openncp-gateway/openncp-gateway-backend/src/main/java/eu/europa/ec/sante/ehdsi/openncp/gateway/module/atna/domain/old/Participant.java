package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old;

import java.util.ArrayList;
import java.util.List;

public class Participant {

    private Long id;

    private String userId;

    private String alternativeUserId;

    private String userName;

    private List<Code> participantTypes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAlternativeUserId() {
        return alternativeUserId;
    }

    public void setAlternativeUserId(String alternativeUserId) {
        this.alternativeUserId = alternativeUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Code> getParticipantTypes() {
        return participantTypes;
    }

    public void setParticipantTypes(List<Code> participantTypes) {
        this.participantTypes = participantTypes;
    }
}
