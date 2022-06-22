package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;

    private final OpenncpErrorCode openncpErrorCode;

    public InsufficientRightsException() {
        openncpErrorCode = OpenncpErrorCode.ERROR_INSUFFICIENT_RIGHTS;
    }

    @Override
    public String getMessage() {
        return openncpErrorCode.getDescription();
    }

    public String getCode() {
        return openncpErrorCode.getCode();
    }

    public OpenncpErrorCode getEhdsiCode() {
        return openncpErrorCode;
    }

}
