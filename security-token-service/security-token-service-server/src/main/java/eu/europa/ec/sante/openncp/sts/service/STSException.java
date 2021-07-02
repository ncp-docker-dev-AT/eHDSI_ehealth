package eu.europa.ec.sante.openncp.sts.service;

public class STSException extends Exception {

    private String message;
    private String errorCode;

    public STSException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
