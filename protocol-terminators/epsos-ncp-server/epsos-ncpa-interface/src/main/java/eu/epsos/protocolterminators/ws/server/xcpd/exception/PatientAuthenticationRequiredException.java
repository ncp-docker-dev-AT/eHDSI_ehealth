package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class PatientAuthenticationRequiredException extends XCPDNIException {
	private static final long serialVersionUID = -159689601257832448L;

	public PatientAuthenticationRequiredException(String message) {
		super(XCPDErrorCode.PatientAuthenticationRequired, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
