package eu.europa.ec.sante.ehdsi.constant.error;

import java.util.Arrays;

public enum EhdsiErrorCode implements ErrorCode{

    EHDSI_ERROR_1001("1001", "Assertion is not valid."),
    EHDSI_ERROR_1002("1002", "The given TRC Assertion does not validate against the Identity Assertion"),
    EHDSI_ERROR_1101("1101",  "No ePrescriptions are registered for the given patient."),
    EHDSI_ERROR_1102("1102",  "No patient summary is registered for the given patient."),
    EHDSI_ERROR_1103("1103", "No Data for MRO"),
    EHDSI_ERROR_1104("1104",  "No original clinical document of the requested type is registered for the given patient."),
    EHDSI_ERROR_1100("1100",  "No documents are registered for the given patient."),
    EHDSI_ERROR_2201("2201",  "Documents were received but not processed"),
    EHDSI_ERROR_4101("4101", null),
    EHDSI_ERROR_4102("4102",  null),
    EHDSI_ERROR_4103("4103",  "ePrescription registry could not be accessed."),
    EHDSI_ERROR_4104("4104",  null),
    EHDSI_ERROR_4105("4105",  "No match"),
    EHDSI_ERROR_4106("4106",  "Invalid Dispensation"),
    EHDSI_ERROR_4107("4107",  "Original data missing"),
    EHDSI_ERROR_4108("4108",  "Pivot data missing"),
    EHDSI_ERROR_4201("4201", "Unsupported Feature"),
    EHDSI_ERROR_4202("4202", "Unknown Signifier"),
    EHDSI_ERROR_4203("4203", "The requested encoding cannot be provided due to a transcoding error."),
    EHDSI_ERROR_4204("4204", "Unknown Filter"),
    EHDSI_ERROR_4205("4205", "Unknown Option"),
    EHDSI_ERROR_4206("4206", "Unknown Patient Identifier"),
    EHDSI_ERROR_4701("4701", "No consent."),
    EHDSI_ERROR_4702("4702",  "Weak Authentication"),
    EHDSI_ERROR_4703("4703", "Insufficient rights"),
    EHDSI_ERROR_4704("4704",  "No Signature"),

    EHDSI_ERROR_GENERIC("4999", "Generic business error code"),
    EHDSI_ERROR_CONNECTION_NOT_POSSIBLE ("5036", "The Country of Treatment (Country B) is unable to contact the Patient Country of Affiliation."),

    // 01. Ensure Health Professional (HP) Identification, Authentication and Authorization
    EHDSI_ERROR_HPI_GENERIC ("5037", "The code can be used for HPI errors when no other HPI business code applies."),
    EHDSI_WARNING_HPI_GENERIC("5038", "The code can be used for HPI warnings when no other HPI business code applies."),

    EHDSI_ERROR_HPI_NO_INFORMATION("5039", "No information has been provided about the HP."),
    EHDSI_ERROR_HPI_INSUFFICIENT_INFORMATION("5040", "Information provided about the HP is insufficient"),
    EHDSI_ERROR_HPI_HAI_NO_INFORMATION("5041", "No information has been provided about the Health Authorities Institutions and/or Healthcare Provider Organisation (HCPO) ."),
    EHDSI_ERROR_HPI_POC_NO_INFORMATION("5042", "No information has been provided about the organisation or healthcare facility where the HP is providing the treatment - Point of Care (PoC)"),
    EHDSI_ERROR_HPI_AUTHENTICATION_NOT_RECEIVED("5043", "HP authentication method has not been received by the Country of Affiliation"),

    // 02. Ensure Patient Identification

    EHDSI_ERROR_PI_GENERIC("5044", "The code can be used for PI errors when no other PI business code applies."),
    EHDSI_WARNING_PI_GENERIC("5045", "The code can be used for PI warnings when no other PI business code applies."),

    EHDSI_ERROR_PI_NO_MATCH("5046", "The identification and authentication used in the Country of Treatment (Country B) are not correctly provided or does not match any existing patient."),
    EHDSI_ERROR_PI_MULTIPLE_MATCHES("5047", "The identification and authentication used in the Country of Treatment (Country B) match multiple patients, instead of only one patient."),

    // 05. Make Patient Summary available to HP

    EHDSI_ERROR_PS_GENERIC("5048", "The code can be used for PS errors when no other PS business code applies."),
    EHDSI_WARNING_PS_GENERIC("5049", "The code can be used for PS warnings when no other PS business code applies."),

    EHDSI_ERROR_PS_MISSING_BASIC_SECTIONS("5050", "Any or all of the 5 basic sections are missing, and no reason is communicated for this missing."),
    EHDSI_WARNING_PS_MISSING_BASIC_DATA("5051", "Any or all of the basic data set is missing."),
    EHDSI_WARNING_PS_MISSING_EXPECTED_MAPPING("5052", "A needed code mapping is missing."),
    EHDSI_ERROR_PS_PDF_FORMAT_NOT_PROVIDED("5053", "A PDF has not been provided."),

    // 06. Make ePrescription available to HP

    EHDSI_ERROR_EP_GENERIC("5000", "The code can be used for EP errors when no other EP business code applies."),
    EHDSI_WARNING_EP_GENERIC("5001", "The code can be used for EP warnings when no other EP business code applies."),

