package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class UnknownFilterException extends NIException {

	private static final long serialVersionUID = -483878991638325728L;

	public UnknownFilterException(String message) {
		super(OpenncpErrorCode.ERROR_UNKNOWN_FILTER, message);
	}
}
