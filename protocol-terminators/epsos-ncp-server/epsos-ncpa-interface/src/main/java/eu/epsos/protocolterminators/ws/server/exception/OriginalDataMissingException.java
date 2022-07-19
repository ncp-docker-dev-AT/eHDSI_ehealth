package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class OriginalDataMissingException extends NIException {
	private static final long serialVersionUID = 4254468101664118588L;

	public OriginalDataMissingException(String message) {
		super(OpenNCPErrorCode.ERROR_ORIGINAL_DATA_MISSING, message);
	}

}
