package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import epsos.ccd.gnomon.auditmanager.EventLog;
import net.RFC3881.AuditMessage;

public interface AuditMessageBuilder {

    AuditMessage build(EventLog eventLog);
}
