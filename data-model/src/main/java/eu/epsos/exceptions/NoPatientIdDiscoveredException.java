package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class NoPatientIdDiscoveredException extends ExceptionWithContext {

    private XCPDErrorCode xcpdErrorCode;

    public NoPatientIdDiscoveredException(OpenNCPErrorCode openncpErrorCode, Throwable cause) {
        super(openncpErrorCode, cause);
    }

    public NoPatientIdDiscoveredException(OpenNCPErrorCode openncpErrorCode, String message) {
        super(openncpErrorCode, message, null);
    }

    public NoPatientIdDiscoveredException(XCPDErrorCode xcpdErrorCode, OpenNCPErrorCode openncpErrorCode, String message, String context) {
        super(openncpErrorCode, message, context);
        this.xcpdErrorCode = xcpdErrorCode;
    }

}
