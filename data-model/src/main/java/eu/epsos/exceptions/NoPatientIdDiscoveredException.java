package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.ErrorCode;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class NoPatientIdDiscoveredException extends ExceptionWithContext {

    public NoPatientIdDiscoveredException(ErrorCode ehdsiErrorCode, Throwable cause) {
        super(ehdsiErrorCode, cause);
    }

    public NoPatientIdDiscoveredException(String message, ErrorCode ehdsiErrorCode, String context) {
        super(message, ehdsiErrorCode, context);
    }

}
