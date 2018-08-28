package se.sb.epsos.web.service;

import se.sb.epsos.web.util.EpsosStringUtils;

import java.io.Serializable;

/**
 * @author andreas
 */
public class DocumentClientDtoCacheKey implements Serializable {
    private static final long serialVersionUID = 8619078236148769223L;
    private String sessionId;
    private String patientId;
    private String documentId;

    public DocumentClientDtoCacheKey(String sessionId, String patientId, String documentId) {
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.documentId = documentId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDocumentId() {
        return documentId;
    }

    @Override
    public int hashCode() {
        return (sessionId + patientId + documentId).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof DocumentClientDtoCacheKey)) return false;
        DocumentClientDtoCacheKey key = (DocumentClientDtoCacheKey) o;
        boolean test = EpsosStringUtils.nullSafeCompare(this.sessionId, key.getSessionId());
        test = test && EpsosStringUtils.nullSafeCompare(this.patientId, key.getPatientId());
        test = test && EpsosStringUtils.nullSafeCompare(this.documentId, key.getDocumentId());
        return test;
    }

    @Override
    public String toString() {
        return "{" + this.sessionId + ", " + this.patientId + ", " + this.documentId + "}";
    }
}
