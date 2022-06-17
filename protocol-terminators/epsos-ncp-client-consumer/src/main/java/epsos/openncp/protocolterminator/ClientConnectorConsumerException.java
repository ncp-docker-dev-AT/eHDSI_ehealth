package epsos.openncp.protocolterminator;

import eu.europa.ec.sante.ehdsi.constant.error.ErrorCode;

public class ClientConnectorConsumerException extends Exception {


    ErrorCode errorCode;
    String context;

    public ClientConnectorConsumerException(ErrorCode errorCode, String context) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.context = context;
    }

    public ClientConnectorConsumerException(ErrorCode errorCode, String context, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.context = context;
    }

    public ClientConnectorConsumerException(String message, ErrorCode errorCode, String context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}
