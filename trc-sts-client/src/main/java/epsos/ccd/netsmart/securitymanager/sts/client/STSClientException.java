package epsos.ccd.netsmart.securitymanager.sts.client;

public class STSClientException extends Exception {

    public STSClientException(String message) {
        super(message);
    }

    public STSClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
