package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class EHICDataRequestedException extends XcpdNIException {
	private static final long serialVersionUID = -8415378217321251050L;

	public EHICDataRequestedException(String message) {
		super(XcpdErrorCode.EHICDataRequested, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
