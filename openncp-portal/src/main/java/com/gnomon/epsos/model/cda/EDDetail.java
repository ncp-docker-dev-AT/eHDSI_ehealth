package com.gnomon.epsos.model.cda;

public class EDDetail {

    private String relativePrescriptionLineId;
    private String dispensedQuantity;
    private String dispensedQuantityUnit;
    private String dispensedNumberOfPackages;
    private String medicineBarcode;
    private String medicineTainiaGnisiotitas;
    private String medicineEofCode;
    private String medicineCommercialName;
    private String medicinePackageFormCode;
    private String medicinePackageFormCodeDescription;
    private String medicineFormCode;
    private String medicineFormCodeDescription;
    private String medicineCapacityQuantity;
    private String medicineDrastikiATCCode;
    private String medicineDrastikiName;
    private String patientInstructions;
    private String medicinePrice;
    private String medicineRefPrice;
    private String patientParticipation;
    private String tameioParticipation;
    private String patientDifference;
    private String medicineExecutionCase;
    private boolean substituted;
    public String getRelativePrescriptionLineId() {
        return relativePrescriptionLineId;
    }

    public void setRelativePrescriptionLineId(String relativePrescriptionLineId) {
        this.relativePrescriptionLineId = relativePrescriptionLineId;
    }

    public String getDispensedQuantity() {
        return dispensedQuantity;
    }

    public void setDispensedQuantity(String dispensedQuantity) {
        this.dispensedQuantity = dispensedQuantity;
    }

    public String getDispensedQuantityUnit() {
        return dispensedQuantityUnit;
    }

    public void setDispensedQuantityUnit(String dispensedQuantityUnit) {
        this.dispensedQuantityUnit = dispensedQuantityUnit;
    }

    public String getDispensedNumberOfPackages() {
        return dispensedNumberOfPackages;
    }

    public void setDispensedNumberOfPackages(String dispensedNumberOfPackages) {
        this.dispensedNumberOfPackages = dispensedNumberOfPackages;
    }

    public String getMedicineBarcode() {
        return medicineBarcode;
    }

    public void setMedicineBarcode(String medicineBarcode) {
        this.medicineBarcode = medicineBarcode;
    }

    public String getMedicineEofCode() {
        return medicineEofCode;
    }

    public void setMedicineEofCode(String medicineEofCode) {
        this.medicineEofCode = medicineEofCode;
    }

    public String getMedicineCommercialName() {
        return medicineCommercialName;
    }

    public void setMedicineCommercialName(String medicineCommercialName) {
        this.medicineCommercialName = medicineCommercialName;
    }

    public String getMedicineFormCode() {
        return medicineFormCode;
    }

    public void setMedicineFormCode(String medicineFormCode) {
        this.medicineFormCode = medicineFormCode;
    }

    public String getMedicineFormCodeDescription() {
        return medicineFormCodeDescription;
    }

    public void setMedicineFormCodeDescription(String medicineFormCodeDescription) {
        this.medicineFormCodeDescription = medicineFormCodeDescription;
    }

    public String getMedicineCapacityQuantity() {
        return medicineCapacityQuantity;
    }

    public void setMedicineCapacityQuantity(String medicineCapacityQuantity) {
        this.medicineCapacityQuantity = medicineCapacityQuantity;
    }

    public String getMedicineDrastikiATCCode() {
        return medicineDrastikiATCCode;
    }

    public void setMedicineDrastikiATCCode(String medicineDrastikiATCCode) {
        this.medicineDrastikiATCCode = medicineDrastikiATCCode;
    }

    public String getMedicineDrastikiName() {
        return medicineDrastikiName;
    }

    public void setMedicineDrastikiName(String medicineDrastikiName) {
        this.medicineDrastikiName = medicineDrastikiName;
    }

    public String getPatientInstructions() {
        return patientInstructions;
    }

    public void setPatientInstructions(String patientInstructions) {
        this.patientInstructions = patientInstructions;
    }

    public String getMedicinePrice() {
        return medicinePrice;
    }

