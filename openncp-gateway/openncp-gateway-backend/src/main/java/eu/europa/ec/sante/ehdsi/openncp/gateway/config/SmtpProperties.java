package eu.europa.ec.sante.ehdsi.openncp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
public class SmtpProperties {

    private String host = "localhost";
    private String port = "587";
    private String username = "username";
    private String password = "password";
    private String auth = "true";
    private final StartTls startTls = new StartTls();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public StartTls getStartTls() {
        return startTls;
    }

    public static class StartTls {
        private String enabled = "true";
        private String required = "true";

        public String isEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String isRequired() {
            return required;
        }

        public void setRequired(String required) {
            this.required = required;
        }
    }
}
