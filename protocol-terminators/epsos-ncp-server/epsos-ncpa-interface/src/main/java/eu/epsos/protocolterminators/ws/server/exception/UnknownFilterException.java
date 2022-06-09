package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.error.EhdsiErrorCode;

public class UnknownFilterException extends NIException {

	private static final long serialVersionUID = -483878991638325728L;

	public UnknownFilterException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4204, message);
	}
}
