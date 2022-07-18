package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

import net.RFC3881.CodedValueType;
import org.joda.time.LocalDateTime;

import java.util.Objects;

public class AbuseEvent {

    CodedValueType requestEventType;
    String pointOfCare;
    String patientId;
    LocalDateTime requestDateTime;
    String filename;
    AbuseTransactionType transactionType;

    public AbuseEvent(CodedValueType requestEventType, String pointOfCare, String patientId, LocalDateTime requestDateTime, String filename, AbuseTransactionType transactionType) {
        this.requestEventType = requestEventType;
        this.pointOfCare = pointOfCare;
        this.patientId = patientId;
        this.requestDateTime = requestDateTime;
        this.filename = filename;
        this.transactionType = transactionType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbuseEvent that = (AbuseEvent) o;
        return Objects.equals(requestEventType, that.requestEventType) && Objects.equals(pointOfCare, that.pointOfCare) && Objects.equals(patientId, that.patientId) && Objects.equals(requestDateTime, that.requestDateTime) && Objects.equals(filename, that.filename) && transactionType == that.transactionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestEventType, pointOfCare, patientId, requestDateTime, filename, transactionType);
    }

    @Override
    public String toString() {
        return "AbuseEventNew{" +
                "requestEventType=" + requestEventType +
                ", pointOfCare='" + pointOfCare + '\'' +
                ", patientId='" + patientId + '\'' +
                ", requestDateTime=" + requestDateTime +
                ", filename='" + filename + '\'' +
                ", transactionType=" + transactionType +
                '}';
    }
}
