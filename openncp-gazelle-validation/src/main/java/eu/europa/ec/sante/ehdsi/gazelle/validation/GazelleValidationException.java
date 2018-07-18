package eu.europa.ec.sante.ehdsi.gazelle.validation;

public class GazelleValidationException extends Exception {

    public GazelleValidationException(String message) {
        super(message);
    }

    public GazelleValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
