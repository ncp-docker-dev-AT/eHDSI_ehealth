package eu.epsos.protocolterminators.integrationtest.ihe.cda;

/**
 * This enumerator gathers all the models used in the CDA Model Based Validator at EVS Client.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum CdaModel {

    BASIC_CDA("BASIC - CDA"),
    CONSENT("epSOS - eConsent"),
    ED_FRIENDLY("epSOS - eDispensation Friendly"),
    ED_PIVOT("epSOS - eDispensation Pivot"),
    EP_FRIENDLY("epSOS - ePrescription Friendly"),
    EP_PIVOT("epSOS - ePrescription Pivot"),
    HCER("epSOS - HCER HealthCare Encounter Report"),
    MRO("epSOS - MRO Medication Related Overview"),
    PS_FRIENDLY("epSOS - Patient Summary Friendly"),
    PS_PIVOT("epSOS - Patient Summary Pivot"),
    SCANNED_DOCUMENT("epSOS - Scanned Document");

    private String name;

    CdaModel(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }
}
