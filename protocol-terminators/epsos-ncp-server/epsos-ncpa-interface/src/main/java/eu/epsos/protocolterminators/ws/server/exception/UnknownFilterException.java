package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class UnknownFilterException extends NIException {

	private static final long serialVersionUID = -483878991638325728L;

	public UnknownFilterException(String message) {
		super(OpenNCPErrorCode.ERROR_UNKNOWN_FILTER, message);
	}
}
