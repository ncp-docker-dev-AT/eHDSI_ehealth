package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available EPSOS event ids.
 * <p>
 * epsos-11: epsosIdentificationService
 * epsos-21: epsosPatientService
 * epsos-31: epsosOrderService
 * epsos-41: epsodDispensationServuceInitialize
 * epsos-42: epsodDispensationServuceDiscard
 * epsos-51: epsodConsentServicePut
 * epsos-52: epsodConsentServiceDiscard
 * epsos-53: epsodConsentServicePin
 * epsos-91: epsosHcpAuthentication
 * epsos-92: epsosTRCAssertion
 * epsos-93: epsosNCPTrustedServiceList
 * epsos-94: epsosPivotTranslation
 * epsos-cf: epsosCommunicationFailure
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
public enum IHEEventType {

    epsosIdentificationServiceFindIdentityByTraits("ITI-55"),
    epsosPatientServiceList("ITI-38"),
    epsosPatientServiceRetrieve("ITI-39"),
    epsosOrderServiceList("ITI-38"),
    epsosOrderServiceRetrieve("ITI-39"),
    epsosDispensationServiceInitialize("ITI-41"),
    epsosDispensationServiceDiscard("ITI-41"),
    epsosConsentServicePut("ITI-41"),
    epsosConsentServiceDiscard("ITI-41"),
    epsosConsentServicePin("epsos-53"),
    epsosHcpAuthentication("ITI-40"),
    epsosTRCAssertion("epsos-92"),
    epsosNCPTrustedServiceList("epsos-93"),
    epsosPivotTranslation("epsos-94"),
    epsosCommunicationFailure("epsos-cf"),
    epsosPACRetrieve("epsos-95"),
    epsosHCERPut("epsos-96"),
    epsosMroList("ITI-38"),
    epsosMroRetrieve("ITI-39"),
    ehealthSMPQuery("ehealth-193"),
    ehealthSMPPush("ehealth-194");

    private String code;

    IHEEventType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
