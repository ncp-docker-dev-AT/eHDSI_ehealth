package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class ProcessingDeferredException extends DocumentProcessingException {
	private static final long serialVersionUID = -2416018860753255893L;

	public ProcessingDeferredException() {
		super("Documents were received but not processed");
		this.setOpenncpErrorCode(OpenNCPErrorCode.ERROR_DOCUMENT_NOT_PROCESSED);
	}
}
