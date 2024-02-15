package epsos.ccd.gnomon.auditmanager.eventidentification;

import net.RFC3881.EventID;

public class EventIDBuilder {

    private String codeSystemName;
    private String csdCode;
    private String displayName;
    private String originalText;

    public EventIDBuilder codeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
        return this;
    }

    public EventIDBuilder csdCode(String csdCode) {
        this.csdCode = csdCode;
        return this;
    }

    public EventIDBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public EventIDBuilder originalText(String originalText) {
        this.originalText = originalText;
        return this;
    }

    public EventID build() {
        EventID eventID = new EventID();
        eventID.setCodeSystemName(this.codeSystemName);
        eventID.setCsdCode(this.csdCode);
        eventID.setDisplayName(this.displayName);
        eventID.setOriginalText(this.originalText);
        return eventID;
    }
}
