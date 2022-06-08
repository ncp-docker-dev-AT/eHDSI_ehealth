package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class ProcessingDeferredException extends DocumentProcessingException {
	private static final long serialVersionUID = -2416018860753255893L;

	public ProcessingDeferredException() {
		super("Documents were received but not processed");
		super.setEhdsiCode(EhdsiErrorCode.EHDSI_ERROR_2201);
	}
}
