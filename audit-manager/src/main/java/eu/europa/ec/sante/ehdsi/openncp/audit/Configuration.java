package eu.europa.ec.sante.ehdsi.openncp.audit;

public enum Configuration {

    AUDIT_REPOSITORY_URL("audit.repository.url"),
    AUDIT_REPOSITORY_PORT("audit.repository.port"),
    KEYSTORE_FILE("NCP_SIG_KEYSTORE_PATH"),
    KEYSTORE_PWD("NCP_SIG_KEYSTORE_PASSWORD"),
    TRUSTSTORE("TRUSTSTORE_PATH"),
    TRUSTSTORE_PWD("TRUSTSTORE_PASSWORD"),
    KEY_ALIAS("NCP_SIG_PRIVATEKEY_ALIAS");

    private String value;

    Configuration(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
