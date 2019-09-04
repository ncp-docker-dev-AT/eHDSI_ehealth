package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

public class AssertionValidationException extends Exception {

    private static final long serialVersionUID = -6478057187366024151L;
    private String message;
    private String code;

    public AssertionValidationException() {
        super();
        this.message = "Assertion is not valid. ";
        this.code = "1001";
    }

    public AssertionValidationException(String message) {
        super();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
