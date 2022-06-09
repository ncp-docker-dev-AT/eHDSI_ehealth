package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private EhdsiErrorCode ehdsiErrorCode;

    public AssertionValidationException() {
        super();
        this.ehdsiErrorCode = EhdsiErrorCode.EHDSI_ERROR_1001;
    }

    public AssertionValidationException(String message) {
        super();
    }

    @Override
    public String getMessage() {
        return ehdsiErrorCode.getMessage();
    }

    public String getCode() {
        return ehdsiErrorCode.getCodeToString();
    }

    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
    }
}
