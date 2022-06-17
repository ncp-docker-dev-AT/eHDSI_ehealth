package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class PatientAuthenticationRequiredException extends NIException {
	private static final long serialVersionUID = -159689601257832448L;

	public PatientAuthenticationRequiredException(String message) {
		super(null, message, XcpdErrorCode.PatientAuthenticationRequired.getCodeSystem());
	}
}
