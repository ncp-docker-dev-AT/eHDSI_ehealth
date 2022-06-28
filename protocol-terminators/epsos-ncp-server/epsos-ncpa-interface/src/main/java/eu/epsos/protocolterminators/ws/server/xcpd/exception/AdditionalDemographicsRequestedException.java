package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class AdditionalDemographicsRequestedException extends XcpdNIException {
	private static final long serialVersionUID = -6309037590489573700L;

	public AdditionalDemographicsRequestedException(String message) {
		super(XcpdErrorCode.AdditionalDemographicsRequested, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
