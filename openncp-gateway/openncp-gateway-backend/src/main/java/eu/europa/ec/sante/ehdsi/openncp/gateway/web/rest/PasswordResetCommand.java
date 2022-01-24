package eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest;

import javax.validation.constraints.NotBlank;

public class PasswordResetCommand {

    @NotBlank
    private String token;

    @NotBlank
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
