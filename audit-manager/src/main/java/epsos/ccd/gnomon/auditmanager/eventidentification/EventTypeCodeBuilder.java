package epsos.ccd.gnomon.auditmanager.eventidentification;

import net.RFC3881.EventTypeCode;

public class EventTypeCodeBuilder {
    private String codeSystemName;
    private String csdCode;
    private String displayName;
    private String originalText;

    public EventTypeCodeBuilder codeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
        return this;
    }

    public EventTypeCodeBuilder csdCode(String csdCode) {
        this.csdCode = csdCode;
        return this;
    }

    public EventTypeCodeBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public EventTypeCodeBuilder originalText(String originalText) {
        this.originalText = originalText;
        return this;
    }

    public EventTypeCode build() {
        EventTypeCode eventTypeCode = new EventTypeCode();
        eventTypeCode.setCodeSystemName(this.codeSystemName);
        eventTypeCode.setCsdCode(this.csdCode);
        eventTypeCode.setDisplayName(this.displayName);
        eventTypeCode.setOriginalText(this.originalText);
        return eventTypeCode;
    }

}
