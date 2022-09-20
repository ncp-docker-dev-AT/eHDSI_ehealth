package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "EHNCP_ANOMALIES")
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String AnomalyDescription;

    private String anomalyType;

    private Date anomalyDateTime;

    private Date beginEventDateTime;

    private Date endEventDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnomalyDescription() {
        return AnomalyDescription;
    }

    public void setAnomalyDescription(String anomalyDescription) {
        AnomalyDescription = anomalyDescription;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public Date getAnomalyDateTime() {
        return anomalyDateTime;
    }

    public void setAnomalyDateTime(Date anomalyDateTime) {
        this.anomalyDateTime = anomalyDateTime;
    }

    public Date getBeginEventDateTime() {
        return beginEventDateTime;
    }

    public void setBeginEventDateTime(Date beginEventDateTime) {
        this.beginEventDateTime = beginEventDateTime;
    }

    public Date getEndEventDateTime() {
        return endEventDateTime;
    }

    public void setEndEventDateTime(Date endEventDateTime) {
        this.endEventDateTime = endEventDateTime;
    }
}
