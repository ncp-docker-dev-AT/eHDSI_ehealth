package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public abstract class ExceptionWithContext extends Exception {

    private static final long serialVersionUID = 1L;

    private final OpenNCPErrorCode openncpErrorCode;

    //Use to pass information from the National Country A
    private String context;

    public ExceptionWithContext(OpenNCPErrorCode openncpErrorCode, Throwable cause) {
        super(cause);
        this.openncpErrorCode = openncpErrorCode;
    }

    public ExceptionWithContext(OpenNCPErrorCode openncpErrorCode, String message, String context) {
        super(message);
        this.openncpErrorCode = openncpErrorCode;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public OpenNCPErrorCode getOpenncpErrorCode() {
        return openncpErrorCode;
    }
}
