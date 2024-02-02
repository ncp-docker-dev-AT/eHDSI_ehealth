package fi.kela.se.epsos.data.model;

import java.util.Date;
import java.util.List;

/**
 * Temporary implementation, since it is needed for the epsos-nc-mock-it module.
 * Idea is that is needs to be modified with the implementation of CP-047.
 *
 * @author ghysmat
 */
public class OrCDDocumentMetaDataImpl extends EPSOSDocumentMetaDataImpl implements OrCDDocumentMetaData {

    private DocumentFileType documentFileType;
    private Date serviceStartTime;

    private String description;
    private List<Author> authors;
    private ReasonOfHospitalisation reasonOfHospitalisation;

    public OrCDDocumentMetaDataImpl(EPSOSDocumentMetaData metaData,
                                    DocumentFileType documentFileType,
                                    String description,
                                    Date serviceStartTime,
                                    List<Author> authors,
                                    ReasonOfHospitalisation reasonOfHospitalisation) {
        super(metaData);
        this.documentFileType = documentFileType;
        this.description = description;
        this.serviceStartTime = serviceStartTime;
        this.authors = authors;
        this.reasonOfHospitalisation = reasonOfHospitalisation;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public DocumentFileType getDocumentFileType() {
        return documentFileType;
    }

    @Override
    public Date getServiceStartTime() {
        return serviceStartTime;
    }

    @Override
    public List<Author> getAuthors() {
        return authors;
    }

    @Override
    public ReasonOfHospitalisation getReasonOfHospitalisation() {
        return reasonOfHospitalisation;
    }
}
