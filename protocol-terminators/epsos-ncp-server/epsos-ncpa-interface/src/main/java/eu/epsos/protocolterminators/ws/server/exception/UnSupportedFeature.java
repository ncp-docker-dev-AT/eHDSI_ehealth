package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.error.EhdsiErrorCode;

public class UnSupportedFeature extends NIException {

	private static final long serialVersionUID = -7044629789540910172L;

	public UnSupportedFeature(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4201, message);
	}

}
