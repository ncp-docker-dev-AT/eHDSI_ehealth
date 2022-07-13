package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class AssertionValidationException extends OpenNCPErrorCodeException {

    private static final long serialVersionUID = -6478057187366024151L;
    private OpenNCPErrorCode openncpErrorCode;

    public AssertionValidationException() {
        super();
        this.openncpErrorCode = OpenNCPErrorCode.ERROR_NOT_VALID_ASSERTION;
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

    public OpenNCPErrorCode getErrorCode() {
        return openncpErrorCode;
    }

    protected void setOpenncpErrorCode(OpenNCPErrorCode openncpErrorCode) {
        this.openncpErrorCode = openncpErrorCode;
    }

}
