package fi.kela.se.epsos.data.model;

import java.awt.desktop.UserSessionEvent;
import java.util.Date;

import java.util.ArrayList;
import java.util.List;

/**
 * Temporary implementation, since it is needed for the epsos-nc-mock-it module.
 * Idea is that is needs to be modified with the implementation of CP-047.
 *
 * @author ghysmat
 */
public class OrCDDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements OrCDDocumentMetaData {

    private DocumentFileType documentFileType;
    private long size;
    private Date serviceStartTime;
    private ReasonOfHospitalisation reasonOfHospitalisation;
    private List<Author> authors = new ArrayList<>();

    public OrCDDocumentMetaDataImpl(EPSOSDocumentMetaData metaData, DocumentFileType documentFileType, long size, Date serviceStartTime, ReasonOfHospitalisation reasonOfHospitalisation) {

    public OrCDDocumentMetaDataImpl(EPSOSDocumentMetaData metaData,
                                    DocumentFileType documentFileType,
                                    long size,
                                    List<Author> authors) {
        super(metaData);
        this.documentFileType = documentFileType;
        this.size = size;
        this.serviceStartTime = serviceStartTime;
        this.reasonOfHospitalisation = reasonOfHospitalisation;
        this.authors = authors;
    }

    @Override
    public DocumentFileType getDocumentFileType() {
        return documentFileType;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public Date getServiceStartTime() {
        return serviceStartTime;
    }

    @Override
    public ReasonOfHospitalisation getReasonOfHospitalisation() {
        return reasonOfHospitalisation;
    }

    @Override
    public List<Author> getAuthors() {
        return authors;
    }


}
