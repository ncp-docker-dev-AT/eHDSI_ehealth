package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class NoSignatureException extends NIException {
	private static final long serialVersionUID = -2813019150881427805L;

	public NoSignatureException(String message) {
		super(OpenncpErrorCode.ERROR_NO_SIGNATURE, message);
	}
}
