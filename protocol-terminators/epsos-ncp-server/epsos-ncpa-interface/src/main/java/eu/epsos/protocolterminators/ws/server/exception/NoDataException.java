package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiErrorCode;

public class NoDataException extends NIException {

	private static final long serialVersionUID = -6429013545938611399L;

	public NoDataException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_1102, message);
	}

}
