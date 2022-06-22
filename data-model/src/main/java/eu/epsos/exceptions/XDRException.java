package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

/**
 * Holds exceptions originated in the XDR Submit process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XDRException extends ExceptionWithContext {

    public XDRException(OpenncpErrorCode openncpErrorCode, Throwable e) {
        super(openncpErrorCode, e);
    }

    public XDRException(String message, OpenncpErrorCode openncpErrorCode, String codeContext) {
        super(message, openncpErrorCode, codeContext);
    }

}
