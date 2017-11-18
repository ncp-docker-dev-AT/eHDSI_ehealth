package eu.europa.ec.sante.ehdsi.tsam.sync;

public class TsamSyncException extends RuntimeException {

    public TsamSyncException(String message) {
        super(message);
    }

    public TsamSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
