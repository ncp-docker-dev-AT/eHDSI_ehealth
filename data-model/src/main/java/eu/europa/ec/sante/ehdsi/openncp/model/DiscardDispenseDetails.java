package eu.europa.ec.sante.ehdsi.openncp.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

public class DiscardDispenseDetails {

    private String discardId;
    private String dispenseId;
    private Date discardDate;
    private String patientId;
    private String healthCareProviderId;
    private String healthCareProvider;
    private String healthCareProviderFacility;
    private String healthCareProviderOrganizationId;
    private String healthCareProviderOrganization;

    public DiscardDispenseDetails() {
        // Not implemented.
    }

    public String getDiscardId() {
        return discardId;
    }

    public void setDiscardId(String discardId) {
        this.discardId = discardId;
    }

    public String getDispenseId() {
        return dispenseId;
    }

    public void setDispenseId(String dispenseId) {
        this.dispenseId = dispenseId;
    }

    public Date getDiscardDate() {
        return discardDate;
    }

    public void setDiscardDate(Date discardDate) {
        this.discardDate = discardDate;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getHealthCareProviderId() {
        return healthCareProviderId;
    }

    public void setHealthCareProviderId(String healthCareProviderId) {
        this.healthCareProviderId = healthCareProviderId;
    }

    public String getHealthCareProvider() {
        return healthCareProvider;
    }

    public void setHealthCareProvider(String healthCareProvider) {
        this.healthCareProvider = healthCareProvider;
    }

    public String getHealthCareProviderFacility() {
        return healthCareProviderFacility;
    }

    public void setHealthCareProviderFacility(String healthCareProviderFacility) {
        this.healthCareProviderFacility = healthCareProviderFacility;
    }

    public String getHealthCareProviderOrganizationId() {
        return healthCareProviderOrganizationId;
    }

    public void setHealthCareProviderOrganizationId(String healthCareProviderOrganizationId) {
        this.healthCareProviderOrganizationId = healthCareProviderOrganizationId;
    }

    public String getHealthCareProviderOrganization() {
        return healthCareProviderOrganization;
    }

    public void setHealthCareProviderOrganization(String healthCareProviderOrganization) {
        this.healthCareProviderOrganization = healthCareProviderOrganization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DiscardDispenseDetails that = (DiscardDispenseDetails) o;

        return new EqualsBuilder()
                .append(discardId, that.discardId)
                .append(dispenseId, that.dispenseId)
                .append(discardDate, that.discardDate)
                .append(patientId, that.patientId)
                .append(healthCareProviderId, that.healthCareProviderId)
                .append(healthCareProvider, that.healthCareProvider)
                .append(healthCareProviderFacility, that.healthCareProviderFacility)
                .append(healthCareProviderOrganizationId, that.healthCareProviderOrganizationId)
                .append(healthCareProviderOrganization, that.healthCareProviderOrganization)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(discardId)
                .append(dispenseId)
                .append(discardDate)
                .append(patientId)
                .append(healthCareProviderId)
                .append(healthCareProvider)
                .append(healthCareProviderFacility)
                .append(healthCareProviderOrganizationId)
                .append(healthCareProviderOrganization)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("discardId", discardId)
                .append("dispenseId", dispenseId)
                .append("discardDate", discardDate)
                .append("patientId", patientId)
                .append("healthCareProviderId", healthCareProviderId)
                .append("healthCareProvider", healthCareProvider)
                .append("healthCareProviderFacility", healthCareProviderFacility)
                .append("healthCareProviderOrganizationId", healthCareProviderOrganizationId)
                .append("healthCareProviderOrganization", healthCareProviderOrganization)
                .toString();
    }
}
