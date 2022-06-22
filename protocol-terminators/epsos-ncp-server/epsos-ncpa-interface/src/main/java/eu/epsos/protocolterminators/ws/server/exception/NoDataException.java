package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class NoDataException extends NIException {

	private static final long serialVersionUID = -6429013545938611399L;

	public NoDataException(String message) {
		super(OpenncpErrorCode.ERROR_PS_NOT_FOUND, message);
	}

}
