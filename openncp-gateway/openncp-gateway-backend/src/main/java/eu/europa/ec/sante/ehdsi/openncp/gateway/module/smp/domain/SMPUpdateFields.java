package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain;

/**
 * @author Inês Garganta
 */

public class SMPUpdateFields {

    private String smpFileName;
    private String smpFileTypeId;
    private String smpFileTypeDescription;
    private String smpFileCountryId;
    private String smpFileCountryDescription;
    private SMPFields fields;
    private Boolean isSigned;

    public SMPUpdateFields() {
        super();
    }

    public String getSmpFileName() {
        return smpFileName;
    }

    public void setSmpFileName(String smpFileName) {
        this.smpFileName = smpFileName;
    }

    public String getSmpFileCountryId() {
        return this.smpFileCountryId;
    }

    public void setSmpFileCountryId(String smpFileCountryId) {
        this.smpFileCountryId = smpFileCountryId;
    }

    public String getSmpFileCountryDescription() {
        return this.smpFileCountryDescription;
    }

    public void setSmpFileCountryDescription(String smpFileCountryDescription) {
        this.smpFileCountryDescription = smpFileCountryDescription;
    }

    public String getSmpFileTypeId() {
        return this.smpFileTypeId;
    }

    public void setSmpFileTypeId(String smpFileTypeId) {
        this.smpFileTypeId = smpFileTypeId;
    }

    public String getSmpFileTypeDescription() {
        return this.smpFileTypeDescription;
    }

    public void setSmpFileTypeDescription(String smpFileTypeDescription) {
        this.smpFileTypeDescription = smpFileTypeDescription;
    }

    public SMPFields getFields() {
        return fields;
    }

    public void setFields(SMPFields fields) {
        this.fields = fields;
    }

    public Boolean getSigned() {
        return isSigned;
    }

    public void setSigned(Boolean signed) {
        isSigned = signed;
    }
}
