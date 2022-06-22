package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class MissingFieldException extends AssertionValidationException {
	
    private static final long serialVersionUID = 9006271227090138486L;

    private final String messageDetailed;

    public MissingFieldException(String messageDetailed) {
        super();
        this.messageDetailed = messageDetailed;
    }

    public MissingFieldException(OpenncpErrorCode openncpErrorCode, String messageDetailed) {
        super();
        this.setEhdsiCode(openncpErrorCode);
        this.messageDetailed = messageDetailed;
    }

    @Override
    public String getMessage() {
        return this.getEhdsiCode().getDescription() + messageDetailed;
    }
}
