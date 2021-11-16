package fi.kela.se.epsos.data.model;

public class EpListParam {
    private boolean dispensable;
    private String atcCode;
    private String atcName;
    private String doseFormCode;
    private String doseFormName;
    private String strength;
    private String substitution;

    public EpListParam(boolean dispensable, String atcCode, String atcName, String doseFormCode, String doseFormName, String strength, String substitution) {
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

    public void setDispensable(boolean dispensable) { this.dispensable = dispensable; }

    public String getAtcCode() {
        return atcCode;
    }

    public String getDoseFormCode() {
        return doseFormCode;
    }

    public String getStrength() {
        return strength;
    }

    public void setAtcCode(String atcCode) { this.atcCode = atcCode; }

    public String getAtcName() {
        return atcName;
    }

    public void setDoseFormCode(String doseFormCode) {
        this.doseFormCode = doseFormCode;
    }

    public String getDoseFormName() {
        return doseFormName;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getSubstitution() {
        return substitution;
    }

    public void setSubstitution(String substitution) {
        this.substitution = substitution;
    }
}
