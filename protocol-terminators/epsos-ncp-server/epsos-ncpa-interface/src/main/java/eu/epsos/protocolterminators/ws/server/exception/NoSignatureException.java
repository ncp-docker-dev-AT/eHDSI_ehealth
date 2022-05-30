package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class NoSignatureException extends NIException {
	private static final long serialVersionUID = -2813019150881427805L;

	public NoSignatureException(String message) {
		super(ErrorCode.ERROR_CODE_4704.getCodeToString(), message);
	}
}
