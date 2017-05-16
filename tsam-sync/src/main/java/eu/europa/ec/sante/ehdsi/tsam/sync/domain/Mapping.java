package eu.europa.ec.sante.ehdsi.tsam.sync.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcoding_association")
public class Mapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "transcoding_association_id")
    private Long transcodingAssociationId = 1L;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_concept_id")
    private CodeSystemEntity source;

    @ManyToOne
    @JoinColumn(name = "target_concept_id")
    private CodeSystemEntity target;

    private String quality;

    private String status;

    @Column(name = "status_date")
    private LocalDateTime statusDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CodeSystemEntity getSource() {
        return source;
    }

    public void setSource(CodeSystemEntity source) {
        this.source = source;
    }

    public CodeSystemEntity getTarget() {
        return target;
    }

    public void setTarget(CodeSystemEntity target) {
        this.target = target;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
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
}
