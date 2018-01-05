package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface XdsValidator {

    boolean validateDocument(String document, String validator);

    boolean validateBase64Document(String base64Document, String validator);
}
