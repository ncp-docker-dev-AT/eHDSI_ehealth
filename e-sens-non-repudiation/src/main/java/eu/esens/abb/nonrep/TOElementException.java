package eu.esens.abb.nonrep;

public class TOElementException extends Exception {

    private static final long serialVersionUID = 5497948833180029330L;

    public TOElementException() {
    }

    public TOElementException(String message) {
        super(message);
    }

    public TOElementException(Throwable cause) {
        super(cause);
    }

    public TOElementException(String message, Throwable cause) {
        super(message, cause);
    }
}
