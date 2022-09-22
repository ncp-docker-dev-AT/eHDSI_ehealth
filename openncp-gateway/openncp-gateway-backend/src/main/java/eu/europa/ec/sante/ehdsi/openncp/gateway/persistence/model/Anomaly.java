package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "EHNCP_ANOMALY")
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    private String description;

    private String type;

    @Column(name = "EVENT_DATE")
    private Date eventDate;

    @Column(name = "EVENT_START_DATE")
    private Date eventStartDate;

    @Column(name = "EVENT_END_DATE")
    private Date eventEndDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }
}
