package tr.com.srdc.epsos.ws.server.xca.impl;

public enum XCAError {

    ERROR_1100("1100", "Success", "", ""),
    ERROR_1101("1101", "Success", "", ""),
    ERROR_1102("1102", "Success", "", ""),
    ERROR_1103("1103", "Success", "", ""),
    ERROR_4201("4201", "Failure", "", ""),
    ERROR_4202("4202", "Failure", "", ""),
    ERROR_4203("4203", "Failure", "Transcoding Error", ""),
    ERROR_4204("4204", "Failure", "", ""),
    ERROR_4205("4205", "Failure", "", ""),
    ERROR_4206("4206", "Failure", "", ""),
    ERROR_4701("4701", "Failure", "", ""),
    ERROR_4702("4702", "Failure", "", ""),
    ERROR_4703("4703", "Failure", "", "");

    private String code;
    private String message;
    private String status;
    private String condition;

    XCAError(String code, String message, String status, String condition) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.condition = condition;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getCondition() {
        return condition;
    }
}
