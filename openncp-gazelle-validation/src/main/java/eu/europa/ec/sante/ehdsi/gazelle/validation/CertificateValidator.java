package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface CertificateValidator {

    boolean validate(String certificate, String type, boolean checkRevocation);
}
