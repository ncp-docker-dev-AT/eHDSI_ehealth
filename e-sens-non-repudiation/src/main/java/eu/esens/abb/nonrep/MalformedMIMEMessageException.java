package eu.esens.abb.nonrep;

public class MalformedMIMEMessageException extends Exception {

    private static final long serialVersionUID = 2728932912021356485L;

    public MalformedMIMEMessageException() {
    }

    public MalformedMIMEMessageException(String message) {
        super(message);
    }

    public MalformedMIMEMessageException(Throwable cause) {
        super(cause);
    }

    public MalformedMIMEMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
