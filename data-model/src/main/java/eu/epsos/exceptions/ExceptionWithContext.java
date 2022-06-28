package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public abstract class ExceptionWithContext extends Exception {

    private static final long serialVersionUID = 1L;

    private final OpenncpErrorCode openncpErrorCode;

    //Use to pass information from the National Country A
    private String context;

    public ExceptionWithContext(OpenncpErrorCode openncpErrorCode, Throwable cause) {
        super(cause);
        this.openncpErrorCode = openncpErrorCode;
    }

    public ExceptionWithContext(OpenncpErrorCode openncpErrorCode, String message, String context) {
        super(message);
        this.openncpErrorCode = openncpErrorCode;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public OpenncpErrorCode getOpenncpErrorCode() {
        return openncpErrorCode;
    }
}
