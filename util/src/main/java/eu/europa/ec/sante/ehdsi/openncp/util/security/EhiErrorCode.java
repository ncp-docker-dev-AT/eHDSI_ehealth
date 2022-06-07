package eu.europa.ec.sante.ehdsi.openncp.util.security;

public enum EhiErrorCode {

    XDSRepositoryError(null),
    XDSMissingDocument(null);

    private final String message;
    EhiErrorCode(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
