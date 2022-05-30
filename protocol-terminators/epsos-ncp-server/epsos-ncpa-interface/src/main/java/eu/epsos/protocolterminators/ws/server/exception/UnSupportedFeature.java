package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class UnSupportedFeature extends NIException {

	private static final long serialVersionUID = -7044629789540910172L;

	public UnSupportedFeature(String message) {
		super(ErrorCode.ERROR_CODE_4201.getCodeToString(), message);
	}

}
