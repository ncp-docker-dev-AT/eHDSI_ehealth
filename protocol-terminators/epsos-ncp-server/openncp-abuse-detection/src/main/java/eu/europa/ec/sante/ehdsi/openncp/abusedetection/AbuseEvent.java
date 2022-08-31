package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

import net.RFC3881.AuditMessage;
import net.RFC3881.CodedValueType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

public class AbuseEvent {

    CodedValueType requestEventType;
    String pointOfCare;
    String patientId;
    LocalDateTime requestDateTime;
    String filename;
    AbuseTransactionType transactionType;

    AuditMessage audit;

    public AbuseEvent(CodedValueType requestEventType, String pointOfCare, String patientId, LocalDateTime requestDateTime,
                      String filename, AbuseTransactionType transactionType, AuditMessage audit) {
        this.requestEventType = requestEventType;
        this.pointOfCare = pointOfCare;
        this.patientId = patientId;
        this.requestDateTime = requestDateTime;
        this.filename = filename;
        this.transactionType = transactionType;
        this.audit = audit;
    }

    public CodedValueType getRequestEventType() {
        return requestEventType;
    }

    public void setRequestEventType(CodedValueType requestEventType) {
        this.requestEventType = requestEventType;
    }

    public String getPointOfCare() {
        return pointOfCare;
    }

    public void setPointOfCare(String pointOfCare) {
        this.pointOfCare = pointOfCare;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(LocalDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public AbuseTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(AbuseTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public AuditMessage getAudit() {
        return audit;
    }

    public void setAudit(AuditMessage audit) {
        this.audit = audit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AbuseEvent that = (AbuseEvent) o;

        return new EqualsBuilder()
                .append(requestEventType, that.requestEventType)
                .append(pointOfCare, that.pointOfCare)
                .append(patientId, that.patientId)
                .append(requestDateTime, that.requestDateTime)
                .append(filename, that.filename)
                .append(transactionType, that.transactionType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(requestEventType)
                .append(pointOfCare)
                .append(patientId)
                .append(requestDateTime)
                .append(filename)
                .append(transactionType).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestEventType", requestEventType)
                .append("pointOfCare", pointOfCare)
                .append("patientId", patientId)
                .append("requestDateTime", requestDateTime)
                .append("filename", filename)
                .append("transactionType", transactionType)
                .toString();
    }
}
