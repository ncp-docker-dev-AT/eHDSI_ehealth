package fi.kela.se.epsos.data.model;

import eu.europa.ec.sante.ehdsi.constant.ClassCode;

public class DocumentAssociationImpl<T extends EPSOSDocumentMetaData> implements DocumentAssociation<T> {

    private final T xmlDocument;
    private final T pdfDocument;

    public DocumentAssociationImpl(T xmlDocument, T pdfDocument) {
        this.xmlDocument = xmlDocument;
        this.pdfDocument = pdfDocument;
    }

    @Override
    public ClassCode getDocumentClassCode(String documentId) {
        T document = getDocument(documentId);
        return document == null ? null : document.getClassCode();
    }

    @Override
    public String getPatientId(String documentId) {
        T document = getDocument(documentId);
        return document == null ? null : document.getPatientId();
    }

    private T getDocument(String documentId) {
        if (xmlDocument.getId().equals(documentId)) {
            return xmlDocument;
        } else if (pdfDocument.getId().equals(documentId)) {
            return pdfDocument;
        }
        return null;
    }

    @Override
    public T getXMLDocumentMetaData() {
        return xmlDocument;
    }

    @Override
    public T getPDFDocumentMetaData() {
        return pdfDocument;
    }

    @Override
    public String toString() {
        return "DocumentAssociationImpl [xmlDocument=" + xmlDocument
                + ", pdfDocument=" + pdfDocument + "]";
    }
}
