package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.cts;

@SuppressWarnings("unused")
public class CtsClientException extends RuntimeException {

    public CtsClientException() {
        super();
    }

    public CtsClientException(String message) {
        super(message);
    }

    public CtsClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CtsClientException(Throwable cause) {
        super(cause);
    }
}
