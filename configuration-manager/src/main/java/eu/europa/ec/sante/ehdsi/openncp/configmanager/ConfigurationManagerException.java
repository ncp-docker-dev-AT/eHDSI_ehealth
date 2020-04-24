package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public class ConfigurationManagerException extends RuntimeException {

    private static final long serialVersionUID = -8449667586219303875L;

    public ConfigurationManagerException(String message) {
        super(message);
    }

    public ConfigurationManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationManagerException(Throwable cause) {
        super(cause);
    }
}