    EHDSI_WARNING_EP_MISSING_BASIC_DATA("5002", "Any or all of the basic data set is missing, and no reason is communicated for this missing."),
    EHDSI_WARNING_EP_MISSING_EXPECTED_MAPPING("5003", "A needed code mapping is missing."),
    EHDSI_ERROR_EP_PDF_FORMAT_NOT_PROVIDED("5004", "A PDF has not been provided."),

    // 07. Handle Dispensation of medicine and Substitution

    EHDSI_ERROR_ED_GENERIC("5005", "The code can be used for ED errors when no other ED business code applies."),
    EHDSI_WARNING_ED_GENERIC("5006", "The code can be used for ED warnings when no other ED business code applies."),

    EHDSI_ERROR_ED_NOT_DISPENSABLE_EPRESCRIPTION("5007", "The selected ePrescription is not dispensable"),
    EHDSI_ERROR_ED_SUBSTITUTION_INFORMATION_NOT_ALLOWED("5008", "Country of Affiliation has received an information of substitution when it was not allowed based on its national legislation."),
    EHDSI_ERROR_ED_EPRESCRIPTION_NOT_IDENTIFIABLE("5009", "Some of the necessary information to allow the identification of the ePrescription and the related medicinal product are missing."),
    EHDSI_WARNING_ED_MISSING_BASIC_DATA("5010", "Any or all of the basic data set is missing, and no reason has been communicated for this missing."),
    EHDSI_ERROR_ED_INFORMATION_NOT_STORE("5011", "Although the Country of Treatment authorizes the eDispensation discard action, eDispensation information could not be stored by the Country of Treatment."),
    EHDSI_WARNING_ED_MISSING_EXPECTED_MAPPING("5012", "An expected code is missing."),
    EHDSI_ERROR_ED_EDISPENSATION_ACK_NOT_RECEIVED("5013", "A new request for available ePrescriptions has been done while the dispensation acknowledgement from Country A has not been received."),
    EHDSI_ERROR_ED_EPRESCRIPTION_NOT_UPDATED("5014", "Some of the ePrescription(s) could not be updated with the dispensed medicine information."),
    EHDSI_ERROR_ED_EDISPENSATION_NOT_STORED("5015", "Although the Country of Affiliation authorizes the eDispensation discard action, eDispensation information could not be stored by the Country of Affiliation."),
    EHDSI_ERROR_ED_DISCARD_SERVICE_NOT_AVAILABLE("5016", "The Country of Treatment cannot contact the eDispensation discard service of the Country of Affiliation"),
    EHDSI_ERROR_ED_DISCARD_FAILED("5017", "The dispensation discard could not be performed in the Country of Affiliation"),
    EHDSI_WARNING_ED_DISCARD_MISSING_MANDATORY_DATA("5018", "Any or all of the mandatory elements are missing."),

    // 08. Make Original Clinical Documents available to HP

    EHDSI_ERROR_ORCD_GENERIC("5019", "The code can be used for ORCD errors when no other ORCD business code applies."),
    EHDSI_WARNING_ORCD_GENERIC("5020", "The code can be used for ORCD warnings when no other ORCD business code applies."),

    EHDSI_ERROR_ORCD_SERVICE_NOT_AVAILABLE("5021", "The Country of Treatment cannot contact the ORCD service of the Country of Affiliation"),
    EHDSI_WARNING_ORCD_MISSING_MANDATORY_DATA("5022", "Any or all of the mandatory elements are missing."),
    EHDSI_ERROR_ORCD_INCORRECT_FORMAT("5023", "The format of the provided file is not one of the authorized ones"),
    EHDSI_WARNING_ORCD_MISSING_MANDATORY_METADATA("5024", "Any or all of the mandatory metadata are missing."),

    // 09. Ensure high quality information (structured, equivalent, understandable) is exchanged between countries

    EHDSI_ERROR_HQI_GENERIC("5025", "The code can be used for HQI errors when no other HQI business code applies."),
    EHDSI_WARNING_HQI_GENERIC("5026", "The code can be used for HQI warnings when no other HQI business code applies."),

    EHDSI_WARNING_HQI_B_INFORMATION_NOT_PROPERLY_STRUCTURED("5027", "Information provided by the Country of Treatment is not properly structured."),
    EHDSI_WARNING_HQI_A_INFORMATION_NOT_PROPERLY_STRUCTURED("5028", "Information provided by the Country of Affiliation is not properly structured."),

    // 10. Ensure the security, performance, traceability and auditability of the services, data and systems

    EHDSI_ERROR_SEC_GENERIC("5029", "The code can be used for SEC errors when no other SEC business code applies."),
    EHDSI_WARNING_SEC_GENERIC("5030", "The code can be used for SEC warnings when no other SEC business code applies."),

    EHDSI_ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED("5031", "Integrity of the exchanged data cannot be ensured by the Country of Affiliation, such as with an insufficient/invalidated SAML assertions,"),

    EHDSI_WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_PATIENT("5032", "An unexpected number of requests has been detected for one specific patient"),
    EHDSI_WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_POINT_OF_CARE("5034", "An unexpected number of requests has been detected from one specific Point of Care"),
    EHDSI_WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS("5035", "An unexpected number of requests has been detected");



    private final String code;
    private final String description;
    EhdsiErrorCode(String code, String description){
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    public static EhdsiErrorCode getErrorCode(String code){
       return Arrays.stream(values())
               .filter(errorCode -> errorCode.getCode().equals(code))
               .findAny()
               .orElse(null);
    }

    @Override
    public String getCodeSystem() {
        return null;
    }
}
