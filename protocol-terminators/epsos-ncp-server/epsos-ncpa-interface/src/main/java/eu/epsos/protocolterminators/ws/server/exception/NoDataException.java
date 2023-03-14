package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class NoDataException extends NIException {

    private static final long serialVersionUID = -6429013545938611399L;

    public NoDataException(String message) {
        super(OpenNCPErrorCode.ERROR_PS_NOT_FOUND, message);
    }
}
