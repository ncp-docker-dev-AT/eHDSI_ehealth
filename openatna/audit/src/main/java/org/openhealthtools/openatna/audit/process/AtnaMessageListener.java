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
            logger.debug("Processing message " + atnaMessage.getEventOutcome());
            atnaMessage.setSourceAddress(message.getSourceIp());
            byte[] bytes = "no message available".getBytes();
            logger.info("[ATNA Secured Node] Message Received: [{}] '{}'-'{}' '{}' '{}' '{}'",
                    message.getMessage().getMessageObject().getEventCode().getCode(), message.getFacility(), message.getSeverity(),
                    message.getHostName(), message.getSourceIp(), message.getTimestamp());
            try {
                bytes = message.toByteArray();
            } catch (SyslogException e1) {
                logger.error("SyslogException: '{}'", e1.getMessage());
            }
            atnaMessage.setMessageContent(bytes);
            boolean persisted = false;
            try {
                persisted = service.process(atnaMessage);
            } catch (Exception e) {
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

        logger.info("Entered in 'exceptionThrown'");
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
