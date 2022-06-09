package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class UnknownSignifierException extends NIException {

	private static final long serialVersionUID = -8404675303540764793L;

	public UnknownSignifierException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4202, message);
	}
}
