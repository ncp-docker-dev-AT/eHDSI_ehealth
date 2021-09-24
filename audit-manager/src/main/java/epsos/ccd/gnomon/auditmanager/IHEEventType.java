package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventType of the AuditMessage.
 * One of the available eHDSI event IDs.
 * <p>
 * EHDSI-11: IdentificationService
 * EHDSI-21: PatientService
 * EHDSI-31: OrderService
 * EHDSI-41: DispensationServiceInitialize
 * EHDSI-42: DispensationServiceDiscard
 * EHDSI-51: ConsentServicePut
 * EHDSI-52: ConsentServiceDiscard
 * EHDSI-53: ConsentServicePin
 * EHDSI-91: HcpAuthentication
 * EHDSI-92: TRCAssertion
 * EHDSI-93: NCPTrustedServiceList
 * EHDSI-94: PivotTranslation
 * EHDSI-CF: CommunicationFailure
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
public enum IHEEventType {

    IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS("ITI-55"),
    PATIENT_SERVICE_LIST("ITI-38"),
    PATIENT_SERVICE_RETRIEVE("ITI-39"),
    ORDER_SERVICE_LIST("ITI-38"),
    ORDER_SERVICE_RETRIEVE("ITI-39"),
    ORCD_SERVICE_LIST("ITI-38"),
    ORCD_SERVICE_RETRIEVE("ITI-39"),
    DISPENSATION_SERVICE_INITIALIZE("ITI-41"),
    DISPENSATION_SERVICE_DISCARD("ITI-41"),
    CONSENT_SERVICE_PUT("ITI-41"),
    CONSENT_SERVICE_DISCARD("ITI-41"),
    CONSENT_SERVICE_PIN("EHDSI-53"),
    HCP_AUTHENTICATION("ITI-40"),
    TRC_ASSERTION("EHDSI-92"),
    NOK_ASSERTION("EHDSI-96"),
    NCP_TRUSTED_SERVICE_LIST("EHDSI-93"),
    PIVOT_TRANSLATION("EHDSI-94"),
    COMMUNICATION_FAILURE("EHDSI-CF"),
    PAC_RETRIEVE("EHDSI-95"),
    HCER_PUT("EHDSI-96"),
    MRO_LIST("ITI-38"),
    MRO_RETRIEVE("ITI-39"),
    SMP_QUERY("EHDSI-193"),
    SMP_PUSH("EHDSI-194");

    private final String code;

    IHEEventType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
