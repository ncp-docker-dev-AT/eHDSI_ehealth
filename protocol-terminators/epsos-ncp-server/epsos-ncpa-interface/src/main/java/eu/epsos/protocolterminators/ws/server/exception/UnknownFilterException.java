package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class UnknownFilterException extends NIException {

	private static final long serialVersionUID = -483878991638325728L;

	public UnknownFilterException(String message) {
		super(ErrorCode.ERROR_CODE_4204.getCodeToString(), message);
	}
}
