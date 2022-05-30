package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class InvalidDispensationException extends NIException {
	private static final long serialVersionUID = -4968105055699416626L;

	public InvalidDispensationException(String code, String message) {
		super(ErrorCode.ERROR_CODE_4106.getCodeToString(), message);
	}

}
