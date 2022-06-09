package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class PivotDataMissingException extends NIException {
	private static final long serialVersionUID = -8929157858698790358L;

	public PivotDataMissingException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4108, message);
	}

}
