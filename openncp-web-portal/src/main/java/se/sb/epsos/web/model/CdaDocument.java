package se.sb.epsos.web.model;

import se.sb.epsos.shelob.ws.client.jaxws.EpsosDocument;
import se.sb.epsos.web.service.DocumentClientDtoCacheKey;
import se.sb.epsos.web.service.MetaDocument;

import java.util.ArrayList;
import java.util.List;

public abstract class CdaDocument extends MetaDocument {

    private static final long serialVersionUID = -7009718766448230201L;

    protected byte[] bytes;
    private List<ErrorFeedback> errorList = new ArrayList<>();
    private EpsosDocument epsosDocument;

    public CdaDocument(DocumentClientDtoCacheKey cacheKey) {
        super(cacheKey, true);
    }

    public CdaDocument(MetaDocument metaDoc) {
        super(metaDoc.getDtoCacheKey().getSessionId(), metaDoc.getDtoCacheKey().getPatientId(), metaDoc.getDoc(), true);
    }

    public CdaDocument(MetaDocument metaDoc, byte[] bytes, EpsosDocument epsosDocument) {
        super(metaDoc.getDtoCacheKey().getSessionId(), metaDoc.getDtoCacheKey().getPatientId(), metaDoc.getDoc(), true);
        this.bytes = bytes;
        this.epsosDocument = epsosDocument;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public List<ErrorFeedback> getError() {

        if (!errorList.isEmpty()) {
            errorList.clear();
        }
        return errorList;
    }
}
