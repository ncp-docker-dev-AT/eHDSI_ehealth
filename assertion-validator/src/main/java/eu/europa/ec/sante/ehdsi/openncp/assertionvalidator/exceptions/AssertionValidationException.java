package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private EhdsiErrorCode ehdsiErrorCode;

    public AssertionValidationException() {
        super();
        this.ehdsiErrorCode = EhdsiErrorCode.EHDSI_ERROR_NOT_VALID_ASSERTION;
    }

    public AssertionValidationException(String message) {
        super();
    }

    @Override
    public String getMessage() {
        return ehdsiErrorCode.getDescription();
    }

    public String getCode() {
        return ehdsiErrorCode.getCode();
    }

    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
    }

    protected void setEhdsiCode(EhdsiErrorCode ehdsiErrorCode) {
        this.ehdsiErrorCode = ehdsiErrorCode;
    }

}
