package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public class PropertyNotFoundException extends ConfigurationManagerException {

    private static final long serialVersionUID = -8234737216376603813L;

    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyNotFoundException(Throwable cause) {
        super(cause);
    }
}
