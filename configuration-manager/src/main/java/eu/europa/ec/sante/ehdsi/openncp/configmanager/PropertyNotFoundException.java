package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public class PropertyNotFoundException extends ConfigurationManagerException {

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
