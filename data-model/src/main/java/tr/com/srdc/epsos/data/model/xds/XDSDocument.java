package tr.com.srdc.epsos.data.model.xds;

import fi.kela.se.epsos.data.model.OrCDDocumentMetaData;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates a set of properties related to a document, but not it's content.
 */
public class XDSDocument {

    private String id;
    private String hcid;
    private String repositoryUniqueId;
    private String documentUniqueId;
    private String mimeType;
    private String name;
    private String size;
    private String description;
    private String creationTime;
    private String eventTime;
    private String healthcareFacility;
    private boolean isPDF;
    private OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation;
    private List<OrCDDocumentMetaData.Author> authors = new ArrayList<>();

    private GenericDocumentCode formatCode;
    private GenericDocumentCode classCode;

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

    public String getSize() { return size; }

    public void setSize(String size) { this.size = size; }

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

    public String getEventTime() { return eventTime; }

    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

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

    public String getMimeType() { return mimeType; }

    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public OrCDDocumentMetaData.ReasonOfHospitalisation getReasonOfHospitalisation() { return reasonOfHospitalisation; }

    public void setReasonOfHospitalisation(OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) { this.reasonOfHospitalisation = reasonOfHospitalisation; }

    public List<OrCDDocumentMetaData.Author> getAuthors() { return authors; }

    public void setAuthors(List<OrCDDocumentMetaData.Author> authors) { this.authors = authors; }

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

}
