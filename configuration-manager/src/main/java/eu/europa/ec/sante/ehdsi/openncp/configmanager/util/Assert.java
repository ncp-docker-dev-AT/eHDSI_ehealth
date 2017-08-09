package eu.europa.ec.sante.ehdsi.openncp.configmanager.util;

// TODO @Renaud Move to openncp-util
public class Assert {

    private Assert() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
