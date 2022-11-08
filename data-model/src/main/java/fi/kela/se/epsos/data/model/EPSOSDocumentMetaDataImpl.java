package fi.kela.se.epsos.data.model;

import eu.europa.ec.sante.ehdsi.constant.ClassCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import tr.com.srdc.epsos.data.model.SimpleConfidentialityEnum;

import java.util.Date;

public class EPSOSDocumentMetaDataImpl implements EPSOSDocumentMetaData {

    private final String id;
    private final String patientId;
    private final int documentFormat;
    private final Date effectiveDate;
    private final ClassCode classCode;
    private final String repositoryId;
    private final String title;
    private final String author;
    private final ConfidentialityMetadata confidentiality;
    private final String language;
    private final Long size;
    private final String hash;

    @Deprecated
    public EPSOSDocumentMetaDataImpl(String id, String patientId, int documentFormat, Date effectiveDate,
                                     ClassCode classCode, String repositoryId, String title, String author, Long size, String hash) {

        this.id = id;
        this.patientId = patientId;
        this.documentFormat = documentFormat;
        this.effectiveDate = effectiveDate;
        this.classCode = classCode;
        this.repositoryId = repositoryId;
        this.title = title;
        this.author = author;
        this.confidentiality = new SimpleConfidentialityMetadata(SimpleConfidentialityEnum.N);
        this.language = null;
        this.size = size;
        this.hash = hash;
    }

    public EPSOSDocumentMetaDataImpl(String id, String patientId, int documentFormat, Date effectiveDate,
                                     ClassCode classCode, String repositoryId, String title, String author,
                                     ConfidentialityMetadata confidentiality, String language, Long size, String hash) {

        this.id = id;
        this.patientId = patientId;
        this.documentFormat = documentFormat;
        this.effectiveDate = effectiveDate;
        this.classCode = classCode;
        this.repositoryId = repositoryId;
        this.title = title;
        this.author = author;
        this.confidentiality = confidentiality;
        this.language = language;
        this.size = size;
        this.hash = hash;
    }

    public EPSOSDocumentMetaDataImpl(EPSOSDocumentMetaData metaData) {

        this.id = metaData.getId();
        this.patientId = metaData.getPatientId();
        this.documentFormat = metaData.getFormat();
        this.effectiveDate = metaData.getEffectiveTime();
        this.classCode = metaData.getClassCode();
        this.repositoryId = metaData.getRepositoryId();
        this.title = metaData.getTitle();
        this.author = metaData.getAuthor();
        this.confidentiality = metaData.getConfidentiality();
        this.language = metaData.getLanguage();
        this.size = metaData.getSize();
        this.hash = metaData.getHash();
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public int getFormat() {
        return documentFormat;
    }

    public Date getEffectiveTime() {
        return effectiveDate;
    }

    public ClassCode getClassCode() {
        return classCode;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public ConfidentialityMetadata getConfidentiality() {
        return confidentiality;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public Long getSize() {
        return size;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("patientId", patientId)
                .append("documentFormat", documentFormat)
                .append("effectiveDate", effectiveDate)
                .append("classCode", classCode.getCode())
                .append("repositoryId", repositoryId)
                .append("title", title)
                .append("author", author)
                .append("confidentialityCode", confidentiality.getConfidentialityCode())
                .append("confidentialityDisplay", confidentiality.getConfidentialityDisplay())
                .append("size", size)
                .append("hash", hash)
                .toString();
    }

    public static class SimpleConfidentialityMetadata implements ConfidentialityMetadata {

        private final SimpleConfidentialityEnum confidentiality;

        public SimpleConfidentialityMetadata(SimpleConfidentialityEnum confidentiality) {
            this.confidentiality = confidentiality;
        }

        @Override
        public String getConfidentialityCode() {
            return confidentiality.name();
        }

        @Override
        public String getConfidentialityDisplay() {
            return confidentiality.getDisplayName();
        }
    }
}
