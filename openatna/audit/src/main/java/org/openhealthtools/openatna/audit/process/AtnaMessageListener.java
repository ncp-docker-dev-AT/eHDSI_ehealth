package org.openhealthtools.openatna.audit.process;

import eu.epsos.util.audit.AuditLogSerializer;
import eu.epsos.util.audit.AuditLogSerializer.Type;
import eu.epsos.util.audit.AuditLogSerializerImpl;
import eu.epsos.util.audit.MessageHandlerListener;
import org.openhealthtools.openatna.anom.AtnaMessage;
import org.openhealthtools.openatna.audit.AtnaFactory;
import org.openhealthtools.openatna.audit.log.PersistenceErrorLogger;
import org.openhealthtools.openatna.audit.log.SyslogErrorLogger;
import org.openhealthtools.openatna.audit.persistence.AtnaPersistenceException;
import org.openhealthtools.openatna.audit.persistence.PersistencePolicies;
import org.openhealthtools.openatna.audit.persistence.dao.ErrorDao;
import org.openhealthtools.openatna.audit.persistence.model.ErrorEntity;
import org.openhealthtools.openatna.audit.service.AuditService;
import org.openhealthtools.openatna.audit.service.ServiceConfiguration;
import org.openhealthtools.openatna.syslog.LogMessage;
import org.openhealthtools.openatna.syslog.SyslogException;
import org.openhealthtools.openatna.syslog.SyslogMessage;
import org.openhealthtools.openatna.syslog.transport.SyslogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Andrew Harrison
 */
public class AtnaMessageListener implements SyslogListener<AtnaMessage>, MessageHandlerListener {

    private Logger logger = LoggerFactory.getLogger(AtnaMessageListener.class);
    private AuditService service;
    private AuditLogSerializer auditLogSerializer;

    public AtnaMessageListener(AuditService service) {
        this.service = service;
        auditLogSerializer = new AuditLogSerializerImpl(Type.ATNA);
    }

    public void messageArrived(SyslogMessage<AtnaMessage> message) {

        logger.info("[ATNA Listener] Message '{}' arrived", message.getClass());
        boolean persisted = false;
        try {
            persisted = processMessage(message);
        } finally {

            if (!persisted) {
                auditLogSerializer.writeObjectToFile(message);
            }
        }
    }

    public boolean handleMessage(Serializable message) {

        logger.info("[ATNA Listener] Handling Message: '{}'", message.getClass());
        if (message instanceof SyslogMessage<?>) {
            return processMessage((SyslogMessage<AtnaMessage>) message);
        } else {
            logger.warn("Message null or unknown type! Cannot handle message.");
            return false;
        }
    }

    public boolean processMessage(SyslogMessage<AtnaMessage> message) {

        synchronized (this) {
            LogMessage<AtnaMessage> msg = message.getMessage();
            AtnaMessage atnaMessage = msg.getMessageObject();
            logger.info("[ATNA Listener] Processing Syslog Message: '{}'", atnaMessage.getEventOutcome());
            atnaMessage.setSourceAddress(message.getSourceIp());
            byte[] bytes = "No message available".getBytes(StandardCharsets.UTF_8);
            logger.info("[ATNA Listener] Message Processed: [{}] '{}'-'{}' '{}' '{}' '{}'",
                    message.getMessage().getMessageObject().getEventCode().getCode(), message.getFacility(),
                    message.getSeverity(), message.getHostName(), message.getSourceIp(), message.getTimestamp());
            try {
                bytes = message.toByteArray();
            } catch (SyslogException e1) {
                logger.error("SyslogException: '{}'", e1.getMessage());
            }
            atnaMessage.setMessageContent(bytes);
            boolean persisted = false;
            try {
                persisted = service.process(atnaMessage);
                if (logger.isDebugEnabled()) {
                    logger.debug("[ATNA Listener] Syslog message '{}' persisted: '{}'", atnaMessage.getMessageId(), persisted);
                }
            } catch (Exception e) {
                logger.error("Exception: '{}'", e.getMessage(), e);
                SyslogException ex = new SyslogException(e.getMessage(), e, bytes);
                if (message.getSourceIp() != null) {
                    ex.setSourceIp(message.getSourceIp());
                }
                exceptionThrown(ex);
            }
            return persisted;
        }
    }

    public void exceptionThrown(SyslogException exception) {

        logger.debug("[ATNA Listener] Processing Exception");
        SyslogErrorLogger.log(exception);
        ServiceConfiguration config = service.getServiceConfig();
        if (config != null) {
            PersistencePolicies pp = config.getPersistencePolicies();
            if (pp != null && pp.isPersistErrors()) {

                ErrorDao dao = AtnaFactory.errorDao();
                ErrorEntity ent = createEntity(exception);
                synchronized (this) {
                    try {
                        dao.save(ent);
                        logger.debug("[ATNA Listener] Error persisted into database");
                    } catch (AtnaPersistenceException e) {
                        PersistenceErrorLogger.log(e);
                    }
                }
            }
        }
    }

    private ErrorEntity createEntity(SyslogException e) {

        ErrorEntity ent = new ErrorEntity();
        ent.setErrorTimestamp(new Date());

        if (e.getBytes() != null) {
            ent.setPayload(e.getBytes());
        }
        if (e.getSourceIp() != null) {
            ent.setSourceIp(e.getSourceIp());
        }
        if (e.getMessage() != null) {
            ent.setErrorMessage(e.getClass().getName() + ":" + e.getMessage());
        }
        ent.setStackTrace(createStackTrace(e));
        return ent;
    }

    private byte[] createStackTrace(Throwable e) {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bout);
        e.printStackTrace(writer);
        writer.flush();
        writer.close();
        return bout.toByteArray();
    }
}
