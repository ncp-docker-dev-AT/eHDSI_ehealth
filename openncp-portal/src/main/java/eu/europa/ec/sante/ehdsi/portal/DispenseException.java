package eu.europa.ec.sante.ehdsi.portal;

public class DispenseException extends RuntimeException {

    public DispenseException(String message) {
        super(message);
    }

    public DispenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
