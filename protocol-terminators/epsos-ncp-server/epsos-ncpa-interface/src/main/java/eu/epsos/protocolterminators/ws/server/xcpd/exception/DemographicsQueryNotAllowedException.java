package eu.epsos.protocolterminators.ws.server.xcpd.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;

public class DemographicsQueryNotAllowedException extends XcpdNIException {
	private static final long serialVersionUID = -1360702816426111325L;

	public DemographicsQueryNotAllowedException(String message) {
		super(XcpdErrorCode.DemographicsQueryNotAllowed, OpenncpErrorCode.ERROR_PI_GENERIC, message);
	}
}
