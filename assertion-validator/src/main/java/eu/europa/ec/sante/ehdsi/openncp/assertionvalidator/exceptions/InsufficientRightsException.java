package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.epsos.util.ErrorCode;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;
    private final ErrorCode errorCode;

    public InsufficientRightsException() {
        errorCode = ErrorCode.ERROR_CODE_4703;
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
