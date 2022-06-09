package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class DiscardDispensationRejectedException extends NIException {

    private static final long serialVersionUID = -9198359055009969925L;

    public DiscardDispensationRejectedException(String message) {
        super(EhdsiErrorCode.EHDSI_ERROR_4105, message);
    }

    public DiscardDispensationRejectedException() {
        this("No matching eDispensation was found");
    }
}
