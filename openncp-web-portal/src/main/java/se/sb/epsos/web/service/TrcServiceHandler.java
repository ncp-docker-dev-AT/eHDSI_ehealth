package se.sb.epsos.web.service;

import epsos.ccd.netsmart.securitymanager.sts.client.TRCAssertionRequest;
import org.opensaml.saml.saml2.core.Assertion;

public class TrcServiceHandler {

    public TRCAssertionRequest buildTrcRequest(Assertion assertion, String patientId, String purposeOfUse) throws Exception {
        return new TRCAssertionRequest.Builder(assertion, patientId).purposeOfUse(purposeOfUse).build();
    }
}
