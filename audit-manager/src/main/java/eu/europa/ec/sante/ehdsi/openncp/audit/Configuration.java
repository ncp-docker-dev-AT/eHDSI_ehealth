package eu.europa.ec.sante.ehdsi.openncp.audit;

public enum Configuration {

    AUDIT_REPOSITORY_URL("audit.repository.url"),
    AUDIT_REPOSITORY_PORT("audit.repository.port"),
    SEAL_KEYSTORE_FILE("NCP_SIG_KEYSTORE_PATH"),
    SEAL_KEYSTORE_PWD("NCP_SIG_KEYSTORE_PASSWORD"),
    SEAL_PRIVATE_KEY_PWD("NCP_SIG_PRIVATEKEY_PASSWORD"),
    SEAL_KEY_ALIAS("NCP_SIG_PRIVATEKEY_ALIAS"),
    TLS_KEYSTORE_FILE("SC_KEYSTORE_PATH"),
    TLS_KEYSTORE_PWD("SC_PRIVATEKEY_PASSWORD"),
    TLS_PRIVATE_KEY_PWD("SC_PRIVATEKEY_PASSWORD"),
    TLS_PRIVATE_KEY_ALIAS("SC_PRIVATEKEY_ALIAS"),
    TRUSTSTORE("TRUSTSTORE_PATH"),
    TRUSTSTORE_PWD("TRUSTSTORE_PASSWORD");


    private final String value;

    Configuration(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
