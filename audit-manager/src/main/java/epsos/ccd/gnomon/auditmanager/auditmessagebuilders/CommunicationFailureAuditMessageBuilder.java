package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditConstant;
import net.RFC3881.AuditMessage;
import net.RFC3881.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationFailureAuditMessageBuilder extends AbstractAuditMessageBuilder implements AuditMessageBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationFailureAuditMessageBuilder.class);

    @Override
    public AuditMessage build(EventLog eventLog) {
        AuditMessage message = null;
        try {
            ObjectFactory of = new ObjectFactory();
            message = of.createAuditMessage();
            addAuditSource(message, "N/A");
            addEventIdentification(message, eventLog.getEventType(), eventLog.getEI_TransactionName(),
                    eventLog.getEI_EventActionCode(), eventLog.getEI_EventDateTime(),
                    eventLog.getEI_EventOutcomeIndicator(), eventLog.getNcpSide());
            addHumanRequestor(message, eventLog.getHR_UserID(), eventLog.getHR_AlternativeUserID(), eventLog.getHR_RoleID(),
                    true);
            addService(message, eventLog.getSC_UserID(), true, AuditConstant.SERVICE_CONSUMER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Consumer", eventLog.getSourceip());
            addService(message, eventLog.getSP_UserID(), false, AuditConstant.SERVICE_PROVIDER,
                    AuditConstant.CODE_SYSTEM_EHDSI, "Service Provider", eventLog.getTargetip());
            addParticipantObject(message, eventLog.getPT_ParticipantObjectID(), Short.valueOf("1"), Short.valueOf("1"),
                    "Patient", "2", AuditConstant.DICOM, "Patient Number",
                    "Patient Number", eventLog.getQueryByParameter(), eventLog.getHciIdentifier());
            addError(message, eventLog.getEM_ParticipantObjectID(), eventLog.getEM_ParticipantObjectDetail(), Short.valueOf("2"),
                    Short.valueOf("3"), "9", "errormsg");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return message;
    }
}
