package fi.kela.se.epsos.data.model;

import eu.europa.ec.sante.ehdsi.constant.ClassCode;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EPSOSDocumentImpl implements EPSOSDocument {

    private static final String HL7_NAMESPACE = "urn:hl7-org:v3";
    private final Logger logger = LoggerFactory.getLogger(EPSOSDocumentImpl.class);
    private final String patientId;
    private final ClassCode classCode;
    private final Document document;

    public EPSOSDocumentImpl(String patientId, ClassCode classCode, Document document) {
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
    public ClassCode getClassCode() {
        return classCode;
    }

    private String getDocumentId() {

        String uid = "";
        if (document != null && document.getElementsByTagNameNS(HL7_NAMESPACE, "id").getLength() > 0) {
            Node id = document.getElementsByTagNameNS(HL7_NAMESPACE, "id").item(0);
            if (id.getAttributes().getNamedItem("root") != null) {
                uid = uid + id.getAttributes().getNamedItem("root").getTextContent();
            }
            if (id.getAttributes().getNamedItem("extension") != null) {
                uid = uid + "^" + id.getAttributes().getNamedItem("extension").getTextContent();
            }
        }
        return uid;
    }

    @Override
    public boolean matchesCriteria(SearchCriteria searchCriteria) {

        logger.debug("Processing Search Criteria");
        String patientIdCriteria = searchCriteria.getCriteriaValue(Criteria.PATIENT_ID);
        String documentIdCriteria = searchCriteria.getCriteriaValue(Criteria.DOCUMENT_ID);

        if (patientIdCriteria != null && !patientIdCriteria.isEmpty()) {
            //TODO: EHNCP-2055 Inconsistency in handling patient id
            return StringUtils.contains(patientIdCriteria, this.patientId) && documentIdCriteria != null
                    && documentIdCriteria.equals(getDocumentId());
        } else {
            return documentIdCriteria != null && StringUtils.equals(documentIdCriteria, getDocumentId());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("patientId", patientId)
                .append("classCode", classCode)
                .append("document", document)
                .toString();
    }
}
