package fi.kela.se.epsos.data.model;

import java.util.Date;

/**
 * Generic EPSOSDocumentMetaData interface. EPDocumentMetaData and PSDocumentMetaData extends this interface.
 */
public interface EPSOSDocumentMetaData {

    int EPSOSDOCUMENT_FORMAT_XML = 1;
    int EPSOSDOCUMENT_FORMAT_PDF = 2;

    String getId();

    String getPatientId();

    int getFormat();

    Date getEffectiveTime();

    String getClassCode();

    String getRepositoryId();

    String getTitle();

    String getAuthor();
    
    String getLanguage();
    
    ConfidentialityMetadata getConfidentiality();
    
    interface ConfidentialityMetadata {
        String getConfidentialityCode();
        String getConfidentialityDisplay();
    }
}
