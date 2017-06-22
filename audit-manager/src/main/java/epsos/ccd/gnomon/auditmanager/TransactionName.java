package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage One of the
 * availbale epsos event ids epsos-11: epsosIdentificationService epsos-21:
 * epsosPatientService epsos-31: epsosOrderService epsos-41:
 * epsodDispensationServuceInitialize epsos-42: epsodDispensationServuceDiscard
 * epsos-51: epsodConsentServicePut epsos-52: epsodConsentServiceDiscard
 * epsos-53: epsodConsentServicePin epsos-91: epsosHcpAuthentication epsos-92:
 * epsosTRCAssertion epsos-93: epsosNCPTrustedServiceList epsos-94:
 * epsosPivotTranslation epsos-cf: epsosCommunicationFailure
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
