package eu.europa.ec.sante.openncp.sts.client;

import org.opensaml.saml.saml2.core.Assertion;

public interface SecurityTokenServiceClient {

    Assertion issueNextOfKinToken();

    Assertion issueTreatmentConfirmationToken();
}
