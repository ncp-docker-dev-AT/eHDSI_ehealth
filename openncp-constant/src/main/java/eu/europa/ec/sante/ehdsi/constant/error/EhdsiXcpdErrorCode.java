package eu.europa.ec.sante.ehdsi.constant.error;

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

    public String getCodeSystem() {
         return "1.3.6.1.4.1.12559.11.10.1.3.2.2.1";
    }
}
