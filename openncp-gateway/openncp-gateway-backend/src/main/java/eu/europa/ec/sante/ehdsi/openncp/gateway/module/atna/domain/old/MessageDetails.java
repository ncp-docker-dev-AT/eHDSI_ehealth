package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageDetails {

    private Long id;

    private String eventActionCode;

    private Instant eventDateTime;

    private String eventOutcome;

    private Code eventId;

    private List<Code> eventTypes = new ArrayList<>();

    private List<MessageParticipant> messageParticipants = new ArrayList<>();

    private String messageContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventActionCode() {
        return eventActionCode;
    }

    public void setEventActionCode(String eventActionCode) {
        this.eventActionCode = eventActionCode;
    }

    public Instant getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Instant eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(String eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    public Code getEventId() {
        return eventId;
    }

    public void setEventId(Code eventId) {
        this.eventId = eventId;
    }

    public List<Code> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<Code> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<MessageParticipant> getMessageParticipants() {
        return messageParticipants;
    }

    public void setMessageParticipants(List<MessageParticipant> messageParticipants) {
        this.messageParticipants = messageParticipants;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
