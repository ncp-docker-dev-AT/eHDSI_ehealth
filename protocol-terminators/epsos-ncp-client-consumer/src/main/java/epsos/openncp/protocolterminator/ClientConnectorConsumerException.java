package epsos.openncp.protocolterminator;

public class ClientConnectorConsumerException extends RuntimeException {

    public ClientConnectorConsumerException(String message) {
        super(message);
    }

    public ClientConnectorConsumerException(String message, Throwable cause) {
        super(message, cause);
    }
}
