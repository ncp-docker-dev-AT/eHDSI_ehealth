package eu.epsos.protocolterminators.integrationtest.common;

import eu.epsos.assertionvalidator.XSPARole;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaExtraction;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaModel;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.TransformerException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public abstract class AbstractIT {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);
    private static final String ROOT_ID = "2.16.17.710.804.1000.990.1";
    private static final String EXTENSION = "6212122451";
    protected static String epr;
    protected Collection<Assertion> assertions;

    @AfterClass
    public static void tearDownClass() {
        LOGGER.info("");
    }

    public static Document readDoc(String name) {

        Document doc = null;

        try {
            String bodyStr = new ResourceLoader().getResource(name); // load SOAP body
            Assert.assertTrue("Empty request body", bodyStr != null && !bodyStr.isEmpty());
            if (StringUtils.isNotBlank(bodyStr)) {
                doc = XMLUtil.parseContent(bodyStr.getBytes(StandardCharsets.UTF_8));
            } else {
                Assert.fail("SOAP Body is empty");
            }
        } catch (ParserConfigurationException ex) {
            LOGGER.error("ParserConfigurationException: '{}'", ex.getMessage(), ex);
            Assert.fail(ex.getLocalizedMessage());
        } catch (SAXException ex) {
            LOGGER.error("SAXException: '{}'", ex.getMessage(), ex);
            Assert.fail(ex.getLocalizedMessage());
        } catch (IOException ex) {
            LOGGER.error("IOException: '{}'", ex.getMessage(), ex);
            Assert.fail(ex.getLocalizedMessage());
        }

        return doc;
    }

    protected static String extractErrorElem(SOAPElement response) {

        SOAPElement ack = (SOAPElement) response.getChildElements(new QName(response.getNamespaceURI(), "acknowledgement")).next();

        try {
            SOAPElement msg = (SOAPElement) ack.getChildElements(new QName(ack.getNamespaceURI(), "acknowledgementDetail")).next();
            SOAPElement txt = (SOAPElement) msg.getChildElements(new QName(msg.getNamespaceURI(), "text")).next();

            return txt.getTextContent();

        } catch (NoSuchElementException ex) {
            LOGGER.error("NoSuchElementException: '{}'", ex.getMessage(), ex);
            return null;
        }
    }

    protected static String success(String testName) {
        return "\u2714 " + testName;
    }

    protected static String fail(String testName) {
        return "\u2718 " + testName;
    }

    protected static SOAPElement extractElem(SOAPElement response, String[] path) {

        try {
            QName nextElem = new QName(response.getNamespaceURI(), path[0]);
            SOAPElement auxElem = (SOAPElement) response.getChildElements(nextElem).next();

            for (int i = 1; i < path.length; i++) {
                nextElem = new QName(response.getNamespaceURI(), path[i]);
                auxElem = (SOAPElement) auxElem.getChildElements(nextElem).next();
            }

            return auxElem;

        } catch (NoSuchElementException ex) {
            LOGGER.error("NoSuchElementException: '{}'", ex.getMessage(), ex);
            return null;
        }
    }

    protected static Collection<Assertion> hcpAssertionCreate(XSPARole role) {

        Collection<Assertion> assertions = new ArrayList<>(1);
        assertions.add(HCPIAssertionCreator.createHCPIAssertion(role));

        return assertions;
    }

    protected static Collection<Assertion> hcpAndTrcAssertionCreate(XSPARole role) {

        Collection<Assertion> assertions = new ArrayList<>(2);

        Assertion hcpAssertion = HCPIAssertionCreator.createHCPIAssertion(role);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion(ROOT_ID, EXTENSION, hcpAssertion.getID());

        assertions.add(hcpAssertion);
        assertions.add(trcAssertion);

        return assertions;
    }

    protected static Collection<Assertion> hcpAssertionCreate(List<String> permissions, XSPARole role) {

        Collection<Assertion> assertions = new ArrayList<>(1);
        assertions.add(HCPIAssertionCreator.createHCPIAssertion(permissions, role));

        return assertions;
    }

    protected static Collection<Assertion> hcpAndTrcAssertionCreate(String patientIdIso, XSPARole role) {

        Collection<Assertion> assertions = new ArrayList<>(2);

        Assertion hcpAssertion = HCPIAssertionCreator.createHCPIAssertion(role);
        Assertion trcAssertion;

        if (patientIdIso == null || patientIdIso.isEmpty()) {
            trcAssertion = TRCAssertionCreator.createTRCAssertion(ROOT_ID, EXTENSION, hcpAssertion.getID());
        } else {
            trcAssertion = TRCAssertionCreator.createTRCAssertion(patientIdIso, hcpAssertion.getID());
        }

        assertions.add(hcpAssertion);
        assertions.add(trcAssertion);

        return assertions;
    }

    protected static Collection<Assertion> hcpAndTrcAssertionCreate(String patientIdIso, List<String> permissions, XSPARole role) {

        Collection<Assertion> assertions = new ArrayList<>(2);

        Assertion hcpAssertion = HCPIAssertionCreator.createHCPIAssertion(permissions, role);
        Assertion trcAssertion;


        if (patientIdIso == null || patientIdIso.isEmpty()) {
            trcAssertion = TRCAssertionCreator.createTRCAssertion("2.16.17.710.804.1000.990.1", "6212122451", hcpAssertion.getID());
        } else {
            trcAssertion = TRCAssertionCreator.createTRCAssertion(patientIdIso, hcpAssertion.getID());
        }

        assertions.add(hcpAssertion);
        assertions.add(trcAssertion);

        return assertions;
    }

    protected static void validateCDA(String reqFilePath, CdaExtraction.MessageType msgType, CdaModel model) {

        //Extract document
        String base64Doc = CdaExtraction.extract(msgType, reqFilePath);
        invokeIheCdaService(base64Doc, msgType, model);

    }

    protected static void validateCDA(SOAPElement soapBody, CdaExtraction.MessageType msgType, CdaModel model) {

        String base64Doc = null;
        SOAPBodyElement body = (SOAPBodyElement) soapBody;
        try {
            base64Doc = CdaExtraction.extract(msgType, XMLUtil.parseContent(XMLUtil.prettyPrint(body.getFirstChild())
                    .getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | TransformerException | IOException | SAXException ex) {
            LOGGER.error("An error has occurred during CDA Validation.", ex);
        }
        invokeIheCdaService(base64Doc, msgType, model);

    }

    private static void invokeIheCdaService(String base64Doc, CdaExtraction.MessageType msgType, CdaModel model) {

//        try {
//
//            ModelBasedValidationWSService service = new ModelBasedValidationWSService();
//            ModelBasedValidationWS port = service.getModelBasedValidationWSPort();
//
//            // Validate document against IHE services
//            DetailedResult detailedResult = CdaExtraction.unmarshalDetails(port.validateBase64Document(base64Doc, model.getName()));
//            LOGGER.info("\u21b3Document validation result");
//            LOGGER.info(" \u251c\u2500Service Name: " + detailedResult.getValResultsOverview().getValidationServiceName().split(" : ")[1]);
//            LOGGER.info(" \u251c\u2500XSD: " + detailedResult.getDocumentValidXsd().getResult());
//            LOGGER.info(" \u251c\u2500Document Well Formed: " + detailedResult.getDocumentWellFormed().getResult());
//            LOGGER.info(" \u251c\u2500Number of Errors: " + detailedResult.getMdaValidation().getNotes().size());
//            LOGGER.info(" \u2514\u2500Overall Result: " + detailedResult.getValResultsOverview().getValidationTestResult());
//        } catch (SOAPException_Exception ex) {
//            LOGGER.error("An error has occurred during CDA Validation.", ex);
//        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        LOGGER.info("----------------------------");
    }

    protected String soapElementToString(SOAPElement element) {

        String str = null;
        try {
            str = XMLUtil.prettyPrint(element);
        } catch (Exception ex) {
            LOGGER.error("Exception: '{}'", ex.getMessage(), ex);
            Assert.fail(ex.getLocalizedMessage());
        }
        return str;
    }

    /*
     * Instance methods
     */
    protected abstract Collection<Assertion> getAssertions(String requestPath, XSPARole role);

    protected SOAPElement testGood(String testName, String request) {

        SOAPElement result = null;

        try {
            result = callService(request);  // call

        } catch (RuntimeException ex) {
            LOGGER.info(fail(testName));// preaty status print to tests list
            LOGGER.info(ex.getMessage(), ex);//must have stack trace for troubleshooting failed integration tests
            Assert.fail(testName + ": " + ex.getMessage());             // fail the test
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(success(testName)); // pretty status print to tests list
        }
        return result;
    }

    protected SOAPElement callService(String request) throws SOAPFaultException {

        Document doc = readDoc(request);                            // read soap request
        SimpleSoapClient client = new SimpleSoapClient(epr);        // SOAP client
        return client.call(doc, assertions);                    // Call service

    }
}
