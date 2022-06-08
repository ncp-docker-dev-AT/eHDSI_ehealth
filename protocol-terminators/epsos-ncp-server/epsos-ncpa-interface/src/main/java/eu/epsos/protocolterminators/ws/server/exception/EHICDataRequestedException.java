package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiXcpdErrorCode;

public class EHICDataRequestedException extends NIException {
	private static final long serialVersionUID = -8415378217321251050L;

	public EHICDataRequestedException(String message) {
		super(null, message, EhdsiXcpdErrorCode.EHICDataRequested.getCodeSystem());
	}
}
