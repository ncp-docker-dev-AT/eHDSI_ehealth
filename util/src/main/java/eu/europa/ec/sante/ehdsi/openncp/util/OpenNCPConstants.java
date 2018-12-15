package eu.europa.ec.sante.ehdsi.openncp.util;

public class OpenNCPConstants {

    public static final String SERVER_EHEALTH_MODE = "server.ehealth.mode";
    public static final ServerMode NCP_SERVER_MODE;

    static {

        String mode = System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE);
        NCP_SERVER_MODE = ServerMode.fromValue(mode);
    }

    private OpenNCPConstants() {
    }
}
