package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class AdditionalDemographicsRequestedException extends XCPDNIException {
	private static final long serialVersionUID = -6309037590489573700L;

	public AdditionalDemographicsRequestedException(String message) {
		super(XCPDErrorCode.AdditionalDemographicsRequested, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
