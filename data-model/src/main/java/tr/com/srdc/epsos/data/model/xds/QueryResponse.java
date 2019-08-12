package tr.com.srdc.epsos.data.model.xds;

import java.util.List;

public class QueryResponse {

    private List<XDSDocumentAssociation> documentAssociations;
    private List<String> failureMessages;

    public List<XDSDocumentAssociation> getDocumentAssociations() {
        return documentAssociations;
    }

    public void setDocumentAssociations(List<XDSDocumentAssociation> documentAssociations) {
        this.documentAssociations = documentAssociations;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }
}
