package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum IheErrorCode implements ErrorCode{

    /*
    * https://profiles.ihe.net/ITI/TF/Volume3/ch-4.2.html
    * Table 4.2.4.1-2: Error Codes (previously Table 4.1-11)
     */
    XDSRepositoryError("The error codes XDSRegistryError or XDSRepositoryError shall be returned if and only if a more detailed code is not available from this table for the condition being reported.", null),
    XDSMissingDocument("DocumentEntry exists in metadata with no corresponding attached document", null);

    private final String codeSystem;
    private final String description;
    IheErrorCode(String description, String codeSystem){
        this.description = description;
        this.codeSystem = codeSystem;
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

    public static IheErrorCode getErrorCode(String code){
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.name().equals(code))
                .findAny()
                .orElse(null);
    }



}
