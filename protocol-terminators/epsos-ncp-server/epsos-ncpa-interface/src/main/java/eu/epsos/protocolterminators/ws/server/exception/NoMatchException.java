package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class NoMatchException extends NIException {

	private static final long serialVersionUID = -1353577541109670053L;

	public NoMatchException(String message) {
		super(ErrorCode.ERROR_CODE_4105.getCodeToString(), message);
	}
}
