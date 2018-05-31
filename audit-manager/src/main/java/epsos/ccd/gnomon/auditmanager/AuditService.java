package epsos.ccd.gnomon.auditmanager;

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
 * @version 1.0, 2010, 30 Jun
 * @see net.RFC3881 http://www.rfc3881.net/ generated classes using JAXB Library for populating audit trail entries
 */
public class AuditService implements MessageHandlerListener {

    public static final String KEY_TIME_BETWEEN_FAILED_LOGS_HANDLING = "time.between.failed.logs.handling";
    public static final long DEFAULT_TIME_BETWEEN = 60 * 60 * 1000L; // 1h

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private FailedLogsHandlerService failedLogsHandlerService;
    private AuditLogSerializer auditLogSerializer;

    /**
     * @deprecated use {@link #eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory.getInstance()} instead.
     */
    @Deprecated
    public AuditService() {

        LOGGER.debug("Creating Audit Service...");
        initialize();
    }

    private void initialize() {

        Type type = Type.AUDIT_MANAGER;
        auditLogSerializer = new AuditLogSerializerImpl(type);
        failedLogsHandlerService = new FailedLogsHandlerServiceImpl(this, type);
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
                LOGGER.debug("Start of AuditLog transmission");
                AuditTrailUtils.getInstance().sendATNASyslogMessage(auditLogSerializer, am, facility, severity);
            } else if (el instanceof AuditMessage) {
                AuditMessage am = (AuditMessage) el;
                LOGGER.debug("Start of AuditLog transmission of backuped audit log");
                AuditTrailUtils.getInstance().sendATNASyslogMessage(null, am, facility, severity);
            } else {
                throw new IllegalArgumentException("Unsupported message format: " + el.getClass().getCanonicalName());
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("Exception: '{}'", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean handleMessage(Serializable message) {

        if (message instanceof SerializableMessage) {
            SerializableMessage sm = (SerializableMessage) message;
            boolean ok = write(sm.getMessage(), sm.getFacility(), sm.getSeverity());
            LOGGER.info("Attempt to write message to OpenATNA server. Result '{}'", ok);
            return ok;
        } else {
            LOGGER.warn("Message null or unknown type! Cannot handle message.");
            return false;
        }
    }
}
