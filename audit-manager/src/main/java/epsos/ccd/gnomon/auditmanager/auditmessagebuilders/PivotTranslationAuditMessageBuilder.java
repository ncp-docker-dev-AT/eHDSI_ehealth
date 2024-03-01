package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import epsos.ccd.gnomon.auditmanager.EventActionCode;
import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditConstant;
import net.RFC3881.AuditMessage;
import net.RFC3881.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PivotTranslationAuditMessageBuilder extends AbstractAuditMessageBuilder implements AuditMessageBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PivotTranslationAuditMessageBuilder.class);
    @Override
    public AuditMessage build(EventLog eventLog) {
        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addAuditSource(message, eventLog.getAS_AuditSourceId());
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(), EventActionCode.EXECUTE.getCode(),
                    eventLog.getEI_EventDateTime(), eventLog.getEI_EventOutcomeIndicator(), eventLog.getNcpSide());

            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER, AuditConstant.CODE_SYSTEM_EHDSI,
                    "Service Provider", eventLog.getTargetip());
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        if (message != null) {
            // Event Target
            addEventTarget(message, eventLog.getEventTargetParticipantObjectIds(), Short.valueOf("4"), Short.valueOf("5"),
                    "in", "eHealth DSI Translation", "Input Data");
            addEventTarget(message, Arrays.asList(eventLog.getEventTargetAdditionalObjectId()), Short.valueOf("4"), Short.valueOf("5"),
                    "out", "eHealth DSI Translation", "Output Data");
        }
        return message;
    }
}
