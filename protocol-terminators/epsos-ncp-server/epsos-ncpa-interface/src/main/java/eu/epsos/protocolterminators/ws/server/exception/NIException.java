package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class NIException extends Exception {

    private static final long serialVersionUID = 2148051521948531853L;
    private OpenncpErrorCode openncpErrorCode;
    private String message;
    private String codeSystem;

    public NIException(OpenncpErrorCode openncpErrorCode, String message) {
        this.openncpErrorCode = openncpErrorCode;
        this.message = message;
    }

    public NIException(OpenncpErrorCode openncpErrorCode, String message, String codeSystem) {
        this(openncpErrorCode, message);
        this.codeSystem = codeSystem;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public OpenncpErrorCode getEhdsiCode() {
        return openncpErrorCode;
    }

    public void setEhdsiCode(OpenncpErrorCode openncpErrorCode) {
        this.openncpErrorCode = openncpErrorCode;
    }
}
