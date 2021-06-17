package fi.kela.se.epsos.data.model;

/**
 * EDispensationDocumentMetaData interface
 */
public interface OrCDDocumentMetaData extends EPSOSDocumentMetaData {

    enum DocumentFileType {
        PDF, PNG, JPEG;
    }
    DocumentFileType getDocumentFileType();

    long getSize();
}
