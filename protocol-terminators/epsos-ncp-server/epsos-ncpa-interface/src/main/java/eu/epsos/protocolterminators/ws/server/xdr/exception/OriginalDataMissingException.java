package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class OriginalDataMissingException extends DocumentProcessingException {
	private static final long serialVersionUID = -1880772107399517210L;

	public OriginalDataMissingException() {
		super("For data of the given kind the Provide Data service provider requires the service consumer to transmit the source coded PDF document.");
		super.setEhdsiCode(EhdsiErrorCode.EHDSI_ERROR_4107);
	}
}
