package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class CollectionIncompleteException extends NIException {

	private static final long serialVersionUID = -8728511084030064015L;

	public CollectionIncompleteException(String message) {
		super(ErrorCode.ERROR_CODE_4102.getCodeToString(), message);
	}
}
