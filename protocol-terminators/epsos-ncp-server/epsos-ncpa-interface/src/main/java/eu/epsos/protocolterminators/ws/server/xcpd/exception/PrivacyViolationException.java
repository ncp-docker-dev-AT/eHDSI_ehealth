package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class PrivacyViolationException extends XcpdNIException {
	private static final long serialVersionUID = -2047613187403340704L;

	public PrivacyViolationException(String message) {
		super(XcpdErrorCode.PrivacyViolation, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
