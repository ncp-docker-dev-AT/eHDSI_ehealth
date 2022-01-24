package tr.com.srdc.epsos.data.model;

import java.util.ArrayList;

public class AssertionInformation {

    private final ArrayList<String> XSPA_Permissions;
    private String XSPA_Subject;
    private String XSPA_Role;
    private String HITSP_Clinical_Speciality;
    private String OnBehalfOf;
    private String XSPA_Organization;
    private String XSPA_Organization_Id;
    private String Healthcare_Facility_Type;
    private String XSPA_Purpose_Of_Use;
    private String XSPA_Locality;

    public AssertionInformation() {
        XSPA_Permissions = new ArrayList<>();
    }

    /**
     * @return the xSPA_Subject
     */
    public String getXSPA_Subject() {
        return XSPA_Subject;
    }

    /**
     * @param xSPASubject the xSPA_Subject to set
     */
    public void setXSPA_Subject(String xSPASubject) {
        XSPA_Subject = xSPASubject;
    }

    /**
     * @return the xSPA_Role
     */
    public String getXSPA_Role() {
        return XSPA_Role;
    }

    /**
     * @param xSPARole the xSPA_Role to set
     */
    public void setXSPA_Role(String xSPARole) {
        XSPA_Role = xSPARole;
    }

    /**
     * @return the hITSP_Clinical_Speciality
     */
    public String getHITSP_Clinical_Speciality() {
        return HITSP_Clinical_Speciality;
    }

    /**
     * @param hITSPClinicalSpeciality the hITSP_Clinical_Speciality to set
     */
    public void setHITSP_Clinical_Speciality(String hITSPClinicalSpeciality) {
        HITSP_Clinical_Speciality = hITSPClinicalSpeciality;
    }

    /**
     * @return the xSPA_Permissions
     */
    public ArrayList<String> getXSPA_Permissions() {
        return XSPA_Permissions;
    }

    /**
     * @param xSPAPermissions the xSPA_Permissions to set
     */
    public void addXSPA_Permissions(String xSPAPermissions) {
        XSPA_Permissions.add(xSPAPermissions);
    }

    /**
     * @return the onBehalfOf
     */
    public String getOnBehalfOf() {
        return OnBehalfOf;
    }

    /**
     * @param onBehalfOf the onBehalfOf to set
     */
    public void setOnBehalfOf(String onBehalfOf) {
        OnBehalfOf = onBehalfOf;
    }

    /**
     * @return the xSPA_Organization
     */
    public String getXSPA_Organization() {
        return XSPA_Organization;
    }

    /**
     * @param xSPAOrganization the xSPA_Organization to set
     */
    public void setXSPA_Organization(String xSPAOrganization) {
        XSPA_Organization = xSPAOrganization;
    }

    /**
     * @return the xSPA_Organization_Id
     */
    public String getXSPA_Organization_Id() {
        return XSPA_Organization_Id;
    }

    /**
     * @param xSPAOrganizationId the xSPA_Organization_Id to set
     */
    public void setXSPA_Organization_Id(String xSPAOrganizationId) {
        XSPA_Organization_Id = xSPAOrganizationId;
    }

    /**
     * @return the healthcare_Facility_Type
     */
    public String getHealthcare_Facility_Type() {
        return Healthcare_Facility_Type;
    }

    /**
     * @param healthcareFacilityType the healthcare_Facility_Type to set
     */
    public void setHealthcare_Facility_Type(String healthcareFacilityType) {
        Healthcare_Facility_Type = healthcareFacilityType;
    }

    /**
     * @return the xSPA_Purpose_Of_Use
     */
    public String getXSPA_Purpose_Of_Use() {
        return XSPA_Purpose_Of_Use;
    }

    /**
     * @param xSPAPurposeOfUse the xSPA_Purpose_Of_Use to set
     */
    public void setXSPA_Purpose_Of_Use(String xSPAPurposeOfUse) {
        XSPA_Purpose_Of_Use = xSPAPurposeOfUse;
    }

    /**
     * @return the xSPA_Locality
     */
    public String getXSPA_Locality() {
        return XSPA_Locality;
    }

    /**
     * @param xSPALocality the xSPA_Locality to set
     */
    public void setXSPA_Locality(String xSPALocality) {
        XSPA_Locality = xSPALocality;
    }
}
