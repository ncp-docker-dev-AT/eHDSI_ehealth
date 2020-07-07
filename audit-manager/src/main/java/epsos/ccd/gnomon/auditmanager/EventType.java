package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available eHDSI event ids:
 * <p>
 * EHDSI-11:    epsosIdentificationService
 * EHDSI-21:    epsosPatientServiceList
 * EHDSI-22:    epsosPatientServiceRetrieve
 * EHDSI-31:    epsosOrderServiceList
 * EHDSI-32:    epsosOrderServiceRetrieve
 * EHDSI-41:    epsosDispensationServiceInitialize
 * EHDSI-42:    epsosDispensationServiceDiscard
 * EHDSI-51:    epsosConsentServicePut
 * EHDSI-52:    epsosConsentServiceDiscard
 * EHDSI-53:    epsosConsentServicePin
 * EHDSI-91:    epsosHcpAuthentication
 * EHDSI-92:    epsosTRCAssertion
 * EHDSI-93:    epsosNCPTrustedServiceList
 * EHDSI-94:    epsosPivotTranslation
 * EHDSI-95:    epsosPACRetrieve
 * EHDSI-96:    epsosHCERPut
 * EHDSI-193: ehealthSMPQuery
 * EHDSI-194: ehealthSMPPush
 * EHDSI-CF:    epsosCommunicationFailure
 * ITI-38:      epsosMroList
 * ITI-39       epsosMroRetrieve
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 */
public enum EventType {

    IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS("EHDSI-11"),
    PATIENT_SERVICE_LIST("EHDSI-21"),
    PATIENT_SERVICE_RETRIEVE("EHDSI-22"),
    ORDER_SERVICE_LIST("EHDSI-31"),
    ORDER_SERVICE_RETRIEVE("EHDSI-32"),
    DISPENSATION_SERVICE_INITIALIZE("EHDSI-41"),
    DISPENSATION_SERVICE_DISCARD("EHDSI-42"),
    CONSENT_SERVICE_PUT("EHDSI-51"),
    CONSENT_SERVICE_DISCARD("EHDSI-52"),
    CONSENT_SERVICE_PIN("EHDSI-53"),
    HCP_AUTHENTICATION("EHDSI-91"),
    TRC_ASSERTION("EHDSI-92"),
    NCP_TRUSTED_SERVICE_LIST("EHDSI-93"),
    PIVOT_TRANSLATION("EHDSI-94"),
    PAC_RETRIEVE("EHDSI-95"),
    HCER_PUT("EHDSI-96"),
    SMP_QUERY("EHDSI-193"),
    SMP_PUSH("EHDSI-194"),
    MRO_LIST("ITI-38"),
    MRO_RETRIEVE("ITI-39"),
    COMMUNICATION_FAILURE("EHDSI-CF");

    private final String code;

    EventType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
