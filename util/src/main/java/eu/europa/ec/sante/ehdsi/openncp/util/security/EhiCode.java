package eu.europa.ec.sante.ehdsi.openncp.util.security;

public enum EhiCode {

    XDSRepositoryError(null);

    private final String message;
    EhiCode(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
