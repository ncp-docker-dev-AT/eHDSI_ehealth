package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class NoConsentException extends NIException {

	private static final long serialVersionUID = 2194752799478399763L;

	public NoConsentException(String message) {
		super(ErrorCode.ERROR_CODE_4701.getCodeToString(), message);
	}

}
