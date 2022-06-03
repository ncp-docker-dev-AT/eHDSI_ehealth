package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class NIException extends Exception {

    private static final long serialVersionUID = 2148051521948531853L;
    private EhdsiCode ehdsiCode;
    private String codeSystem;
    private String message;

    public NIException(EhdsiCode ehdsiCode, String message) {
        this.ehdsiCode = ehdsiCode;
        this.message = message;
    }

    public NIException(EhdsiCode ehdsiCode, String message, String codeSystem) {
        this(ehdsiCode, message);
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


    public EhdsiCode getEhdsiCode() {
        return ehdsiCode;
    }

    public void setEhdsiCode(EhdsiCode ehdsiCode) {
        this.ehdsiCode = ehdsiCode;
    }
}
