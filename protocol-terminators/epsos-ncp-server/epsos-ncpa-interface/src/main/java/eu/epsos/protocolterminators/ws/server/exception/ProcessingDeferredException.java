package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class ProcessingDeferredException extends NIException {

	private static final long serialVersionUID = 4872216168488255110L;

	public ProcessingDeferredException(String message) {
		super(ErrorCode.ERROR_CODE_2201.getCodeToString(), message);
	}
}
