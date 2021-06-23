package fi.kela.se.epsos.data.model;

public class EpListParam {
    private boolean dispensable;
    private String atcCode;
    private String doseFormCode;
    private String strength;

    public EpListParam(boolean dispensable, String atcCode, String doseFormCode, String strength) {
        this.dispensable = dispensable;
        this.atcCode = atcCode;
        this.doseFormCode = doseFormCode;
        this.strength = strength;
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

    public void setDoseFormCode(String doseFormCode) {
        this.doseFormCode = doseFormCode;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}
