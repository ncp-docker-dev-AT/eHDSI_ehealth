package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class RenderingIncompleteException extends NIException {

	private static final long serialVersionUID = -8555270493456361182L;

	public RenderingIncompleteException(String message) {
		super(ErrorCode.ERROR_CODE_4101.getCodeToString(), message);
	}
}
