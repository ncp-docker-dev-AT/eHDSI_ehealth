package se.sb.epsos.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class NcpServiceException extends Exception implements Serializable {

    private static final long serialVersionUID = -3820997307354041479L;
    private ArrayList<String> knowErrorCodes = new ArrayList<>(
            Arrays.asList("1002", "4202", "4203", "4204", "4103", "4104", "XDSRegistryError", "XDSRepositoryError",
                    "PrivacyViolation", "InsufficientRights", "PatientAuthenticationRequired", "AnswerNotAvailable",
                    "PolicyViolation", "4701", "4702", "4703", "1031", "1101", "FINNCP-0001", "FINNCP-0002",
                    "FINNCP-0003", "FINNCP-0004", "FINNCP-0005", "FINNCP-0006", "FINNCP-0007", "FINNCP-0008",
                    "FINNCP-0009", "FINNCP-0010", "FINNCP-0011", "FINNCP-0012", "FINNCP-0013", "4106"));
    private boolean causedByLoginRequired;
    private boolean knownEpsosError;
    private String epsosErrorCode;
    private String epsosErrorMessage;

    public NcpServiceException(String s, Throwable e) {
        super(s, e);
        for (String knownCode : knowErrorCodes) {
            if (e != null && e.getMessage() != null && e.getMessage().contains(knownCode)) {
                setEpsosError(knownCode);
                break;
            }
        }
    }

    private void setEpsosError(String errorCode) {
        this.epsosErrorCode = errorCode;
        this.knownEpsosError = true;
    }

    public boolean isCausedByLoginRequired() {
        return causedByLoginRequired;
    }

    public boolean isKnownEpsosError() {
        return knownEpsosError;
    }

    public String getEpsosErrorCode() {
        return epsosErrorCode;
    }

    public String getEpsosErrorMessage() {
        return epsosErrorMessage;
    }

}
