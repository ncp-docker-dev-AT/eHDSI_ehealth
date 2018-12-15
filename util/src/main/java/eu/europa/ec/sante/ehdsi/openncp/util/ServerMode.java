package eu.europa.ec.sante.ehdsi.openncp.util;


import org.apache.commons.lang3.StringUtils;

public enum ServerMode {

    /**
     * eHNCP is running in an unknown mode,  no value has been set in server startup script.
     */
    UNKNOWN,
    /**
     * eHNCP is running in test mode. Purpose is to allow/enable Clinical audits and Remote Validation.
     */
    TEST,
    /**
     * eHNCP is running a production mode, no Clinical audits generated.
     */
    PRODUCTION;

    /**
     * @param value
     * @return
     */
    public static ServerMode fromValue(String value) {

        try {

            if (StringUtils.isBlank(value)) {
                return ServerMode.UNKNOWN;
            }
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return ServerMode.UNKNOWN;
        }
    }
}
