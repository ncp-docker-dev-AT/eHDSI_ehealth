package epsos.openncp.protocolterminator;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class ClientConnectorConsumerException extends Exception {


    private OpenncpErrorCode openncpErrorCode;
    String context;

    public ClientConnectorConsumerException(OpenncpErrorCode openncpErrorCode, String message, String context) {
        super(message);
        this.openncpErrorCode = openncpErrorCode;
        this.context = context;
    }

    public ClientConnectorConsumerException(OpenncpErrorCode openncpErrorCode, String message, String context, Throwable cause) {
        super(message, cause);
        this.openncpErrorCode = openncpErrorCode;
        this.context = context;
    }

    public OpenncpErrorCode getOpenncpErrorCode() {
        return openncpErrorCode;
    }

    public String getContext() {
        return context;
    }


}
