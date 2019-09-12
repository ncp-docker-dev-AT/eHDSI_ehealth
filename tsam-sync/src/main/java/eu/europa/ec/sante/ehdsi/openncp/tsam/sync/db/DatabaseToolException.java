package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db;

@SuppressWarnings("unused")
public class DatabaseToolException extends RuntimeException {

    public DatabaseToolException() {
        super();
    }

    public DatabaseToolException(String message) {
        super(message);
    }

    public DatabaseToolException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseToolException(Throwable cause) {
        super(cause);
    }
}
