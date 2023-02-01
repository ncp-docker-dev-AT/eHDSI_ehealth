package fi.kela.se.epsos.data.model;

public class EpListParam {
    private boolean dispensable;
    private String atcCode;
    private String atcName;
    private String doseFormCode;
    private String doseFormName;
    private String strength;
    private EPDocumentMetaData.SubstitutionMetaData substitution;

    public EpListParam(boolean dispensable, String atcCode, String atcName, String doseFormCode, String doseFormName,
                       String strength, EPDocumentMetaData.SubstitutionMetaData substitution) {
        this.dispensable = dispensable;
        this.atcCode = atcCode;
        this.atcName = atcName;
        this.doseFormCode = doseFormCode;
        this.doseFormName = doseFormName;
        this.strength = strength;
        this.substitution = substitution;
    }

    public boolean isDispensable() {
        return dispensable;
    }

    public void setDispensable(boolean dispensable) {
        this.dispensable = dispensable;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public String getDoseFormCode() {
        return doseFormCode;
    }

    public void setDoseFormCode(String doseFormCode) {
        this.doseFormCode = doseFormCode;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getAtcName() {
        return atcName;
    }

    public String getDoseFormName() {
        return doseFormName;
    }

    public EPDocumentMetaData.SubstitutionMetaData getSubstitution() {
        return substitution;
    }

    public void setSubstitution(EPDocumentMetaData.SubstitutionMetaData substitution) {
        this.substitution = substitution;
    }
}
