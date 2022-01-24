package epsos.openncp.protocolterminator;

public class ClientConnectorConsumerException extends Exception {

    String context;

    public ClientConnectorConsumerException(String message) {
        super(message);
    }

    public ClientConnectorConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientConnectorConsumerException(String message, String context, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}
