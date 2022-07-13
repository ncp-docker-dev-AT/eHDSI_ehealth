package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class DocumentProcessingException extends NIException {

    private static final long serialVersionUID = 2212600691470466466L;

    public DocumentProcessingException() {
        this( "Invalid Dispensation");
    }

    public DocumentProcessingException(String message) {
        super(OpenNCPErrorCode.ERROR_EP_ALREADY_DISPENSED, message);
    }
}
