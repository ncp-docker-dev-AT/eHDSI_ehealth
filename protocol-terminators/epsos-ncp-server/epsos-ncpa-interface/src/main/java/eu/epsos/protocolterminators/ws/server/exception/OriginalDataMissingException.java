package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class OriginalDataMissingException extends NIException {
	private static final long serialVersionUID = 4254468101664118588L;

	public OriginalDataMissingException(String message) {
		super(OpenncpErrorCode.ERROR_ORIGINAL_DATA_MISSING, message);
	}

}
