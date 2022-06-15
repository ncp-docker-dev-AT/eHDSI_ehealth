package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhiErrorCode implements ErrorCode{

    /*
    * https://profiles.ihe.net/ITI/TF/Volume3/ch-4.2.html
    * Table 4.2.4.1-2: Error Codes (previously Table 4.1-11)
     */
    XDSRepositoryError("The error codes XDSRegistryError or XDSRepositoryError shall be returned if and only if a more detailed code is not available from this table for the condition being reported.", null),
    XDSMissingDocument("DocumentEntry exists in metadata with no corresponding attached document", null),
    //TODO add the other errorcode from the list

    /*
    * https://profiles.ihe.net/ITI/TF/Volume2/ITI-55.html
     * Table 3.55.4.2.2.7-1: Coded values for codeSystem=1.3.6.1.4.1.19376.1.2.27.3
     */
    AnswerNotAvailable("The answer is not available. Human intervention may be needed.", "1.3.6.1.4.1.19376.1.2.27.3"),
    ResponderBusy("The responder was not able to process the request because it is currently overloaded.", "1.3.6.1.4.1.19376.1.2.27.3"),
    InternalError("The responder was not able to respond due to an internal error or inconsistency.", "1.3.6.1.4.1.19376.1.2.27.3");
    private final String codeSystem;
    private final String description;
    EhiErrorCode(String description, String codeSystem){
        this.description = description;
        this.codeSystem = codeSystem;
    }

    public String getMessage() {
        return this.name();
    }

    public String getCode() {
        return this.name();
    }

    public String getDescription() {
        return description;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public static EhiErrorCode getErrorCode(String code){
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.name().equals(code))
                .findAny()
                .orElse(null);
    }



}
