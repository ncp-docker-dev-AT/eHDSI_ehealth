package eu.europa.ec.sante.ehdsi.gazelle.validation;

/**
 *
 */
public interface AuditMessageValidator {

    /**
     * @param document
     * @param validator
     * @return
     */
    String validateDocument(String document, String validator);

    /**
     * @param base64Document
     * @param validator
     * @return
     */
    String validateBase64Document(String base64Document, String validator);
}
