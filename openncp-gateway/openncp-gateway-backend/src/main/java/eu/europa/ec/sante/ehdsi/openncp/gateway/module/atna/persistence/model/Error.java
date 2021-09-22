package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "errors")
public class Error {

    @Id
    private Long id;

    private String errorMessage;

    private Instant errorTimestamp;

    private String payload;

    private String sourceIp;

    private String stackTrace;

    private String version;

    public Long getId() {
        return id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getErrorTimestamp() {
        return errorTimestamp;
    }

    public String getPayload() {
        return payload;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getVersion() {
        return version;
    }
}
