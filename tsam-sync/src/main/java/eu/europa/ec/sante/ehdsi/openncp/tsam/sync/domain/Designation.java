package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "designation")
@SuppressWarnings("unused")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String designation;

    @Column(name = "language_code")
    private String languageCode;

    private String type;

    @Column(name = "is_preferred")
    private Boolean isPreferred;

    private String status;

    @Column(name = "status_date")
    private LocalDateTime statusDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODE_SYSTEM_CONCEPT_ID")
    private Concept concept;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getPreferred() {
        return isPreferred;
    }

    public void setPreferred(Boolean preferred) {
        isPreferred = preferred;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDateTime statusDate) {
        this.statusDate = statusDate;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept codeSystemConcept) {
        this.concept = codeSystemConcept;
    }
}
