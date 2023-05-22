package org.openhealthtools.openatna.anom;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.*;

/**
 * Audit message interface
 */
public class AtnaMessage implements Serializable {

    private static final long serialVersionUID = -5502378798460439820L;
    private final Set<AtnaCode> eventTypeCodes = new HashSet<>();
    private final Set<AtnaMessageParticipant> participants = new HashSet<>();
    private final Set<AtnaSource> sources = new HashSet<>();
    private final Set<AtnaMessageObject> objects = new HashSet<>();
    private Long messageId;
    private AtnaCode eventCode;
    private EventAction eventActionCode;
    private EventOutcome eventOutcome;
    private Date eventDateTime;
    private String sourceAddress;
    private byte[] messageContent;

    public AtnaMessage(AtnaCode eventCode, EventOutcome eventOutcome) {
        this.eventCode = eventCode;
        this.eventOutcome = eventOutcome;
    }

    /**
     * Returns unique ID for the message. This is assigned once a message has been successfully persisted.
     *
     * @return Atna message unique ID.
     */
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public List<AtnaCode> getEventTypeCodes() {
        return new ArrayList<>(eventTypeCodes);
    }

    public AtnaMessage addEventTypeCode(AtnaCode value) {
        this.eventTypeCodes.add(value);
        return this;
    }

    public AtnaMessage removeEventTypeCode(AtnaCode value) {
        this.eventTypeCodes.remove(value);
        return this;
    }

    public AtnaCode getEventCode() {
        return eventCode;
    }

    public AtnaMessage setEventCode(AtnaCode eventCode) {
        this.eventCode = eventCode;
        return this;
    }

    public EventAction getEventActionCode() {
        return eventActionCode;
    }

    public AtnaMessage setEventActionCode(EventAction eventActionCode) {
        this.eventActionCode = eventActionCode;
        return this;
    }

    public EventOutcome getEventOutcome() {
        return eventOutcome;
    }

    public AtnaMessage setEventOutcome(EventOutcome eventOutcome) {
        this.eventOutcome = eventOutcome;
        return this;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public AtnaMessage setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
        return this;
    }

    public List<AtnaMessageParticipant> getParticipants() {
        return new ArrayList<>(participants);
    }

    public AtnaMessage addParticipant(AtnaMessageParticipant participant) {
        this.participants.add(participant);
        return this;
    }

    public AtnaMessage removeParticipant(AtnaMessageParticipant participant) {
        this.participants.remove(participant);
        return this;
    }

    public AtnaMessageParticipant getParticipant(String id) {
        for (AtnaMessageParticipant participant : participants) {
            if (participant.getParticipant().getUserId().equals(id)) {
                return participant;
            }
        }
        return null;
    }

    public List<AtnaSource> getSources() {
        return new ArrayList<>(sources);
    }

    public AtnaMessage addSource(AtnaSource atnaSource) {
        this.sources.add(atnaSource);
        return this;
    }

    public AtnaMessage removeSource(AtnaSource atnaSource) {
        this.sources.remove(atnaSource);
        return this;
    }

    public AtnaSource getSource(String id) {
        for (AtnaSource source : sources) {
            if (source.getSourceId().equals(id)) {
                return source;
            }
        }
        return null;
    }

    public List<AtnaMessageObject> getObjects() {
        return new ArrayList<>(objects);
    }

    public AtnaMessage addObject(AtnaMessageObject object) {
        this.objects.add(object);
        return this;
    }

    public AtnaMessage removeObject(AtnaMessageObject object) {
        this.objects.remove(object);
        return this;
    }

    public AtnaMessageObject getObject(String id) {
        for (AtnaMessageObject object : objects) {
            if (object.getObject().getObjectId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AtnaMessage that = (AtnaMessage) o;

        return new EqualsBuilder()
                .append(messageId, that.messageId)
                .append(eventCode, that.eventCode)
                .append(eventTypeCodes, that.eventTypeCodes)
                .append(eventActionCode, that.eventActionCode)
                .append(eventOutcome, that.eventOutcome)
                .append(eventDateTime, that.eventDateTime)
                .append(sourceAddress, that.sourceAddress)
                .append(participants, that.participants)
                .append(sources, that.sources)
                .append(objects, that.objects)
                .append(messageContent, that.messageContent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(eventCode)
                .append(eventTypeCodes)
                .append(eventActionCode)
                .append(eventOutcome)
                .append(eventDateTime)
                .append(sourceAddress)
                .append(participants)
                .append(sources)
                .append(objects)
                .append(messageContent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("eventTypeCodes", eventTypeCodes)
                .append("participants", participants)
                .append("sources", sources)
                .append("objects", objects)
                .append("messageId", messageId)
                .append("eventCode", eventCode)
                .append("eventActionCode", eventActionCode)
                .append("eventOutcome", eventOutcome)
                .append("eventDateTime", eventDateTime)
                .append("sourceAddress", sourceAddress)
                .append("messageContent", messageContent)
                .toString();
    }
}
