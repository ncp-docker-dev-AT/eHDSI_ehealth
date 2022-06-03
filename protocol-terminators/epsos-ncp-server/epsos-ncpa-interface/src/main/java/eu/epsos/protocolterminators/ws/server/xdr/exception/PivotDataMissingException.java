package eu.epsos.protocolterminators.ws.server.xdr.exception;

import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class PivotDataMissingException extends DocumentProcessingException {
	private static final long serialVersionUID = 8197669683897748059L;

	public PivotDataMissingException() {
		super("The service consumer did not provide the epSOS pivot coded document which is requested by the Provide Data service provider for the given kind of data.");
		super.setEhdsiCode(EhdsiCode.EHDSI_ERROR_4108);
	}
}
