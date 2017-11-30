package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface AssertionValidator {

    boolean validateDocument(String document, String validator);

    boolean validateBase64Document(String base64Document, String validator);
}
