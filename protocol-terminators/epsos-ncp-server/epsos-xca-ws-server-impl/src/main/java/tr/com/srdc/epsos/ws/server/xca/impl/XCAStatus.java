package tr.com.srdc.epsos.ws.server.xca.impl;

public enum XCAStatus {

    SUCCESS("Success"), FAILURE("Failure");

    private String status;

    XCAStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}