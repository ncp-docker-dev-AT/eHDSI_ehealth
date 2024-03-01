package epsos.ccd.gnomon.auditmanager.eventidentification;

import net.RFC3881.EventID;
import net.RFC3881.EventIdentificationContents;
import net.RFC3881.EventTypeCode;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

public class EventIdentificationContentsBuilder {

        private EventID eventID;
        private List<EventTypeCode> eventTypeCodes;

        private String eventActionCode;

        private XMLGregorianCalendar eventDateTime;

        private String eventOutcomeIndicator;

        public EventIdentificationContentsBuilder() {
        }

        public EventIdentificationContentsBuilder eventID(EventID eventID) {
            this.eventID = eventID;
            return this;
        }

        public EventIdentificationContentsBuilder eventTypeCode(EventTypeCode eventTypeCode) {
            if (this.eventTypeCodes == null) {
                this.eventTypeCodes = new ArrayList<>();
            }
            if (eventTypeCode != null) {
                eventTypeCodes.add(eventTypeCode);
            }
            return this;
        }

        public EventIdentificationContentsBuilder eventActionCode(String eventActionCode) {
            this.eventActionCode = eventActionCode;
            return this;
        }

        public EventIdentificationContentsBuilder eventDateTime(XMLGregorianCalendar eventDateTime) {
            this.eventDateTime = eventDateTime;
            return this;
        }

        public EventIdentificationContentsBuilder eventOutcomeIndicator(String eventOutcomeIndicator) {
            this.eventOutcomeIndicator = eventOutcomeIndicator;
            return this;
        }

        public EventIdentificationContents build() {
            EventIdentificationContents eventIdentificationContents = new EventIdentificationContents();
            eventIdentificationContents.setEventActionCode(this.eventActionCode);
            eventIdentificationContents.setEventDateTime(this.eventDateTime);
            eventIdentificationContents.setEventOutcomeIndicator(this.eventOutcomeIndicator);
            eventIdentificationContents.setEventID(eventID);
            eventIdentificationContents.getEventTypeCode().addAll(eventTypeCodes);
            return eventIdentificationContents;
        }
 }
