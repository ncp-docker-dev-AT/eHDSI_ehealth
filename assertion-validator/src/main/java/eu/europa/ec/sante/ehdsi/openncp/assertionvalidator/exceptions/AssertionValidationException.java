package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private EhdsiCode ehdsiCode;

    public AssertionValidationException() {
        super();
        this.ehdsiCode = EhdsiCode.EHDSI_ERROR_1001;
    }

    public AssertionValidationException(String message) {
        super();
    }

    @Override
    public String getMessage() {
        return ehdsiCode.getMessage();
    }

    public String getCode() {
        return ehdsiCode.getCodeToString();
    }

    public EhdsiCode getEhdsiCode() {
        return ehdsiCode;
    }
}
