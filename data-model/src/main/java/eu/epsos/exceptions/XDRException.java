package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

/**
 * Holds exceptions originated in the XDR Submit process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XDRException extends ExceptionWithContext {

    public XDRException(EhdsiErrorCode ehdsiErrorCode,Throwable e) {
        super(ehdsiErrorCode, e);
    }

    public XDRException(String message,  EhdsiErrorCode ehdsiErrorCode, String codeContext) {
        super(message, ehdsiErrorCode, codeContext);
    }

}
