package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private OpenncpErrorCode openncpErrorCode;

    public AssertionValidationException() {
        super();
        this.openncpErrorCode = OpenncpErrorCode.ERROR_NOT_VALID_ASSERTION;
    }

    public AssertionValidationException(String message) {
        super();
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

    protected void setEhdsiCode(OpenncpErrorCode openncpErrorCode) {
        this.openncpErrorCode = openncpErrorCode;
    }

}
