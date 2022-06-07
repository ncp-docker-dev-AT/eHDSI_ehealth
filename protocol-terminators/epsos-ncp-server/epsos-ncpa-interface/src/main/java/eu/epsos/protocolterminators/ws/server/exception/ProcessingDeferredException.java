package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiErrorCode;

public class ProcessingDeferredException extends NIException {

	private static final long serialVersionUID = 4872216168488255110L;

	public ProcessingDeferredException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_2201, message);
	}
}
