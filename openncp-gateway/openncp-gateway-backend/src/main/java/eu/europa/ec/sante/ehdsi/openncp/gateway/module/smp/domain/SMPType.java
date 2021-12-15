package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain;

/**
 * @author Inês Garganta
 */

public enum SMPType {

    //SPECIFICATION
    Country_B_Identity_Provider("Country B Identity Provider"),
    Patient_Identification_Authentication("Patient Identification and Authentication"),
    Provisioning_of_Data_Provide("Provisioning of Data - Provide"),
    Provisioning_of_Data_BPPC("Provisioning of Data - BPPC"),
    Request_of_Data_Fetch("Request of Data - Fetch"),
    Request_of_Data_Query("Request of Data - Query"),
    Request_of_Data_Retrieve("Request of Data - Retrieve"),
    International_Search_Mask("International Search Mask"),
    Redirect("Redirect");

    protected static final SMPType[] ALL = {Country_B_Identity_Provider,
            Patient_Identification_Authentication, Provisioning_of_Data_Provide, Provisioning_of_Data_BPPC,
            Request_of_Data_Fetch, Request_of_Data_Query, Request_of_Data_Retrieve, International_Search_Mask, Redirect};

    private final String description;

    SMPType(final String description) {
        this.description = description;
    }

    public static SMPType[] getAll() {
        return ALL;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
