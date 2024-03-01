package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditConstant;
import net.RFC3881.AuditMessage;

public class DispensationDiscardServiceAuditMessageBuilder extends AbstractAuditMessageBuilder implements AuditMessageBuilder {
    @Override
    public AuditMessage build(EventLog eventLog) {
        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        // Event Target
        if (message != null) {
            addParticipantObject(message, eventLog.getReqM_ParticipantObjectID(), Short.valueOf("2"), Short.valueOf("20"),
                    "Patient", "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd", "IHE XDS Metadata", "Patient Number",
                    "Cross Gateway Query", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                        "12", AuditConstant.ACTION_DISCARD, Short.valueOf("14"), "", "");
        }
        return message;
    }
}
