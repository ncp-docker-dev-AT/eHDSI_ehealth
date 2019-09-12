package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CODE_SYSTEM_CONCEPT")
@SuppressWarnings("unused")
public class Concept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String definition;

    private String status;

    private LocalDateTime statusDate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CODE_SYSTEM_VERSION_ID", nullable = false)
    private CodeSystemVersion codeSystemVersion;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "concepts")
    private List<ValueSetVersion> valueSetVersions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "concept")
    private List<Designation> designations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "target")
    private List<Mapping> mappings = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public CodeSystemVersion getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public void setCodeSystemVersion(CodeSystemVersion codeSystemVersion) {
        this.codeSystemVersion = codeSystemVersion;
    }

    public List<ValueSetVersion> getValueSetVersions() {
        return valueSetVersions;
    }

    public void addValueSetVersion(ValueSetVersion valueSetVersion) {
        valueSetVersions.add(valueSetVersion);
        valueSetVersion.getConcepts().add(this);
    }

    public List<Designation> getDesignations() {
        return designations;
    }

    public void addDesignation(Designation designation) {
        designations.add(designation);
        designation.setConcept(this);
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void addMapping(Mapping mapping) {
        mappings.add(mapping);
        mapping.setTarget(this);
    }
}
