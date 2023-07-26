package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old;

import java.time.Instant;

public class Message {

    private Long id;

    private String eventActionCode;

    private Instant eventDateTime;

    private String eventOutcome;

    private String eventId;

    private String eventTypes;

    private String messageType;

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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String eventTypes) {
        this.eventTypes = eventTypes;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
