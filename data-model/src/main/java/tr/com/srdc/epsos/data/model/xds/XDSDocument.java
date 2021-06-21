package tr.com.srdc.epsos.data.model.xds;

import tr.com.srdc.epsos.data.model.GenericDocumentCode;

/**
 * This class encapsulates a set of properties related to a document, but not it's content.
 */
public class XDSDocument {

    private String id;
    private String hcid;
    private String repositoryUniqueId;
    private String documentUniqueId;
    private String name;
    private String description;
    private String creationTime;
    private String healthcareFacility;
    private String authorPerson;
    private boolean isPDF;
    private GenericDocumentCode formatCode;
    private GenericDocumentCode classCode;

    private String atcCode;
    private String atcText;
    private String strength;
    private String doseFormCode;
    private String doseFormText;
    private String Substitution;

    public String getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public String getAtcText() {
        return atcText;
    }

    public void setAtcText(String atcText) {
        this.atcText = atcText;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDoseFormCode() {
        return doseFormCode;
    }

    public void setDoseFormCode(String doseFormCode) {
        this.doseFormCode = doseFormCode;
    }

    public String getDoseFormText() {
        return doseFormText;
    }

    public void setDoseFormText(String doseFormText) {
        this.doseFormText = doseFormText;
    }

    public String getSubstitution() {
        return Substitution;
    }

    public void setSubstitution(String substitution) {
        Substitution = substitution;
    }

    /**
     * @return the hcid
     */
    public String getHcid() {
        return hcid;
    }

    /**
     * @param hcid the hcid to set
     */
    public void setHcid(String hcid) {
        this.hcid = hcid;
    }

    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }

    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }

    public String getDocumentUniqueId() {
        return documentUniqueId;
    }

    public void setDocumentUniqueId(String documentUniqueId) {
        this.documentUniqueId = documentUniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getHealthcareFacility() {
        return healthcareFacility;
    }

    public void setHealthcareFacility(String healthcareFacility) {
        this.healthcareFacility = healthcareFacility;
    }

    public boolean isPDF() {
        return isPDF;
    }

    public void setPDF(boolean isPDF) {
        this.isPDF = isPDF;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the formatCode
     */
    public GenericDocumentCode getFormatCode() {
        return formatCode;
    }

    /**
     * @param formatCode the formatCode to set
     */
    public void setFormatCode(GenericDocumentCode formatCode) {
        this.formatCode = formatCode;
    }

    /**
     * @param schema the formatCode schema
     * @param value  the formatCode value
     */
    public void setFormatCode(String schema, String value) {
        GenericDocumentCode documentCode = new GenericDocumentCode();
        documentCode.setSchema(schema);
        documentCode.setValue(value);
        this.formatCode = documentCode;
    }

    /**
     * @return the classCode
     */
    public GenericDocumentCode getClassCode() {
        return classCode;
    }

    /**
     * @param classCode the classCode to set
     */
    public void setClassCode(GenericDocumentCode classCode) {
        this.classCode = classCode;
    }

    /**
     * @param schema the classCode schema
     * @param value  the classCode value
     */
    public void setClassCode(String schema, String value) {
        GenericDocumentCode documentCode = new GenericDocumentCode();
        documentCode.setSchema(schema);
        documentCode.setValue(value);
        this.classCode = documentCode;
    }

    /**
     * @return the authorPerson
     */
    public String getAuthorPerson() {
        return authorPerson;
    }

    /**
     * @param authorPerson the authorPerson to set
     */
    public void setAuthorPerson(String authorPerson) {
        this.authorPerson = authorPerson;
    }
}
