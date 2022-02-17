package eu.europa.ec.sante.ehdsi.openncp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
public class SmtpProperties {

    private final Smtp smtp = new Smtp();
    private String host;
    private String port;
    private String username;
    private String password;

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

    public Smtp getSmtp() {
        return smtp;
    }

    public static class Smtp {
        private final StartTls startTls = new StartTls();
        private final Ssl ssl = new Ssl();
        private Boolean auth;
        private int connectionTimeout;
        private int writeTimeout;
        private int timeout;

        public Boolean getAuth() {
            return auth;
        }

        public void setAuth(Boolean auth) {
            this.auth = auth;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        public StartTls getStartTls() {
            return startTls;
        }

        public Ssl getSsl() {
            return ssl;
        }

        public static class StartTls {
            private Boolean enabled;
            private Boolean required;

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Boolean getRequired() {
                return required;
            }

            public void setRequired(Boolean required) {
                this.required = required;
            }
        }

        public static class Ssl {
            private Boolean enable;
            private String trust;

            public Boolean getEnable() {
                return enable;
            }

            public void setEnable(Boolean enable) {
                this.enable = enable;
            }

            public String getTrust() {
                return trust;
            }

            public void setTrust(String trust) {
                this.trust = trust;
            }
        }
    }
}
