package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

public class InvalidFieldException extends AssertionValidationException {

    private static final long serialVersionUID = 8922552840552468444L;

    private final String messageDetailed;

    public InvalidFieldException(String messageDetailed) {
        super();
        this.messageDetailed = messageDetailed;
    }

    @Override
    public String getMessage() {
        return this.getEhdsiCode().getMessage() + messageDetailed;
    }

}
