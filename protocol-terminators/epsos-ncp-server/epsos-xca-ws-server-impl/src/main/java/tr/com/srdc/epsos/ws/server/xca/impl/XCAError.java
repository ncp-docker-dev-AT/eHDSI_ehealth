package tr.com.srdc.epsos.ws.server.xca.impl;

public enum XCAError {

    ERROR_1100("1100", false, "No Data", ""),
    ERROR_1101("1101", false, "No Data for eP", ""),
    ERROR_1102("1102", false, "No Data for PS", ""),
    ERROR_1103("1103", true, "No Data for MRO", ""),
    ERROR_4201("4201", true, "Unsupported Feature", ""),
    ERROR_4202("4202", true, "Unknown Signifier", ""),
    ERROR_4203("4203", true, "The requested encoding cannot be provided due to a transcoding error.", ""),
    ERROR_4204("4204", true, "Unknown Filter", ""),
    ERROR_4205("4205", true, "Unknown Option", ""),
    ERROR_4206("4206", true, "Unknown Patient Identifier", ""),
    ERROR_4701("4701", true, "No Consent", ""),
    ERROR_4702("4702", true, "Weak Authentication", ""),
    ERROR_4703("4703", true, "Insufficient Rights", "");

    private String code;
    private boolean failed;
    private String message;
    private String condition;

    XCAError(String code, boolean failed, String message, String condition) {
        this.code = code;
        this.failed = failed;
        this.message = message;
        this.condition = condition;
    }

    public String getCode() {
        return code;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getMessage() {
        return message;
    }

    public String getCondition() {
        return condition;
    }
}
