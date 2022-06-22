package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class UnknownSignifierException extends NIException {

	private static final long serialVersionUID = -8404675303540764793L;

	public UnknownSignifierException(String message) {
		super(OpenncpErrorCode.ERROR_UNKNOWN_SIGNIFIER, message);
	}
}
