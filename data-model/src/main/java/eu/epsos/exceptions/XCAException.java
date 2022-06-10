package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

/**
 * Holds exceptions originated in the XCA Query / Retrieve process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XCAException extends ExceptionWithContext {

    public XCAException(String message, EhdsiErrorCode ehdsiErrorCode) {
        super(message, ehdsiErrorCode);
    }

    public XCAException(String message, EhdsiErrorCode ehdsiErrorCode, String context) {
        super(message, ehdsiErrorCode, context);
    }

}
