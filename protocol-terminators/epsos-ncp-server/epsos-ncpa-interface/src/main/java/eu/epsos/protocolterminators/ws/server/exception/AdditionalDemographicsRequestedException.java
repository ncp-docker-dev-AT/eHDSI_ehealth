package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiXcpdErrorCode;

public class AdditionalDemographicsRequestedException extends NIException {
	private static final long serialVersionUID = -6309037590489573700L;

	public AdditionalDemographicsRequestedException(String message) {
		super(null, message, EhdsiXcpdErrorCode.AdditionalDemographicsRequested.getCodeSystem());
	}
}
