package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public final class OpenNcpProperties {

    private OpenNcpProperties() {
    }

    public static final String CERTIFICATES_DIRECTORY = "certificates.storepath";

    public static final String COUNTRY_PRINCIPAL_SUBDIVISION = "COUNTRY_PRINCIPAL_SUBDIVISION";

    public static final String NCP_COUNTRY = "ncp.country";

    public static final String NCP_EMAIL = "ncp.email";

    public static final String TRUSTSTORE = "TRUSTSTORE_PATH";

    public static final String TRUSTSTORE_PASS = "TRUSTSTORE_PASSWORD";

    public static final String SERVER_IP = "SERVER_IP";

    // SMP/SML

    public static final String SMP_SML_ADMIN_URL = "SMP_ADMIN_URL";

    public static final String SMP_SML_DNS_DOMAIN = "SML_DOMAIN";

    public static final String SMP_SML_SUPPORT = "SMP_SUPPORT";

    public static final String SMP_SML_SUPPORT_EMAIL = "SMP_SUPPORT_EMAIL";
}
