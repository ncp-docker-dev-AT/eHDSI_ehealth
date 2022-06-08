package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class NIException extends Exception {

    private static final long serialVersionUID = 2148051521948531853L;
    private EhdsiErrorCode ehdsiErrorCode;
    private String codeSystem;
    private String message;

    public NIException(EhdsiErrorCode ehdsiErrorCode, String message) {
        this.ehdsiErrorCode = ehdsiErrorCode;
        this.message = message;
    }

    public NIException(EhdsiErrorCode ehdsiErrorCode, String message, String codeSystem) {
        this(ehdsiErrorCode, message);
        this.codeSystem = codeSystem;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
    }

    public void setEhdsiCode(EhdsiErrorCode ehdsiErrorCode) {
        this.ehdsiErrorCode = ehdsiErrorCode;
    }
}
