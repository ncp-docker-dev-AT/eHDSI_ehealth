package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;

public class TranscodingErrorException extends NIException {

    private static final long serialVersionUID = -8381001130860083595L;

    public TranscodingErrorException(String message) {
        super(ErrorCode.ERROR_CODE_4203.getCodeToString(), message);
    }
}
