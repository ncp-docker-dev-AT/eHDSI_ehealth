package eu.epsos.util;

import bsh.StringUtil;

import java.util.Arrays;

public enum ErrorCode {

    ERROR_CODE_1001(1001, "Assertion is not valid."),
    ERROR_CODE_1002(1002, "The given TRC Assertion does not validate against the Identity Assertion"),
    ERROR_CODE_1101(1101,  "No ePrescriptions are registered for the given patient."),
    ERROR_CODE_1102(1102,  "No patient summary is registered for the given patient."),
    ERROR_CODE_1103(1103, "No Data for MRO"),
    ERROR_CODE_1104(1104,  "No original clinical document of the requested type is registered for the given patient."),
    ERROR_CODE_1100(1100,  "No documents are registered for the given patient."),
    ERROR_CODE_2201(2201,  "Documents were received but not processed"),
    ERROR_CODE_4101(4101, null),
    ERROR_CODE_4102(4102,  null),
    ERROR_CODE_4103(4103,  "ePrescription registry could not be accessed."),
    ERROR_CODE_4104(4104,  null),
    ERROR_CODE_4105(4105,  "No match"),
    ERROR_CODE_4106(4106,  "Invalid Dispensation"),
    ERROR_CODE_4107(4107,  "Original data missing"),
    ERROR_CODE_4108(4108,  "Pivot data missing"),
    ERROR_CODE_4201(4201, "Unsupported Feature"),
    ERROR_CODE_4202(4202, "Unknown Signifier"),
    ERROR_CODE_4203(4203, "The requested encoding cannot be provided due to a transcoding error."),
    ERROR_CODE_4204(4204, "Unknown Filter"),
    ERROR_CODE_4205(4205, "Unknown Option"),
    ERROR_CODE_4206(4206, "Unknown Patient Identifier"),
    ERROR_CODE_4701(4701, "No consent."),
    ERROR_CODE_4702(4702,  "Weak Authentication"),
    ERROR_CODE_4703(4703, "Insufficient rights"),
    ERROR_CODE_4704(4704,  "No Signature");

    private final int code;
    private final String message;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getCodeToString() {
        return String.valueOf(code);
    }

    public String getMessage() {
        return message;
    }


    public static ErrorCode getErrorCode(int code){
       return Arrays.stream(values())
               .filter(errorCode -> errorCode.getCode() == code)
               .findAny()
               .orElse(null);
    }

}
