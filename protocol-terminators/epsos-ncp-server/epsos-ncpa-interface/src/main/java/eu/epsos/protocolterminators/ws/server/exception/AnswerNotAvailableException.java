package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.error.EhiErrorCode;

public class AnswerNotAvailableException extends NIException {
	private static final long serialVersionUID = 7640387067196506306L;

	public AnswerNotAvailableException(String message) {
		super(null, message, null);
		this.setCodeSystem(EhiErrorCode.AnswerNotAvailable.getCodeSystem());
	}
}
