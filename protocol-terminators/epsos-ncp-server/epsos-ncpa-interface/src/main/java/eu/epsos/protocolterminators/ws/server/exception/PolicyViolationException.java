package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiXcpdErrorCode;

public class PolicyViolationException extends NIException {
	private static final long serialVersionUID = 620192232688288283L;

	public PolicyViolationException(String message) {
		super(null, message, EhdsiXcpdErrorCode.PolicyViolation.getCodeSystem());
	}
}
