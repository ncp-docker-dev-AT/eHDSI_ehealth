package se.sb.epsos.web.service;

import se.sb.epsos.shelob.ws.client.jaxws.EpsosDocument;
import se.sb.epsos.web.model.DocType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MetaDocument implements Serializable {

    private static final long serialVersionUID = 1L;
    private DocumentClientDtoCacheKey dtoCacheKey;
    private EpsosDocument doc;

    public MetaDocument(DocumentClientDtoCacheKey cacheKey, boolean forceCacheOverride) {

        super();
        this.dtoCacheKey = cacheKey;
        this.doc = new EpsosDocument();
        this.doc.setUuid(cacheKey.getDocumentId());
        if (DocumentCache.getInstance().contains(dtoCacheKey) && forceCacheOverride) {
            DocumentCache.getInstance().flush(dtoCacheKey);
        }
        DocumentCache.getInstance().put(this.dtoCacheKey, this);
    }

    public MetaDocument(String sessionId, String patientId, EpsosDocument doc) {
        this(sessionId, patientId, doc, false);
    }

    public MetaDocument(String sessionId, String patientId, EpsosDocument doc, boolean forceCacheOverride) {

        this(new DocumentClientDtoCacheKey(sessionId, patientId, doc.getUuid()), forceCacheOverride);
        this.doc = doc;
    }

    public DocType getType() {

        if (doc != null) {
            return DocType.getFromFormatCode(doc.getFormatCode().getNodeRepresentation());
        }
        return null;
    }

    public String getDescription() {

        if (doc != null && doc.getDescription() != null) {
            return doc.getDescription();
        }
        return null;
    }

    public String getTitle() {

        if (doc != null && doc.getTitle() != null) {
            return doc.getTitle();
        }
        return null;
    }

    public String getAuthor() {

        if (doc != null && doc.getAuthor() != null) {
            return doc.getAuthor();
        }
        return null;
    }

    public Date getCreationDate() {

        if (doc != null && doc.getCreationDate() != null) {

            return doc.getCreationDate().toGregorianCalendar().getTime();
        }
        return null;
    }

    public EpsosDocument getDoc() {
        return doc;
    }

    public DocumentClientDtoCacheKey getDtoCacheKey() {
        return dtoCacheKey;
    }

    public EpsosDocument getChildDocumentPdf() {

        List<EpsosDocument> associatedDocuments = doc.getAssociatedDocuments();
        if (associatedDocuments != null && !associatedDocuments.isEmpty()) {
            for (EpsosDocument epsosDocument : associatedDocuments) {
                if (DocType.PDF.toString().equals(epsosDocument.getFormatCode().getNodeRepresentation())) {
                    return epsosDocument;
                }
            }
        }
        return null;
    }
}
