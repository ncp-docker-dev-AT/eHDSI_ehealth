package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EPSOSDocumentImpl implements EPSOSDocument {

    private static final String HL7_NAMESPACE = "urn:hl7-org:v3";
    private final Logger logger = LoggerFactory.getLogger(EPSOSDocumentImpl.class);
    private String patientId;
    private String classCode;
    private Document document;

    public EPSOSDocumentImpl(String patientId, String classCode, Document document) {
        this.patientId = patientId;
        this.classCode = classCode;
        this.document = document;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getClassCode() {
        return classCode;
    }

    private String getDocumentId() {

        String oid = "";
        if (document != null && document.getElementsByTagNameNS(HL7_NAMESPACE, "id").getLength() > 0) {
            Node id = document.getElementsByTagNameNS(HL7_NAMESPACE, "id").item(0);
            if (id.getAttributes().getNamedItem("root") != null) {
                oid = oid + id.getAttributes().getNamedItem("root").getTextContent();
            }
            if (id.getAttributes().getNamedItem("extension") != null) {
                oid = oid + "^" + id.getAttributes().getNamedItem("extension").getTextContent();
            }
        }
        logger.info("Document ID: '{}'", oid);
        return oid;
    }

    @Override
    public boolean matchesCriteria(SearchCriteria sc) {

        String patientId = sc.getCriteriaValue(Criteria.PatientId);
        String documentId = sc.getCriteriaValue(Criteria.DocumentId);

        if (patientId != null && !patientId.isEmpty()) {
            return patientId.equals(this.patientId) && documentId != null && documentId.equals(getDocumentId());
        } else {
            return documentId != null && documentId.equals(getDocumentId());
        }
    }

    @Override
    public String toString() {

        return "EPSOSDocumentImpl [patientId = " + patientId + ", document=" + document + "]";
    }
}
