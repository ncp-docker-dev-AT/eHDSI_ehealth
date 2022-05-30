package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class NoDataException extends NIException {

	private static final long serialVersionUID = -6429013545938611399L;

	public NoDataException(String message) {
		super(ErrorCode.ERROR_CODE_1102.getCodeToString(), message);
	}

}
