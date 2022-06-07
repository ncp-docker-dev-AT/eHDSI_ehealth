package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiErrorCode;

public class OriginalDataMissingException extends NIException {
	private static final long serialVersionUID = 4254468101664118588L;

	public OriginalDataMissingException(String code, String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4107, message);
	}

}
