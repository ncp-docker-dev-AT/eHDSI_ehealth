package eu.europa.ec.sante.ehdsi.tsam.sync.client;


public class TermServerClientException extends RuntimeException {

    private int statusCode;

    private String statusText;

    private String body;

    public TermServerClientException(int statusCode, String statusText, String body) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.body = body;
    }

    public int statusCode() {
        return statusCode;
    }

    public String statusText() {
        return statusText;
    }

    public String body() {
        return body;
    }

    @Override
    public String getMessage() {
        return "TermServer Client exception occurred (" + statusCode + " " + statusText + ")";
    }
}
