package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditConstant;
import net.RFC3881.AuditMessage;

public class XCAListAuditMessageBuilder extends AbstractAuditMessageBuilder implements AuditMessageBuilder {
    @Override
    public AuditMessage build(EventLog eventLog) {
        AuditMessage message = createAuditTrailForHCPAssurance(eventLog);
        if (message != null) {
            addParticipantObject(message, eventLog.getReqM_ParticipantObjectID(), Short.valueOf("2"), Short.valueOf("24"),
                    "Patient", "ITI-38", "IHE Transactions", "Patient Number",
                    "Cross Gateway Query", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("2"), Short.valueOf("4"),
                    "12", "", Short.valueOf("0"), AuditConstant.DICOM, "Cross Gateway Query");

        }
        return message;
    }
}