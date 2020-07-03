package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;

public class DiscardDispensationRejectedException extends NIException {

    private static final long serialVersionUID = -9198359055009969925L;

    public DiscardDispensationRejectedException() {
        super("4105", "No matching eDispensation was found");
    }

    public DiscardDispensationRejectedException(String message) {
        super("4105", message);
    }
}
