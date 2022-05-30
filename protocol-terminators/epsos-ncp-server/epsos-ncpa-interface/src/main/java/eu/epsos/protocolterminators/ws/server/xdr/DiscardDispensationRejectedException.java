package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.util.ErrorCode;

public class DiscardDispensationRejectedException extends NIException {

    private static final long serialVersionUID = -9198359055009969925L;

    public DiscardDispensationRejectedException() {
        super(ErrorCode.ERROR_CODE_4105.getCodeToString(), "No matching eDispensation was found");
    }

    public DiscardDispensationRejectedException(String message) {
        super(ErrorCode.ERROR_CODE_4105.getCodeToString(), message);
    }
}
