package eu.europa.ec.sante.ehdsi.gazelle.validation;

public interface IGazelleValidatorFactory {

    AssertionValidator getAssertionValidator();

    AuditMessageValidator getAuditMessageValidator();

    CdaValidator getCdaValidator();

    CertificateValidator getCertificateValidator();

    HL7v3Validator getHL7v3Validator();

    SchematronValidator getSchematronValidator();

    XdsValidator getXdsValidator();
}
