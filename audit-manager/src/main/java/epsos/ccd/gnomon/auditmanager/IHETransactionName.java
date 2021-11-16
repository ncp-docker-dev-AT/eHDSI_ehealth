package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available eHDSI event ids:
 * <p>
 * EHDSI-11:    epsosIdentificationService
 * EHDSI-21:    epsosPatientService
 * EHDSI-31:    epsosOrderService
 * EHDSI-41:    epsosDispensationServiceInitialize
 * EHDSI-42:    epsosDispensationServiceDiscard
 * EHDSI-51:    epsosConsentServicePut
 * EHDSI-52:    epsosConsentServiceDiscard
 * EHDSI-53:    epsosConsentServicePin
 * EHDSI-91:    epsosHcpAuthentication
 * EHDSI-92:    epsosTRCAssertion
 * EHDSI-93:    epsosNCPTrustedServiceList
 * EHDSI-94:    epsosPivotTranslation
 * EHDSI-CF:    epsosCommunicationFailure
 * EHDSI-193:   SMP::Query
 * EHDSI-194:   SMP::Push
 */
public enum IHETransactionName {

    IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS("XCPD::CrossGatewayPatientDiscovery"),
    PATIENT_SERVICE_LIST("XCA::CrossGatewayQuery"),
    PATIENT_SERVICE_RETRIEVE("XCA::CrossGatewayRetrieve"),
    ORCD_SERVICE_LIST("XCA::CrossGatewayQuery"),
    ORCD_SERVICE_RETRIEVE("XCA::CrossGatewayRetrieve"),
    ORDER_SERVICE_LIST("XCA::CrossGatewayQuery"),
    ORDER_SERVICE_RETRIEVE("XCA::CrossGatewayRetrieve"),
    DISPENSATION_SERVICE_INITIALIZE("XDR::ProvideandRegisterDocumentSet-b"),
    DISPENSATION_SERVICE_DISCARD("XDR::ProvideandRegisterDocumentSet-b"),
    CONSENT_SERVICE_PUT("XDR::ProvideandRegisterDocumentSet-b"),
    CONSENT_SERVICE_DISCARD("XDR::ProvideandRegisterDocumentSet-b"),
    CONSENT_SERVICE_PIN("epsosConsentService::PIN"),
    HCP_AUTHENTICATION("XUA::ProvideX-UserAssertion"),
    TRC_ASSERTION("ncp::TrcAssertion"),
    NOK_ASSERTION("ncp::NokAssertion"),
    NCP_TRUSTED_SERVICE_LIST("ncpConfigurationManager::ImportNSL"),
    PIVOT_TRANSLATION("ncpTransformationMgr::Translate"),
    COMMUNICATION_FAILURE("epsosCommunicationFailure"),
    PAC_RETRIEVE("XCA::CrossGatewayRetrieve"),
    HCER_PUT("XDR::ProvideandRegisterDocumentSet-b"),
    MRO_SERVICE_LIST("XCA::CrossGatewayQuery"),
    MRO_SERVICE_RETRIEVE("XCA::CrossGatewayRetrieve"),
    SMP_QUERY("SMP::Query"),
    SMP_PUSH("SMP::Push");

    private final String code;

    IHETransactionName(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
