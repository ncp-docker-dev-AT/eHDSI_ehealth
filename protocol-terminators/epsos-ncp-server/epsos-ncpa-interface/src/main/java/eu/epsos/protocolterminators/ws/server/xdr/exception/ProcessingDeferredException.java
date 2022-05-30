package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.epsos.util.ErrorCode;

public class ProcessingDeferredException extends DocumentProcessingException {
	private static final long serialVersionUID = -2416018860753255893L;

	public ProcessingDeferredException() {
		super("Documents were received but not processed");
		super.setCode(ErrorCode.ERROR_CODE_2201.getCodeToString());
	}
}
