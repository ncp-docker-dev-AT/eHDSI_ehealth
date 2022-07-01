package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class PivotDataMissingException extends DocumentProcessingException {
	private static final long serialVersionUID = 8197669683897748059L;

	public PivotDataMissingException() {
		super(OpenncpErrorCode.ERROR_PIVOT_MISSING.getDescription());
		this.setOpenncpErrorCode(OpenncpErrorCode.ERROR_PIVOT_MISSING);
	}
}
