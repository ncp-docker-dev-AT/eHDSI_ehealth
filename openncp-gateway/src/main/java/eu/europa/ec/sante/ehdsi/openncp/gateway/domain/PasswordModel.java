package eu.europa.ec.sante.ehdsi.openncp.gateway.domain;

public class PasswordModel {

    private String newPassword;
    private String matchPassword;

    public PasswordModel() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getMatchPassword() {
        return matchPassword;
    }

    public void setMatchPassword(String matchPassword) {
        this.matchPassword = matchPassword;
    }
}
