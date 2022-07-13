package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class XCPDNIException extends NIException {

    private final XCPDErrorCode xcpdErrorCode;

    public XCPDNIException(XCPDErrorCode xcpdErrorCode, OpenNCPErrorCode openncpErrorCode, String message) {
        super(openncpErrorCode, message);
        this.xcpdErrorCode = xcpdErrorCode;
    }

    public XCPDErrorCode getXcpdErrorCode() {
        return xcpdErrorCode;
    }

    public String getCodeSystem() {
        return xcpdErrorCode.getCodeSystem();
    }
}
