package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class XcpdNIException extends NIException {

    private final XcpdErrorCode xcpdErrorCode;

    public XcpdNIException(XcpdErrorCode xcpdErrorCode, OpenncpErrorCode openncpErrorCode, String message) {
        super(openncpErrorCode, message);
        this.xcpdErrorCode = xcpdErrorCode;
    }

    public XcpdErrorCode getXcpdErrorCode(){
        return xcpdErrorCode;
    }

    public String getCodeSystem() {
        return xcpdErrorCode.getCodeSystem();
    }


}
