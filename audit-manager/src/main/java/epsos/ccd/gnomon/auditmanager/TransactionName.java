package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available eHDSI event ids:
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
public enum TransactionName {

    IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS("IdentityService::FindIdentityByTraits"),
    PATIENT_SERVICE_LIST("PatientService::List"),
    PATIENT_SERVICE_RETRIEVE("PatientService::Retrieve"),
    ORDER_SERVICE_LIST("OrderService::List"),
    ORDER_SERVICE_RETRIEVE("OrderService::Retrieve"),
    ORCD_SERVICE_LIST("OrCDService::List"),
    ORCD_SERVICE_RETRIEVE("OrCDService::Retrieve"),
    DISPENSATION_SERVICE_INITIALIZE("DispensationService::Initialize"),
    DISPENSATION_SERVICE_DISCARD("DispensationService::Discard"),
    CONSENT_SERVICE_PUT("ConsentService::Put"),
    CONSENT_SERVICE_DISCARD("ConsentService::Discard"),
    CONSENT_SERVICE_PIN("ConsentService::PIN"),
    HCP_AUTHENTICATION("identityProvider::HPAuthentication"),
    TRC_ASSERTION("ncp::TrcAssertion"),
    NOK_ASSERTION("ncp::NokAssertion"),
    NCP_TRUSTED_SERVICE_LIST("ncpConfigurationManager::ImportNSL"),
    PIVOT_TRANSLATION("ncpTransformationMgr::Translate"),
    COMMUNICATION_FAILURE("CommunicationFailure"),
    PAC_RETRIEVE("PACRetrieve"),
    HCER_PUT("HCERService:Put"),
    MRO_SERVICE_LIST("MroService::List"),
    MRO_SERVICE_RETRIEVE("MroService::Retrieve"),
    SMP_QUERY("SMP::Query"),
    SMP_PUSH("SMP::Push"),
    ANOMALY_DETECTED("ncp:AnomalyDetected");

    private String code;

    TransactionName(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
