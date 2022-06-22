package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class InvalidDispensationException extends NIException {
	private static final long serialVersionUID = -4968105055699416626L;

	public InvalidDispensationException(String code, String message) {
		super(OpenncpErrorCode.ERROR_EP_ALREADY_DISPENSED, message);
	}

}
