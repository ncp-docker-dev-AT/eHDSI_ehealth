package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;

public class PivotDataMissingException extends NIException {
	private static final long serialVersionUID = -8929157858698790358L;

	public PivotDataMissingException(String message) {
		super(OpenncpErrorCode.ERROR_PIVOT_MISSING, message);
	}

}
