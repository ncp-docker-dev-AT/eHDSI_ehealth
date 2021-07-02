package eu.europa.ec.sante.openncp.sts.ws;

import eu.ehdsi.openncp.sts.cxf.MessageBody;
import eu.ehdsi.openncp.sts.cxf.SecurityTokenServicePortType;
import eu.europa.ec.sante.openncp.sts.SecurityTokenServiceUtil;
import eu.europa.ec.sante.openncp.sts.XmlUtil;
import eu.europa.ec.sante.openncp.sts.service.AssertionTokenService;
import eu.europa.ec.sante.openncp.sts.service.STSException;
import org.apache.cxf.headers.Header;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

@Component
@WebService(targetNamespace = "https://ehdsi.eu/", serviceName = "SecurityTokenService",
        portName = "SecurityTokenServicePort", wsdlLocation = "classpath:/wsdl/SecurityTokenService.wsdl",
        endpointInterface = "eu.ehdsi.openncp.sts.cxf.SecurityTokenServicePortType")
public class IdentityProviderServiceEndpoint extends AbstractServiceEndpoint implements SecurityTokenServicePortType {

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            // SAML Framework cannot be initialized correctly.
        }
    }

    private final Logger logger = LoggerFactory.getLogger(IdentityProviderServiceEndpoint.class);

    @Autowired
    AssertionTokenService assertionTokenService;

    @Override
    public MessageBody issueTreatmentToken(MessageBody rstMessage) {

        logger.info("[STS] issueTreatmentToken");
        List<Object> objectList = rstMessage.getAny();
        logger.info("Return: '{}'", objectList.toString());
        return null;
    }

    @Override
    public MessageBody issueNextOfKinToken(MessageBody rstMessage) {
        logger.info("[STS] issueNextOfKinToken");
        List<Header> headers = getSoapHeader();
        for (Header header : headers) {
            if (((Element) header.getObject()).getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) header.getObject();
                logger.info("Message ID: '{}'", element.getTextContent());
            }
        }

        List<Object> objectList = rstMessage.getAny();
        for (Object o : objectList) {
            logger.info("Object: '{}'", o.getClass());
            Document document = ((Element) o).getOwnerDocument();
            logger.info("Document:\n'{}'", XmlUtil.documentToString(document));
            logger.info("Patient ID: '{}'", SecurityTokenServiceUtil.getPatientID(document));
        }

        Document signedDocument = null;
        try {
            var assertion = assertionTokenService.generateNextOfKinToken(null, "NextOfKinID");
            signedDocument = assertionTokenService.getSignedDocument(assertion);
        } catch (ParserConfigurationException | MarshallingException | STSException e) {
            logger.error("Signed document cannot be retrieved");
        }
        Document requestSecurityTokenResponse = SecurityTokenServiceUtil.createRequestSecurityTokenResponse(signedDocument);
        var messageBody = new MessageBody();
        messageBody.getAny().add(requestSecurityTokenResponse.getDocumentElement());
        return messageBody;
    }
}
