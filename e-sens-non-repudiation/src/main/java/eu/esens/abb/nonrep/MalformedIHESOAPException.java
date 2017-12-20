package eu.esens.abb.nonrep;

public class MalformedIHESOAPException extends Exception {

    private static final long serialVersionUID = 1273575902438414667L;

    public MalformedIHESOAPException() {
    }

    public MalformedIHESOAPException(String message) {
        super(message);
    }

    public MalformedIHESOAPException(Throwable cause) {
        super(cause);
    }

    public MalformedIHESOAPException(String message, Throwable cause) {
        super(message, cause);
    }
}
