package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class NoPatientIdDiscoveredException extends ExceptionWithContext {

    private XcpdErrorCode xcpdErrorCode;

    public NoPatientIdDiscoveredException(OpenncpErrorCode openncpErrorCode, Throwable cause) {
        super(openncpErrorCode, cause);
    }

    public NoPatientIdDiscoveredException(XcpdErrorCode xcpdErrorCode, OpenncpErrorCode openncpErrorCode, String message, String context) {
        super(openncpErrorCode, message, context);
        this.xcpdErrorCode = xcpdErrorCode;
    }

}
