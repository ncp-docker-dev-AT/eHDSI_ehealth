package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available eHDSI event ids:
 * <p>
 * epsos-11:    epsosIdentificationService
 * epsos-21:    epsosPatientServiceList
 * epsos-22:    epsosPatientServiceRetrieve
 * epsos-31:    epsosOrderServiceList
 * epsos-32:    epsosOrderServiceRetrieve
 * epsos-41:    epsosDispensationServiceInitialize
 * epsos-42:    epsosDispensationServiceDiscard
 * epsos-51:    epsosConsentServicePut
 * epsos-52:    epsosConsentServiceDiscard
 * epsos-53:    epsosConsentServicePin
 * epsos-91:    epsosHcpAuthentication
 * epsos-92:    epsosTRCAssertion
 * epsos-93:    epsosNCPTrustedServiceList
 * epsos-94:    epsosPivotTranslation
 * epsos-95:    epsosPACRetrieve
 * epsos-96:    epsosHCERPut
 * ehealth-193: ehealthSMPQuery
 * ehealth-194: ehealthSMPPush
 * epsos-cf:    epsosCommunicationFailure
 * ITI-38:      epsosMroList
 * ITI-39       epsosMroRetrieve
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
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
    epsosPACRetrieve("epsos-95"),
    epsosHCERPut("epsos-96"),
    ehealthSMPQuery("ehealth-193"),
    ehealthSMPPush("ehealth-194"),
    epsosMroList("ITI-38"),
    epsosMroRetrieve("ITI-39"),
    epsosCommunicationFailure("epsos-cf");

    private String code;

    EventType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
