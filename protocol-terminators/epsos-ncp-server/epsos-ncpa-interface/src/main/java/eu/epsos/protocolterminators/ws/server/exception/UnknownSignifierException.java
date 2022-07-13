package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class UnknownSignifierException extends NIException {

	private static final long serialVersionUID = -8404675303540764793L;

	public UnknownSignifierException(String message) {
		super(OpenNCPErrorCode.ERROR_GENERIC_SERVICE_SIGNIFIER_UNKNOWN, message);
	}
}
