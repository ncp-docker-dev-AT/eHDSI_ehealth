package eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest.model;

import javax.validation.constraints.NotBlank;

public class PasswordReset {

    @NotBlank
    private String password;

    @NotBlank
    private String oldPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
