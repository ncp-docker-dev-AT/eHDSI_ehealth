package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class DemographicsQueryNotAllowedException extends XCPDNIException {
	private static final long serialVersionUID = -1360702816426111325L;

	public DemographicsQueryNotAllowedException(String message) {
		super(XCPDErrorCode.DemographicsQueryNotAllowed, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
