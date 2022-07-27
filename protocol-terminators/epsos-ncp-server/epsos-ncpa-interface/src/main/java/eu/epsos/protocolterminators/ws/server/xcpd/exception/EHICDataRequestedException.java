package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XCPDErrorCode;

public class EHICDataRequestedException extends XCPDNIException {
	private static final long serialVersionUID = -8415378217321251050L;

	public EHICDataRequestedException(String message) {
		super(XCPDErrorCode.EHICDataRequested, OpenNCPErrorCode.ERROR_PI_GENERIC, message);
	}
}
