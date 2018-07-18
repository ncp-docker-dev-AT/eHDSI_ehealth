package eu.epsos.validation.datamodel.cda;

import eu.epsos.validation.datamodel.common.ObjectType;
import tr.com.srdc.epsos.util.Constants;

/**
 * This enumerator gathers all the models used in the CDA Model Based Validator at EVS Client.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @deprecated
 */
@Deprecated
public enum CdaModel {

    ART_DECOR_CDA_PIVOT("eHDSI - ART-DECOR based CDA validation (PIVOT)"),
    ART_DECOR_CDA_FRIENDLY("eHDSI - ART-DECOR based CDA validation (FRIENDLY)"),
    ART_DECOR_SCANNED("eHDSI - ART-DECOR based Scanned Document"),
    BASIC_CDA("BASIC - CDA"),
    HCER("epSOS - HCER HealthCare Encounter Report"),
    MRO("epSOS - MRO Medication Related Overview"),
    PS_FRIENDLY("epSOS - Patient Summary Friendly"),
    PS_PIVOT("epSOS - Patient Summary Pivot"),
    SCANNED_DOCUMENT("epSOS - Scanned Document"),
    CONSENT("epSOS - eConsent"),
    ED_FRIENDLY("epSOS - eDispensation Friendly"),
    ED_PIVOT("epSOS - eDispensation Pivot"),
    EP_FRIENDLY("epSOS - ePrescription Friendly"),
    EP_PIVOT("epSOS - ePrescription Pivot");

    private String name;

    CdaModel(String s) {
        name = s;
    }

    public static CdaModel checkModel(String model) {

        for (CdaModel m : CdaModel.values()) {
            if (model.equals(m.toString())) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid CDA model value: " + model);
    }

    /**
     * This helper method will return a specific CDA model based on a document class code
     * (also choosing between friendly or pivot documents).
     *
     * @param classCode The document class code.
     * @param isPivot   The boolean flag stating if the document is pivot or
     *                  not.
     * @return the correspondent CDA model.
     * @deprecated
     */
    @Deprecated
    public static String obtainCdaModel(String classCode, boolean isPivot) {

        if (classCode == null || classCode.isEmpty()) {
            return null;
        }
        if (isPivot) {
            if (classCode.equals(Constants.MRO_CLASSCODE)) {
                return CdaModel.MRO.toString();
            }
            if (classCode.equals(Constants.PS_CLASSCODE) || classCode.equals(Constants.EP_CLASSCODE) || classCode.equals(Constants.ED_CLASSCODE)) {
                return CdaModel.ART_DECOR_CDA_PIVOT.toString();
            }
            if (classCode.equals(Constants.HCER_CLASSCODE)) {
                return CdaModel.HCER.toString();
            }
            if (classCode.equals(Constants.CONSENT_CLASSCODE)) {
                return CdaModel.CONSENT.toString();
            }
        } else {
            if (classCode.equals(Constants.MRO_CLASSCODE)) {
                return CdaModel.MRO.toString();
            }
            if (classCode.equals(Constants.PS_CLASSCODE) || classCode.equals(Constants.EP_CLASSCODE) || classCode.equals(Constants.ED_CLASSCODE)) {
                return CdaModel.ART_DECOR_CDA_FRIENDLY.toString();
            }
            if (classCode.equals(Constants.HCER_CLASSCODE)) {
                return CdaModel.HCER.toString();
            }
            if (classCode.equals(Constants.CONSENT_CLASSCODE)) {
                return CdaModel.CONSENT.toString();
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public ObjectType getObjectType() {
        return ObjectType.CDA;
    }
}
