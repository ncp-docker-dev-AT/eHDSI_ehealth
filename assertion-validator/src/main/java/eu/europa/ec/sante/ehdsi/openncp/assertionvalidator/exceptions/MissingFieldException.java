package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

public class MissingFieldException extends AssertionValidationException {
	
    private static final long serialVersionUID = 9006271227090138486L;

    private final String messageDetailed;

    public MissingFieldException(String messageDetailed) {
        super();
        this.messageDetailed = messageDetailed;
    }

    @Override
    public String getMessage() {
        return this.getEhdsiCode().getMessage() + messageDetailed;
    }
}
