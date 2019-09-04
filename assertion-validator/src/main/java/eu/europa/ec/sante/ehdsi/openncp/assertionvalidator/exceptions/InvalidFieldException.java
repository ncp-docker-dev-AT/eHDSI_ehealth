package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

public class InvalidFieldException extends AssertionValidationException {

    private static final long serialVersionUID = 8922552840552468444L;

    public InvalidFieldException(String message) {
        super();
        this.setMessage(this.getMessage() + message);
    }
}
