package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class NoSignatureException extends NIException {
	private static final long serialVersionUID = -2813019150881427805L;

	public NoSignatureException(String message) {
		super(EhdsiCode.EHDSI_ERROR_4704, message);
	}
}
