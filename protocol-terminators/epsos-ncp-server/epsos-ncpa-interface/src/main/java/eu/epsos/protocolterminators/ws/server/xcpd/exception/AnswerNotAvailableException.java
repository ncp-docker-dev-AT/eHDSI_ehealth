package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class AnswerNotAvailableException extends XCPDNIException {
	private static final long serialVersionUID = 7640387067196506306L;

	public AnswerNotAvailableException(String message) {
		super(XCPDErrorCode.AnswerNotAvailable, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
