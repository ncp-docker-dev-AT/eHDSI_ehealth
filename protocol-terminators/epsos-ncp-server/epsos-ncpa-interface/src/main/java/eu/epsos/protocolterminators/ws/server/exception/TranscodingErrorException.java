package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiErrorCode;

public class TranscodingErrorException extends NIException {

    private static final long serialVersionUID = -8381001130860083595L;

    public TranscodingErrorException(String message) {
        super(EhdsiErrorCode.EHDSI_ERROR_4203, message);
    }
}
