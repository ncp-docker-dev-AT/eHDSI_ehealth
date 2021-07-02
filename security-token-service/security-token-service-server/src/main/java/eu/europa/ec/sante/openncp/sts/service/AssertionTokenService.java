package eu.europa.ec.sante.openncp.sts.service;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

public interface AssertionTokenService {

    Assertion generateNextOfKinToken(Assertion clinicianAssertion, String nextOfKinId) throws STSException;

    Assertion generateTreatmentConfirmationToken(Assertion clinicianAssertion, String patientId, String purposeOfUse) throws STSException;

    Assertion generateTreatmentConfirmationToken(Assertion clinicianAssertion, String patientId, String purposeOfUse,
                                                 String prescriptionId, String dispensePinCode) throws STSException;

    Document getSignedDocument(Assertion assertion) throws ParserConfigurationException, MarshallingException;
}
