package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.ErrorCode;

public abstract class ExceptionWithContext extends Exception {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private String context;

    public ExceptionWithContext(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExceptionWithContext(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public ExceptionWithContext(String message, ErrorCode errorCode, String context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
