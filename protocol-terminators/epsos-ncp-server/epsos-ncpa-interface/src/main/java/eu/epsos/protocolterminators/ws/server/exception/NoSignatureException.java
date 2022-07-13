package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class NoSignatureException extends NIException {
	private static final long serialVersionUID = -2813019150881427805L;

	public NoSignatureException(String message) {
		super(OpenNCPErrorCode.ERROR_NO_SIGNATURE, message);
	}
}
