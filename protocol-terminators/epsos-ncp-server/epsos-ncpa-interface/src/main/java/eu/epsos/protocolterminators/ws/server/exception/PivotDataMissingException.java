package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class PivotDataMissingException extends NIException {
	private static final long serialVersionUID = -8929157858698790358L;

	public PivotDataMissingException(String message) {
		super(ErrorCode.ERROR_CODE_4108.getCodeToString(), message);
	}

}
