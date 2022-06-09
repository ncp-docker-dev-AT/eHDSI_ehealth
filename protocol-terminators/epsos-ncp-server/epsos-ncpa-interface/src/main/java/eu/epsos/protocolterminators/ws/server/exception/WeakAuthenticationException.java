package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class WeakAuthenticationException extends NIException {
	private static final long serialVersionUID = -3723212345611879399L;

	public WeakAuthenticationException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4702, message);
	}

}
