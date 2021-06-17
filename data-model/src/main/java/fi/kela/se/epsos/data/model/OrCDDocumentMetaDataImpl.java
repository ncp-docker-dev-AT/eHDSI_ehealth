package fi.kela.se.epsos.data.model;

/**
 * Temporary implementation, since it is needed for the epsos-nc-mock-it module.
 * Idea is that is needs to be modified with the implementation of CP-047.
 *
 * @author ghysmat
 */
public class OrCDDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements OrCDDocumentMetaData {

    private DocumentFileType documentFileType;
    private long size;

    public OrCDDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, DocumentFileType documentFileType, long size) {
        super(metaData);
        this.documentFileType = documentFileType;
        this.size = size;
    }

    @Override
    public DocumentFileType getDocumentFileType() {
        return documentFileType;
    }

    @Override
    public long getSize() {
        return size;
    }
}
