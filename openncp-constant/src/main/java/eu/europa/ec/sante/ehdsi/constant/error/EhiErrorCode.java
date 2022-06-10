package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhiErrorCode implements ErrorCode{

    /*
    * https://profiles.ihe.net/ITI/TF/Volume3/ch-4.2.html
    * Table 4.2.4.1-2: Error Codes (previously Table 4.1-11)
     */
    XDSRepositoryError(null, null),
    XDSMissingDocument(null, null),
    //TODO add the other errorcode from the list

    /*
    * https://profiles.ihe.net/ITI/TF/Volume2/ITI-55.html
     * Table 3.55.4.2.2.7-1: Coded values for codeSystem=1.3.6.1.4.1.19376.1.2.27.3
     */
    AnswerNotAvailable(null, "1.3.6.1.4.1.19376.1.2.27.3"),
    ResponderBusy(null, "1.3.6.1.4.1.19376.1.2.27.3"),
    InternalError(null, "1.3.6.1.4.1.19376.1.2.27.3");

    private final String description;
    private final String codeSystem;
    EhiErrorCode(String description, String codeSystem){
        this.description = description;
        this.codeSystem = codeSystem;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getCode() {
        return this.name();
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
