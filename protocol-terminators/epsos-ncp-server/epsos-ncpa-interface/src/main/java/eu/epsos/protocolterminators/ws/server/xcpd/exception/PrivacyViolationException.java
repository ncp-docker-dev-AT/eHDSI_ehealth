package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class PrivacyViolationException extends XCPDNIException {
	private static final long serialVersionUID = -2047613187403340704L;

	public PrivacyViolationException(String message) {
		super(XCPDErrorCode.PrivacyViolation, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
