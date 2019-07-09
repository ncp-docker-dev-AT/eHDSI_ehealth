package eu.europa.ec.sante.ehdsi.openncp.audit;

import epsos.ccd.gnomon.auditmanager.AuditTrailUtils;
import epsos.ccd.gnomon.auditmanager.EventLog;
import epsos.ccd.gnomon.utils.SerializableMessage;
import eu.epsos.util.audit.*;
import eu.epsos.util.audit.AuditLogSerializer.Type;
import net.RFC3881.AuditMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * This service provides access to the system defined properties
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @see net.RFC3881 http://www.rfc3881.net/ generated classes using JAXB Library for populating audit trail entries
 */
public class AuditService implements MessageHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private FailedLogsHandlerService failedLogsHandlerService;
    private AuditLogSerializer auditLogSerializer;

    protected AuditService() {

        logger.debug("Creating Audit Service...");
        auditLogSerializer = new AuditLogSerializerImpl(Type.AUDIT_MANAGER);
        failedLogsHandlerService = new FailedLogsHandlerServiceImpl(this, Type.AUDIT_MANAGER);
        failedLogsHandlerService.start();
    }

    /**
     * Provides a method to write an Audit Log.
     *
     * @param el
     * @param facility the facility number according to log4j
     * @param severity the severity of the message
     * @return true if auditLog is attempted to be sent
     */
    public synchronized Boolean write(Object el, String facility, String severity) {

        try {
            if (el instanceof EventLog) {
                EventLog eventLog = (EventLog) el;
                AuditMessage am = AuditTrailUtils.getInstance().createAuditMessage(eventLog);
                logger.debug("Start of AuditLog transmission");
                AuditTrailUtils.getInstance().sendATNASyslogMessage(auditLogSerializer, am, facility, severity);
            } else if (el instanceof AuditMessage) {
                AuditMessage am = (AuditMessage) el;
                logger.debug("Start of AuditLog transmission of backuped audit log");
                AuditTrailUtils.getInstance().sendATNASyslogMessage(auditLogSerializer, am, facility, severity);
            } else {
                throw new IllegalArgumentException("Unsupported message format: " + el.getClass().getCanonicalName());
            }
            return true;
        } catch (Exception e) {
            logger.warn("Exception: '{}'", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean handleMessage(Serializable message) {

        if (message instanceof SerializableMessage) {
            SerializableMessage sm = (SerializableMessage) message;
            boolean sent = write(sm.getMessage(), sm.getFacility(), sm.getSeverity());
            logger.info("Attempt to write message to OpenATNA server. Result '{}'", sent);
            return sent;
        } else {
            logger.warn("Message null or unknown type! Cannot handle message.");
            return false;
        }
    }

    protected void stopFailedHandler() {
        this.failedLogsHandlerService.stop();
    }
}
