package eu.epsos.validation.datamodel.audit;

import eu.epsos.validation.datamodel.common.ObjectType;

/**
 * This enumerator gathers all the models used in the Audit Messages Validator at EVS Client.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum AuditModel {

    EPSOS_IDENTIFICATION_SERVICE_AUDIT_SP("epSOS - Identification Service Audit (Service Provider)"),
    EPSOS2_PROVIDE_DATA_SERVICE_SC("epSOS-2 - Provide Data Service (Service Consumer)"),
    EPSOS_ISSUANCE_HCP_ASSERTION("epSOS - Issuance of a HCP Identity Assertion"),
    EPSOS2_IDENTIFICATION_SERVICE_AUDIT_SP("epSOS-2 - Identification Service Audit (Service Provider)"),
    EPSOS_ORDER_SERVICE_AUDIT_SC("epSOS - Order Service Audit (Service Consumer)"),
    EPSOS2_IMPORT_NCP_TRUSTED_LIST("epSOS-2 - Import of an epSOS NCP Trusted Service List"),
    EPSOS_ISSUANCE_TRC_ASSERTION("epSOS - Issuance of a Treatment Relationship Confirmation Assertion"),
    EPSOS_PATIENT_PRIVACY_AUDIT("epSOS - Patient Privacy Audit"),
    EPSOS_CONSENT_SERVICE_DISCARD_AUDIT_SC("epSOS - ConsentService:Discard() Audit (Service Consumer)"),
    EPSOS_CONSENT_SERVICE_PUT_AUDIT_SC("epSOS - ConsentService:put() Audit (Service Consumer)"),
    EPSOS2_HCP_ASSURANCE_AUDIT("epSOS-2 - HCP Assurance Audit"),
    EPSOS_ED_INIT_AUDIT_SC("epSOS - DispensationService:Initialize() Audit (Service Consumer)"),
    EPSOS_DOC_PATIENT_INFO_NOTIFICATION("epSOS - Documentation of the Patient Information Notification"),
    EPSOS2_ISSUANCE_HCP_ASSERTION("epSOS-2 - Issuance of a HCP Identity Assertion"),
    EPSOS2_FETCH_DOC_SERVICE_SP("epSOS-2 - Fetch Document Service (Service Provider)"),
    EPSOS2_FETCH_DOC_SERVICE_SC("epSOS-2 - Fetch Document Service (Service Consumer)"),
    EPSOS_ED_DISCARD_AUDITP_SP("epSOS - DispensationService:Discard() Audit (Service Provider)"),
    EPSOS_CS_PUT_AUDIT_SP("epSOS - ConsentService:put() Audit (Service Provider)"),
    EPSOS_PS_RETRIEVE_REQUEST_XCA("epSOS PatientService:retrieve - request (V1 XCA)"),
    EPSOS_OS_AUDIT_SP("epSOS - Order Service Audit (Service Provider)"),
    EPSOS_HCP_ASSURANCE_AUDIT("epSOS - HCP Assurance Audit"),
    EPSOS_PATIENT_SERVICE_AUDIT_SC("epSOS - Patient Service Audit (Service Consumer)"),
    EPSOS_PIVOT_TRANSLATION("epSOS - Pivot Translation of a Medical Document"),
    EPSOS_PATIENT_SERVICE_AUDIT_SP("epSOS - Patient Service Audit (Service Provider)"),
    EPSOS_PATIENT_ID_MAPPING_AUDIT("epSOS - Patient ID Mapping Audit"),
    EPSOS_ED_INIT_AUDIT_SP("epSOS - DispensationService:Initialize() Audit (Service Provider)"),
    EPSOS_ED_DISCARD_AUDIT_SC("epSOS - DispensationService:Discard() Audit (Service Consumer)"),
    EPSOS2_PIVOS_TRANSLATION("epSOS-2 - Pivot Translation of a Medical Document"),
    EPSOS2_ISSUANCE_TRC_ASSERTION("epSOS-2 - Issuance of a Treatment Relationship Confirmation Assertion"),
    EPSOS_CS_DISCARD_AUDIT_SP("epSOS - ConsentService:Discard() Audit (Service Provider)"),
    EPSOS2_PROVIDE_DATA_SERVICE_SP("epSOS-2 - Provide Data Service (Service Provider)"),
    EPSOS_IMPORT_NCP_TRUSTED_LIST("epSOS - Import of an epSOS NCP Trusted Service List");

    private String name;

    AuditModel(String s) {
        name = s;
    }

    public static AuditModel checkModel(String model) {

        for (AuditModel m : AuditModel.values()) {
            if (model.equals(m.toString())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public ObjectType getObjectType() {
        return ObjectType.AUDIT;
    }
}
