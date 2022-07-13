package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class NoMatchException extends NIException {

	private static final long serialVersionUID = -1353577541109670053L;

	public NoMatchException(String message) {
		super(OpenNCPErrorCode.ERROR_EP_NOT_MATCHING, message);
	}
}
