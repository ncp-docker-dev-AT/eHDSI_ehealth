package eu.esens.abb.nonrep;

public class ObligationDischargeException extends Exception {

    private static final long serialVersionUID = 8580686800820475109L;

    public ObligationDischargeException() {
    }

    public ObligationDischargeException(String message) {
        super(message);
    }

    public ObligationDischargeException(Throwable cause) {
        super(cause);
    }

    public ObligationDischargeException(String message, Throwable cause) {
        super(message, cause);
    }
}
