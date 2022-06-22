package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

/**
 * Holds exceptions originated in the XCA Query / Retrieve process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XCAException extends ExceptionWithContext {

    public XCAException(String message, OpenncpErrorCode openncpErrorCode) {
        super(message, openncpErrorCode);
    }

    public XCAException(String message, OpenncpErrorCode openncpErrorCode, String context) {
        super(message, openncpErrorCode, context);
    }

}
