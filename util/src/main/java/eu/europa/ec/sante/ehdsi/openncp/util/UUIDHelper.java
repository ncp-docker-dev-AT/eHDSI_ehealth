package eu.europa.ec.sante.ehdsi.openncp.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHelper.class);

    private UUIDHelper() {
    }

    public static String encodeAsURN(String uuid) {

        uuid = StringUtils.removeAll(uuid, "urn:uuid:");
        uuid = StringUtils.removeAll(uuid, "_");
        uuid = StringUtils.removeAll(uuid, "-");

        Pattern pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

        String uuidTemp = pattern.matcher(uuid).replaceAll("$1-$2-$3-$4-$5");

        if (isUUIDValid(uuidTemp)) {
            return "urn:uuid:" + uuidTemp;
        } else {
            return "";
        }
    }

    private static boolean isUUIDValid(String message) {

        try {
            UUID uuid = java.util.UUID.fromString(message);
            LOGGER.debug("Valid UUID: '{}'", uuid);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("IllegalArgumentException: " + e.getMessage());
            return false;
        }
    }
}
