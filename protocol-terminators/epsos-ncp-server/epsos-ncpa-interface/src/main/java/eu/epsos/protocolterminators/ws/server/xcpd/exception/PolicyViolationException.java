package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class PolicyViolationException extends XCPDNIException {
	private static final long serialVersionUID = 620192232688288283L;

	public PolicyViolationException(String message) {
		super(XCPDErrorCode.PolicyViolation, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
