package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class NoMatchException extends NIException {

	private static final long serialVersionUID = -1353577541109670053L;

	public NoMatchException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_EP_NOT_MATCHING, message);
	}
}
