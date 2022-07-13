package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class NIException extends Exception {

    private static final long serialVersionUID = 2148051521948531853L;
    private OpenNCPErrorCode openncpErrorCode;
    private String message;

    public NIException(OpenNCPErrorCode openncpErrorCode, String message) {
        this.openncpErrorCode = openncpErrorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public OpenNCPErrorCode getOpenncpErrorCode() {
        return openncpErrorCode;
    }

    public void setOpenncpErrorCode(OpenNCPErrorCode openncpErrorCode) {
        this.openncpErrorCode = openncpErrorCode;
    }
}