    public void setMedicinePrice(String medicinePrice) {
        this.medicinePrice = medicinePrice;
    }

    public String getMedicineRefPrice() {
        return medicineRefPrice;
    }

    public void setMedicineRefPrice(String medicineRefPrice) {
        this.medicineRefPrice = medicineRefPrice;
    }

    public String getPatientParticipation() {
        return patientParticipation;
    }

    public void setPatientParticipation(String patientParticipation) {
        this.patientParticipation = patientParticipation;
    }

    public String getTameioParticipation() {
        return tameioParticipation;
    }

    public void setTameioParticipation(String tameioParticipation) {
        this.tameioParticipation = tameioParticipation;
    }

    public String getPatientDifference() {
        return patientDifference;
    }

    public void setPatientDifference(String patientDifference) {
        this.patientDifference = patientDifference;
    }

    public String getMedicineExecutionCase() {
        return medicineExecutionCase;
    }

    public void setMedicineExecutionCase(String medicineExecutionCase) {
        this.medicineExecutionCase = medicineExecutionCase;
    }

    public String getMedicineTainiaGnisiotitas() {
        return medicineTainiaGnisiotitas;
    }

    public void setMedicineTainiaGnisiotitas(String medicineTainiaGnisiotitas) {
        this.medicineTainiaGnisiotitas = medicineTainiaGnisiotitas;
    }

    public String getMedicinePackageFormCode() {
        return medicinePackageFormCode;
    }

    public void setMedicinePackageFormCode(String medicinePackageFormCode) {
        this.medicinePackageFormCode = medicinePackageFormCode;
    }

    public String getMedicinePackageFormCodeDescription() {
        return medicinePackageFormCodeDescription;
    }

    public void setMedicinePackageFormCodeDescription(
            String medicinePackageFormCodeDescription) {
        this.medicinePackageFormCodeDescription = medicinePackageFormCodeDescription;
    }

    @Override
    public String toString() {
        return "EDDetail{" +
                "relativePrescriptionLineId='" + relativePrescriptionLineId + '\'' +
                ", dispensedQuantity='" + dispensedQuantity + '\'' +
                ", dispensedQuantityUnit='" + dispensedQuantityUnit + '\'' +
                ", dispensedNumberOfPackages='" + dispensedNumberOfPackages + '\'' +
                ", medicineBarcode='" + medicineBarcode + '\'' +
                ", medicineTainiaGnisiotitas='" + medicineTainiaGnisiotitas + '\'' +
                ", medicineEofCode='" + medicineEofCode + '\'' +
                ", medicineCommercialName='" + medicineCommercialName + '\'' +
                ", medicinePackageFormCode='" + medicinePackageFormCode + '\'' +
                ", medicinePackageFormCodeDescription='" + medicinePackageFormCodeDescription + '\'' +
                ", medicineFormCode='" + medicineFormCode + '\'' +
                ", medicineFormCodeDescription='" + medicineFormCodeDescription + '\'' +
                ", medicineCapacityQuantity='" + medicineCapacityQuantity + '\'' +
                ", medicineDrastikiATCCode='" + medicineDrastikiATCCode + '\'' +
                ", medicineDrastikiName='" + medicineDrastikiName + '\'' +
                ", patientInstructions='" + patientInstructions + '\'' +
                ", medicinePrice='" + medicinePrice + '\'' +
                ", medicineRefPrice='" + medicineRefPrice + '\'' +
                ", patientParticipation='" + patientParticipation + '\'' +
                ", tameioParticipation='" + tameioParticipation + '\'' +
                ", patientDifference='" + patientDifference + '\'' +
                ", medicineExecutionCase='" + medicineExecutionCase + '\'' +
                ", substituted=" + substituted +
                '}';
    }

    /**
     * A method returning if the detail is substituted.
     *
     * @return if the detail is substituted.
     */
    public boolean isSubstituted() {
        return substituted;
    }

    /**
     * A method setting the detailed as substituted.
     *
     * @param substituted the substitution flag to set.
     */
    public void setSubstituted(boolean substituted) {
        this.substituted = substituted;
    }
}
