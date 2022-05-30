package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class OriginalDataMissingException extends NIException {
	private static final long serialVersionUID = 4254468101664118588L;

	public OriginalDataMissingException(String code, String message) {
		super(ErrorCode.ERROR_CODE_4107.getCodeToString(), message);
	}

}
