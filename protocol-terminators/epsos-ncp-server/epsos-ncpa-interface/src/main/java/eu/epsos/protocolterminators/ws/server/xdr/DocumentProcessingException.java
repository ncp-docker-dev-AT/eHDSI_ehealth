package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;

public class DocumentProcessingException extends NIException {

    private static final long serialVersionUID = 2212600691470466466L;

    public DocumentProcessingException() {
        super("4106", "Invalid Dispensation");
    }

    public DocumentProcessingException(String message) {
        super("4106", message);
    }
}
