package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain;

public class Concept {

    private Long id;

    private String code;

    private String codeSystemOid;

    private String codeSystemName;

    public Concept(Long id, String code, String codeSystemOid, String codeSystemName) {
        this.id = id;
        this.code = code;
        this.codeSystemOid = codeSystemOid;
        this.codeSystemName = codeSystemName;
    }

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

    public String getCodeSystemOid() {
        return codeSystemOid;
    }

    public void setCodeSystemOid(String codeSystemOid) {
        this.codeSystemOid = codeSystemOid;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }
}
