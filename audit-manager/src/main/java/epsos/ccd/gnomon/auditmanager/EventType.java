package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage
 * One of the available EPSOS event ids
 * <p>
 * epsos-11: epsosIdentificationService
 * epsos-21: epsosPatientService
 * epsos-31: epsosOrderServiceList
 * epsos-32: epsosOrderServiceRetrieve
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
public enum EventType {

    epsosIdentificationServiceFindIdentityByTraits("epsos-11"),
    epsosPatientServiceList("epsos-21"),
    epsosPatientServiceRetrieve("epsos-22"),
    epsosOrderServiceList("epsos-31"),
    epsosOrderServiceRetrieve("epsos-32"),
    epsosDispensationServiceInitialize("epsos-41"),
    epsosDispensationServiceDiscard("epsos-42"),
    epsosConsentServicePut("epsos-51"),
    epsosConsentServiceDiscard("epsos-52"),
    epsosConsentServicePin("epsos-53"),
    epsosHcpAuthentication("epsos-91"),
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

    EventType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
