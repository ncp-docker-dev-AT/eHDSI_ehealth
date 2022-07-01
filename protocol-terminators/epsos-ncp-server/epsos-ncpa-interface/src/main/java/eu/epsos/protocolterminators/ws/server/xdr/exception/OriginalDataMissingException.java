package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class OriginalDataMissingException extends DocumentProcessingException {
	private static final long serialVersionUID = -1880772107399517210L;

	public OriginalDataMissingException() {
		super(OpenncpErrorCode.ERROR_ORIGINAL_DATA_MISSING.getDescription());
		this.setOpenncpErrorCode(OpenncpErrorCode.ERROR_ORIGINAL_DATA_MISSING);
	}
}
