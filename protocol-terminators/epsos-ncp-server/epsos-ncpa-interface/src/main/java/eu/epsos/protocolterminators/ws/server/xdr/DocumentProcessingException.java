package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.util.ErrorCode;

public class DocumentProcessingException extends NIException {

    private static final long serialVersionUID = 2212600691470466466L;

    public DocumentProcessingException() {
        super(ErrorCode.ERROR_CODE_4106.getCodeToString(), "Invalid Dispensation");
    }

    public DocumentProcessingException(String message) {
        super(ErrorCode.ERROR_CODE_4106.getCodeToString(), message);
    }
}
