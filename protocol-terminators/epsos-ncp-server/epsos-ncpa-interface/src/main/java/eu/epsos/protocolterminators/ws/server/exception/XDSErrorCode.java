package eu.epsos.protocolterminators.ws.server.exception;

/**
 * XDSErrorCode is representing the Error Codes available for Cross-Enterprise Document Sharing operations, including
 * IHE Error Codes defined in Vol3 - Table 4.1-11 and the ones from the eHDSI XDR Profile.
 */
public enum XDSErrorCode implements ErrorCode {

    //TODO: List of Error Codes shall be completed according the specification.
    NO_MATCHING_PRESCRIPTION("4105", "No Match"),
    INVALID_DISPENSE("4106", "Invalid Dispensation"),
    ORIGINAL_DATA_MISSING("4107", "Original data missing"),
    PIVOT_DATA_MISSING("4108", "Pivot data missing"),
    WEAK_AUTHENTICATION("4702", "Weak Authentication"),
    NO_SIGNATURE("4704", "No Signature");

    private final String code;
    private final String message;

    XDSErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
