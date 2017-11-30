package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface SchematronValidator {

    boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata);
}
