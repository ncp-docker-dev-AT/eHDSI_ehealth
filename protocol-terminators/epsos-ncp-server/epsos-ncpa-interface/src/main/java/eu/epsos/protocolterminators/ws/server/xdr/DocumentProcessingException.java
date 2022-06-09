package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class DocumentProcessingException extends NIException {

    private static final long serialVersionUID = 2212600691470466466L;

    public DocumentProcessingException() {
        this( "Invalid Dispensation");
    }

    public DocumentProcessingException(String message) {
        super(EhdsiErrorCode.EHDSI_ERROR_4106, message);
    }
}
