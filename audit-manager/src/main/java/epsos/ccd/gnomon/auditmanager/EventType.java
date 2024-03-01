package epsos.ccd.gnomon.auditmanager;

import epsos.ccd.gnomon.auditmanager.auditmessagebuilders.*;
import net.RFC3881.AuditMessage;

public enum EventType {

    IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS("EHDSI-11", "ITI-55", "XCPD::CrossGatewayPatientDiscovery", new IdentificationServiceAuditMessageBuilder()),
    PATIENT_SERVICE_LIST("EHDSI-21", "ITI-38", "XCA::CrossGatewayQuery", new XCAListAuditMessageBuilder()),
    PATIENT_SERVICE_RETRIEVE("EHDSI-22", "ITI-39", "XCA::CrossGatewayRetrieve", new XCARetrieveAuditMessageBuilder()),
    ORDER_SERVICE_LIST("EHDSI-31", "ITI-38", "XCA::CrossGatewayQuery", new XCAListAuditMessageBuilder()),
    ORDER_SERVICE_RETRIEVE("EHDSI-32","ITI-39", "XCA::CrossGatewayRetrieve", new XCARetrieveAuditMessageBuilder()),
    ORCD_SERVICE_LIST("EHDSI-61", "ITI-38", "XCA::CrossGatewayQuery", new XCAListAuditMessageBuilder()),
    ORCD_SERVICE_RETRIEVE("EHDSI-62","ITI-39", "XCA::CrossGatewayRetrieve", new XCARetrieveAuditMessageBuilder()),
    DISPENSATION_SERVICE_INITIALIZE("EHDSI-41", "ITI-41", "XDR::ProvideandRegisterDocumentSet-b", new DispensationServiceAuditMessageBuilder()),
    DISPENSATION_SERVICE_DISCARD("EHDSI-42", "ITI-41", "XDR::ProvideandRegisterDocumentSet-b", new DispensationDiscardServiceAuditMessageBuilder()),
    HCP_AUTHENTICATION("EHDSI-91", "ITI-40", "XUA::ProvideX-UserAssertion", new HCPAuthenticationAuditMessageBuilder()),
    TRC_ASSERTION("EHDSI-92", "EHDSI-92", "ncp::TrcAssertion", new TRCAssertionAuditMessageBuilder()),
    NOK_ASSERTION("EHDSI-96", "EHDSI-96", "ncp::NokAssertion", new NOKAssertionAuditMessageBuilder()),
    NCP_TRUSTED_SERVICE_LIST("EHDSI-93", "EHDSI-93", "ncpConfigurationManager::ImportNSL", new NCPTrustedServiceListAuditMessageBuilder()),
    PIVOT_TRANSLATION("EHDSI-94", "EHDSI-94", "ncpTransformationMgr::Translate", new PivotTranslationAuditMessageBuilder()),
    SMP_QUERY("EHDSI-193", "EHDSI-193", "SMP::Query", new SMPAuditMessageBuilder()),
    SMP_PUSH("EHDSI-194", "EHDSI-194", "SMP::Push", new SMPAuditMessageBuilder()),
    COMMUNICATION_FAILURE("EHDSI-CF", "EHDSI-CF", "epsosCommunicationFailure", new CommunicationFailureAuditMessageBuilder());

    private final String code;
    private final String iheCode;
    private final String iheTransactionName;

    private final AuditMessageBuilder builder;

    public String getCode() {
        return code;
    }

    public String getIheCode() {
        return iheCode;
    }

    public String getIheTransactionName() {
        return iheTransactionName;
    }

    public AuditMessageBuilder getBuilder() { return builder; }

    EventType(String code,
              String iheCode,
              String iheTransactionName,
              AuditMessageBuilder builder) {
        this.code = code;
        this.iheCode = iheCode;
        this.iheTransactionName = iheTransactionName;
        this.builder = builder;
    }

    public AuditMessage buildAuditMessage(EventLog eventLog) {
        return getBuilder().build(eventLog);
    }
}
