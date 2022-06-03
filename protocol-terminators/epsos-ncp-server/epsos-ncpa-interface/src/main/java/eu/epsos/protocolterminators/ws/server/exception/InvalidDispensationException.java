package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class InvalidDispensationException extends NIException {
	private static final long serialVersionUID = -4968105055699416626L;

	public InvalidDispensationException(String code, String message) {
		super(EhdsiCode.EHDSI_ERROR_4106, message);
	}

}
