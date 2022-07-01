package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public abstract class OpenncpErrorCodeException extends Exception {

    public abstract OpenncpErrorCode getOpenncpErrorCode();

}
