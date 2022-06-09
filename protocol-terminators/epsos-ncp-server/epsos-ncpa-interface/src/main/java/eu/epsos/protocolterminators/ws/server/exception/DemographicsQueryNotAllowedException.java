package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiXcpdErrorCode;

public class DemographicsQueryNotAllowedException extends NIException {
	private static final long serialVersionUID = -1360702816426111325L;

	public DemographicsQueryNotAllowedException(String message) {
		super(null, message, null);
		this.setCodeSystem(EhdsiXcpdErrorCode.DemographicsQueryNotAllowed.getCodeSystem());
	}
}
