package eu.epsos.pt.cc;

public class ClientConnectorException extends RuntimeException {

    private static final long serialVersionUID = -7921992152572796564L;

    public ClientConnectorException() {
        super();
    }

    public ClientConnectorException(String message) {
        super(message);
    }

    public ClientConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientConnectorException(Throwable cause) {
        super(cause);
    }

    protected ClientConnectorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
