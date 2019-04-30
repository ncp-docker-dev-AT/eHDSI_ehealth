package tr.com.srdc.epsos.securityman.exceptions;

import java.util.HashMap;
import java.util.Map;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;
    private static final Map<Integer, String> errorMessages = new HashMap<>();

    static {
        errorMessages.put(1002, "The given TRC Assertion does not validate against the Identity Assertion");
        errorMessages.put(4701, "No consent.");
        errorMessages.put(4703, "Either the security policy of country A or a privacy policy of the patient (that was given in country A) does not allow the requested operation to be performed by the HCP.");
    }

    private String message;
    private String errorCode;

    public InsufficientRightsException() {
        errorCode = "4703";
        message = errorMessages.get(Integer.parseInt(errorCode));
    }

    public InsufficientRightsException(int errorCode) {
        message = errorMessages.get(errorCode);
        this.errorCode = Integer.toString(errorCode);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return errorCode;
    }
}
