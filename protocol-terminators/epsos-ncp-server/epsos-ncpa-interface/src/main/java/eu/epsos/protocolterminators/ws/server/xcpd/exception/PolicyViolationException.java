package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class PolicyViolationException extends XcpdNIException {
	private static final long serialVersionUID = 620192232688288283L;

	public PolicyViolationException(String message) {
		super(XcpdErrorCode.PolicyViolation, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
