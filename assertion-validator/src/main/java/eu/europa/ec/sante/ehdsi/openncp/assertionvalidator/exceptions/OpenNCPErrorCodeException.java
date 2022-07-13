package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public abstract class OpenNCPErrorCodeException extends Exception {

    public abstract OpenNCPErrorCode getErrorCode();
}
