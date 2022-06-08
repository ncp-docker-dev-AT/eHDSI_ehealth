package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class NoConsentException extends NIException {

	private static final long serialVersionUID = 2194752799478399763L;

	public NoConsentException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4701, message);
	}

}
