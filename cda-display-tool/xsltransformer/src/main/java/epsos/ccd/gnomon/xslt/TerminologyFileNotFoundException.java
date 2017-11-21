package epsos.ccd.gnomon.xslt;

public class TerminologyFileNotFoundException extends Exception {

    public TerminologyFileNotFoundException(String message) {
        super(message);
    }

    public TerminologyFileNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
