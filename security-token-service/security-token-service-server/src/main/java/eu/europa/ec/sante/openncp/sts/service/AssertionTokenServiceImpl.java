package eu.europa.ec.sante.openncp.sts.service;

import epsos.ccd.netsmart.securitymanager.SamlTRCIssuer;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import eu.europa.ec.sante.openncp.securitymanager.SamlNextOfKinIssuer;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Service
public class AssertionTokenServiceImpl implements AssertionTokenService {

    @Override
    public Assertion generateNextOfKinToken(Assertion clinicianAssertion, String nextOfKinId) throws STSException {

        try {

            var samlTRCIssuer = new SamlNextOfKinIssuer();
            return samlTRCIssuer.issueNextOfKinToken(clinicianAssertion, "doctorId", clinicianAssertion.getID(), null);
        } catch (SMgrException e) {
            throw new STSException();
        }
//        Assertion hcpIdAssertion = getIdAssertionFromHeader(header);
//        if (hcpIdAssertion != null) {
//            logger.info("hcpIdAssertion: '{}'", hcpIdAssertion.getID());
//            if (hcpIdAssertion.getIssueInstant() != null) {
//                logger.info("hcpIdAssertion Issue Instant: '{}'", hcpIdAssertion.getIssueInstant());
//            }
//        }
//        Assertion trc = samlTRCIssuer.issueTrcToken(null, "", "", "","", null);
//        if (hcpIdAssertion != null) {
//            logger.info("HCP Assertion Date: '{}' TRC Assertion Date: '{}' -- '{}'",
//                    hcpIdAssertion.getIssueInstant().atZone(ZoneId.of("UTC")),
//                    trc.getIssueInstant().atZone(ZoneId.of("UTC")), trc.getAuthnStatements().isEmpty());
//        }

//        SOAPMessage response = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
//        response.getSOAPBody().addDocument(STSUtils.createRSTRC(signedDoc));
//        createResponseHeader(response.getSOAPHeader(), mid);
//        String strRespHeader = STSUtils.domElementToString(response.getSOAPHeader());
//        String strReqHeader = STSUtils.domElementToString(header);
    }

    @Override
    public Assertion generateTreatmentConfirmationToken(Assertion clinicianAssertion, String patientId, String purposeOfUse) throws STSException {

        try {
            var samlTRCIssuer = new SamlTRCIssuer();
            return samlTRCIssuer.issueTrcToken(clinicianAssertion, patientId, purposeOfUse, null);

        } catch (SMgrException e) {
            throw new STSException();
        }
    }

    @Override
    public Assertion generateTreatmentConfirmationToken(Assertion clinicianAssertion, String patientId, String purposeOfUse,
                                                        String prescriptionId, String dispensePinCode) throws STSException {
        try {
            var samlTRCIssuer = new SamlTRCIssuer();
            return samlTRCIssuer.issueTrcToken(clinicianAssertion, patientId, purposeOfUse, dispensePinCode, prescriptionId, null);

        } catch (SMgrException e) {
            throw new STSException();
        }
    }

    @Override
    public Document getSignedDocument(Assertion assertion) throws ParserConfigurationException, MarshallingException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document signedDocument = builder.newDocument();
        var marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        marshallerFactory.getMarshaller(assertion).marshall(assertion, signedDocument);
        return signedDocument;
    }
}
