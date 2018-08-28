package eu.epsos.protocolterminators.integrationtest.common;

import eu.epsos.assertionvalidator.XSPARole;
import eu.epsos.util.IheConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Test client that uses SAAJ (SOAP with Attachments API for Java) for calling web services. To be used for test
 * purposes only, for example integration-tests.
 *
 * @author gareth
 */
public class SimpleSoapClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleSoapClient.class);
    private static final String SECURITY_NS = IheConstants.SOAP_HEADERS.SECURITY_XSD;
    private static final String SECURITY_HEADER = "Security";
    private final String endpoint;

    /**
     * Instanciate the SimpleSoapClient
     *
     * @param endpoint URL for an NCP-A IHE Service Endpoint (eg.
     *                 http://localhost:8090/epsos-ws-server/services/XCA_Service/)
     */
    public SimpleSoapClient(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Main class for testing & development purposes
     *
     * @param args
     */
    public static void main(String[] args) {

        // load SOAP body content from file system
        String bodyStr = new ResourceLoader().getResource("/xca/AdhocQueryRequest.xml");
        LOG.error(bodyStr);
        Document doc = null;
        try {

            doc = XMLUtil.parseContent(bodyStr.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Exception: '{}'", e.getMessage(), e);
        }

        // build list of SAML2 assertions
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARole.PHYSICIAN);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion("", "");
        Collection<Assertion> assertions = new ArrayList<>();
        assertions.add(idAssertion);
        assertions.add(trcAssertion);

        // send soap request
        SimpleSoapClient client = new SimpleSoapClient("http://localhost:8080/epsos-ws-server/services/XCA_Service/");
        SOAPElement response;
        try {
            response = client.call(doc, assertions);
            LOG.info("");
            LOG.info(response.getValue());

        } catch (SOAPFaultException ex) {
            LOG.error("SOAPFaultException: '{}'", ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Convert any SOAP object that implements SOAPMessage into a String
     *
     * @param msg SOAP object
     * @return String
     * @throws SOAPException
     * @throws IOException
     */
    private static String getXmlFromSOAPMessage(SOAPMessage msg) throws SOAPException, IOException {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        msg.writeTo(byteArrayOS);
        return new String(byteArrayOS.toByteArray());
    }

    /**
     * Construct a SOAP message and send to web service endpoint.
     *
     * @param document   An XML document with the contents of the SOAP Body
     * @param assertions List of SAML2 assertions to be included in the SOAP Header
     * @return SOAPElement
     */
    public SOAPElement call(Document document, Collection<Assertion> assertions) throws SOAPFaultException {
        SOAPElement returnElement = null;
        try {
            // construct message
            SOAPMessage message = MessageFactory.newInstance().createMessage();

            // populate SOAP header
            SOAPHeader header = message.getSOAPHeader();
            SOAPElement security = header.addChildElement(SECURITY_HEADER, "wsse", SECURITY_NS);
            for (Assertion assertion : assertions) {
                AssertionMarshaller marshaller = new AssertionMarshaller();
                Element element = marshaller.marshall(assertion);
                SOAPFactory soapFactory = SOAPFactory.newInstance();
                SOAPElement assertionSOAP = soapFactory.createElement(element);
                security.addChildElement(assertionSOAP);
            }

            // populate SOAP body
            SOAPBody body = message.getSOAPBody();
            body.addDocument(document);
            LOG.debug("Request:\n'{}'", getXmlFromSOAPMessage(message));

            // do SOAP request
            SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = connection.call(message, endpoint);
            connection.close();

            // process response
            SOAPBody responseBody = response.getSOAPBody();
            LOG.debug("Response:\n'{}'", getXmlFromSOAPMessage(response));
            returnElement = (SOAPBodyElement) responseBody.getChildElements().next();

            /* If error present */
            if (responseBody.getFault() != null) {
                LOG.info(getXmlFromSOAPMessage(response));
                throw new SOAPFaultException(responseBody.getFault());
            }

        } catch (SOAPException | MarshallingException | IOException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }

        return returnElement;
    }
}
