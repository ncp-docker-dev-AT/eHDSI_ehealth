package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class WeakAuthenticationException extends NIException {
	private static final long serialVersionUID = -3723212345611879399L;

	public WeakAuthenticationException(String message) {
		super(ErrorCode.ERROR_CODE_4702.getCodeToString(), message);
	}

}
