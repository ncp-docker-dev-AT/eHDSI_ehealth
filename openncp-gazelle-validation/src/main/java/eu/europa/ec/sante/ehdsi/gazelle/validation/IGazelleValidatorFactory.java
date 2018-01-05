package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface IGazelleValidatorFactory {

    AssertionValidator getAssertionValidator();

    AuditMessageValidator getAuditMessageValidator();

    CdaValidator getCdaValidator();

    CertificateValidator getCertificateValidator();

    SchematronValidator getSchematronValidator();

    XdsValidator getXdsValidator();
}
