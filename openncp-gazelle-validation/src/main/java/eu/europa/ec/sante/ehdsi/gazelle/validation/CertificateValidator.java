package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface CertificateValidator {

    String validate(String certificate, String type, boolean checkRevocation);
}
