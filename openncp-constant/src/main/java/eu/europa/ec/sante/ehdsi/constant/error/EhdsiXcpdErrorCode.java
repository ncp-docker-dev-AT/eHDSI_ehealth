package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhdsiXcpdErrorCode implements ErrorCode{

    /*
     * XCPD Profile Technical Specification
     * https://webgate.ec.europa.eu/fpfis/wikis/x/3eTzN
     */
    AdditionalDemographicsRequested,
    DemographicsQueryNotAllowed,
    EHICDataRequested,
    PrivacyViolation,
    InsufficientRights,
    PatientAuthenticationRequired,
    PolicyViolation;

    EhdsiXcpdErrorCode(){
    }

    @Override
    public String getCode() {
        return this.name();
    }

    public String getCodeSystem() {
         return "1.3.6.1.4.1.12559.11.10.1.3.2.2.1";
    }

    public static EhdsiXcpdErrorCode getErrorCode(String code){
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.name().equals(code))
                .findAny()
                .orElse(null);
    }

    public String getDescription() {
        //TODO add description field
        return null;
    }

}
