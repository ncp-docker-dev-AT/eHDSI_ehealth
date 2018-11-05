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
public enum IHETransactionName {

    epsosIdentificationServiceFindIdentityByTraits("XCPD::CrossGatewayPatientDiscovery"),
    epsosPatientServiceList("XCA::CrossGatewayQuery"),
    epsosPatientServiceRetrieve("XCA::CrossGatewayRetrieve"),
    epsosOrderServiceList("XCA::CrossGatewayQuery"),
    epsosOrderServiceRetrieve("XCA::CrossGatewayRetrieve"),
    epsosDispensationServiceInitialize("XDR::ProvideandRegisterDocumentSet-b"),
    epsosDispensationServiceDiscard("XDR::ProvideandRegisterDocumentSet-b"),
    epsosConsentServicePut("XDR::ProvideandRegisterDocumentSet-b"),
    epsosConsentServiceDiscard("XDR::ProvideandRegisterDocumentSet-b"),
    epsosConsentServicePin("epsosConsentService::PIN"),
    epsosHcpAuthentication("XUA::ProvideX-UserAssertion"),
    epsosTRCAssertion("ncp::TrcAssertion"),
    epsosNCPTrustedServiceList("ncpConfigurationManager::ImportNSL"),
    epsosPivotTranslation("ncpTransformationMgr::Translate"),
    epsosCommunicationFailure("epsosCommunicationFailure"),
    epsosPACRetrieve("XCA::CrossGatewayRetrieve"),
    epsosHCERPut("XDR::ProvideandRegisterDocumentSet-b"),
    epsosMroServiceList("XCA::CrossGatewayQuery"),
    epsosMroServiceRetrieve("XCA::CrossGatewayRetrieve"),
    ehealthSMPQuery("SMP::Query"),
    ehealthSMPPush("SMP::Push");

    private String code;

    IHETransactionName(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
