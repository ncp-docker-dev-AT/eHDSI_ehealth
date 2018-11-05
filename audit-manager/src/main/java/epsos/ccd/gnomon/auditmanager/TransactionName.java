package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage
 * One of the available eHDSI event ids:
 * <p>
 * epsos-11:    epsosIdentificationService
 * epsos-21:    epsosPatientService
 * epsos-31:    epsosOrderService
 * epsos-41:    epsosDispensationServuceInitialize
 * epsos-42:    epsosDispensationServuceDiscard
 * epsos-51:    epsosConsentServicePut
 * epsos-52:    epsosConsentServiceDiscard
 * epsos-53:    epsosConsentServicePin
 * epsos-91:    epsosHcpAuthentication
 * epsos-92:    epsosTRCAssertion
 * epsos-93:    epsosNCPTrustedServiceList
 * epsos-94:    epsosPivotTranslation
 * epsos-cf:    epsosCommunicationFailure
 * ehealth-193: SMP::Query
 * ehealth-194: SMP::Push
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
public enum TransactionName {

    epsosIdentificationServiceFindIdentityByTraits("epsosIdentityService::FindIdentityByTraits"),
    epsosPatientServiceList("epsosPatientService::List"),
    epsosPatientServiceRetrieve("epsosPatientService::Retrieve"),
    epsosOrderServiceList("epsosOrderService::List"),
    epsosOrderServiceRetrieve("epsosOrderService::Retrieve"),
    epsosDispensationServiceInitialize("epsosDispensationService::Initialize"),
    epsosDispensationServiceDiscard("epsosDispensationService::Discard"),
    epsosConsentServicePut("epsosConsentService::Put"),
    epsosConsentServiceDiscard("epsosConsentService::Discard"),
    epsosConsentServicePin("epsosConsentService::PIN"),
    epsosHcpAuthentication("identityProvider::HPAuthentication"),
    epsosTRCAssertion("ncp::TrcAssertion"),
    epsosNCPTrustedServiceList("ncpConfigurationManager::ImportNSL"),
    epsosPivotTranslation("ncpTransformationMgr::Translate"),
    epsosCommunicationFailure("epsosCommunicationFailure"),
    epsosPACRetrieve("epsosPACRetrieve"),
    epsosHCERPut("epsosHCERService:Put"),
    epsosMroServiceList("epsosMroService::List"),
    epsosMroServiceRetrieve("epsosMroService::Retrieve"),
    ehealthSMPQuery("SMP::Query"),
    ehealthSMPPush("SMP::Push");

    private String code;

    TransactionName(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
