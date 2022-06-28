package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class PatientAuthenticationRequiredException extends XcpdNIException {
	private static final long serialVersionUID = -159689601257832448L;

	public PatientAuthenticationRequiredException(String message) {
		super(XcpdErrorCode.PatientAuthenticationRequired, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
