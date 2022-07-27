package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class DiscardDispensationRejectedException extends NIException {

    private static final long serialVersionUID = -9198359055009969925L;

    public DiscardDispensationRejectedException(String message) {
        super(OpenNCPErrorCode.ERROR_EP_NOT_MATCHING, message);
    }

    public DiscardDispensationRejectedException() {
        this("No matching eDispensation was found");
    }
}
