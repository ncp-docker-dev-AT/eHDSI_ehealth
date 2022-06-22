package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class ProcessingDeferredException extends NIException {

	private static final long serialVersionUID = 4872216168488255110L;

	public ProcessingDeferredException(String message) {
		super(OpenncpErrorCode.ERROR_DOCUMENT_NOT_PROCESSED, message);
	}
}
