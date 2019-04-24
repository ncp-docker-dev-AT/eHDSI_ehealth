package tr.com.srdc.epsos.securityman.exceptions;

public class MissingFieldException extends AssertionValidationException {
	
    private static final long serialVersionUID = 9006271227090138486L;

    public MissingFieldException(String message) {
        super();
        this.setMessage(this.getMessage() + message);
    }
}
