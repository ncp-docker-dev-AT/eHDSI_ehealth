package eu.europa.ec.sante.ehdsi.openncp.configmanager;

@SuppressWarnings("squid:S2068")
public final class StandardProperties {

    private StandardProperties() {
    }

    // NCP Configuration Properties

    public static final String NCP_CERTIFICATES_DIRECTORY = "certificates.storepath";

    public static final String NCP_COUNTRY_PRINCIPAL_SUBDIVISION = "COUNTRY_PRINCIPAL_SUBDIVISION";

    public static final String NCP_COUNTRY = "ncp.country";

    public static final String NCP_EMAIL = "ncp.email";

    public static final String NCP_KEYSTORE = "SC_KEYSTORE_PATH";

    public static final String NCP_KEYSTORE_PASSWORD = "SC_KEYSTORE_PASSWORD";

    public static final String NCP_SERVER = "SERVER_IP";

    public static final String NCP_TRUSTSTORE = "TRUSTSTORE_PATH";

    public static final String NCP_TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";

    // SMP/SML Configuration Properties

    public static final String SMP_SML_ADMIN_URL = "SMP_ADMIN_URL";

    public static final String SMP_SML_CLIENT_KEY_ALIAS = "SC_SMP_CLIENT_PRIVATEKEY_ALIAS";

    public static final String SMP_SML_CLIENT_KEY_PASSWORD = "SC_SMP_CLIENT_PRIVATEKEY_PASSWORD";

    public static final String SMP_SML_DNS_DOMAIN = "SML_DOMAIN";

    public static final String SMP_SML_SUPPORT = "SMP_SUPPORT";

    public static final String SMP_SML_SUPPORT_EMAIL = "SMP_SUPPORT_EMAIL";
}
