package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class UnknownSignifierException extends NIException {

	private static final long serialVersionUID = -8404675303540764793L;

	public UnknownSignifierException(String message) {
		super(ErrorCode.ERROR_CODE_4202.getCodeToString(), message);
	}
}
