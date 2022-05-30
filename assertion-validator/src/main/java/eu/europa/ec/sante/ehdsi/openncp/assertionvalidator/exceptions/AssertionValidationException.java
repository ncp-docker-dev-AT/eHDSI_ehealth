package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.epsos.util.ErrorCode;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private ErrorCode errorCode;

    public AssertionValidationException() {
        super();
        this.errorCode = ErrorCode.ERROR_CODE_1001;
    }

    public AssertionValidationException(String message) {
        super();
    }

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }

    public String getCode() {
        return errorCode.getCodeToString();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }


}
