package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhdsiXcpdErrorCode implements ErrorCode{

    /*
     * XCPD Profile Technical Specification
     * https://webgate.ec.europa.eu/fpfis/wikis/x/3eTzN
     */
    AdditionalDemographicsRequested("The service requestor tried an identification based on an ID only or did not provide enough data to univocally identify the patient."),
    DemographicsQueryNotAllowed("The service  provider  only allows  for patient identification by national/shared ID."),
    EHICDataRequested("The service provider only allows for patient identification by national health card or EHIC. Queries based on demographics only are not supported."),
    PrivacyViolation("The service provider does not accept he query because responding MAY lead to a disclosure of private patient data."),
    InsufficientRights("The requestor has insufficient rights to query for patient’s identity data."),
    PatientAuthenticationRequired("A respective identifier (e.g. GSS TAN)was not provided."),
    PolicyViolation("The service consumer defined a confidence level that conflicts with thesecurity policy of the service provider.");

    private final String description;

    EhdsiXcpdErrorCode(String description){
        this.description = description;
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
         return "1.3.6.1.4.1.12559.11.10.1.3.2.2.1";
    }

    public static EhdsiXcpdErrorCode getErrorCode(String code){
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.name().equals(code))
                .findAny()
                .orElse(null);
    }

}
