package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class TranscodingErrorException extends NIException {

    private static final long serialVersionUID = -8381001130860083595L;

    public TranscodingErrorException(String message) {
        super(EhdsiErrorCode.EHDSI_ERROR_TRANSCODING_ERROR, message);
    }
}
