package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class OriginalDataMissingException extends DocumentProcessingException {
	private static final long serialVersionUID = -1880772107399517210L;

	public OriginalDataMissingException() {
		super(OpenNCPErrorCode.ERROR_ORIGINAL_DATA_MISSING.getDescription());
		this.setOpenncpErrorCode(OpenNCPErrorCode.ERROR_ORIGINAL_DATA_MISSING);
	}
}
