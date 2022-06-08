package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiXcpdErrorCode;

public class PatientAuthenticationRequiredException extends NIException {
	private static final long serialVersionUID = -159689601257832448L;

	public PatientAuthenticationRequiredException(String message) {
		super(null, message, EhdsiXcpdErrorCode.PatientAuthenticationRequired.getCodeSystem());
	}
}
