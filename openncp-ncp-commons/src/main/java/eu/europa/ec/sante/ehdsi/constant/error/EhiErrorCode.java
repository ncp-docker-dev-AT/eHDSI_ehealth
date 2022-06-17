package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhiErrorCode implements ErrorCode{

    /*
    * https://profiles.ihe.net/ITI/TF/Volume3/ch-4.2.html
    * Table 4.2.4.1-2: Error Codes (previously Table 4.1-11)
     */
    XDSRepositoryError("The error codes XDSRegistryError or XDSRepositoryError shall be returned if and only if a more detailed code is not available from this table for the condition being reported.", null),
    XDSMissingDocument("DocumentEntry exists in metadata with no corresponding attached document", null);
    //TODO add the other errorcode from the list



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
