package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class CollectionIncompleteException extends NIException {

	private static final long serialVersionUID = -8728511084030064015L;

	public CollectionIncompleteException(String message) {
		super(EhdsiErrorCode.EHDSI_ERROR_4102, message);
	}
}
