package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface SchematronValidator {

    String validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata);
}
