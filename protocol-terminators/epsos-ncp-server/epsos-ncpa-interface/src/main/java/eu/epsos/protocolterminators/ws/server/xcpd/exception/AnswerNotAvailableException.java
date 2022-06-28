package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class AnswerNotAvailableException extends XcpdNIException {
	private static final long serialVersionUID = 7640387067196506306L;

	public AnswerNotAvailableException(String message) {
		super(XcpdErrorCode.AnswerNotAvailable, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
