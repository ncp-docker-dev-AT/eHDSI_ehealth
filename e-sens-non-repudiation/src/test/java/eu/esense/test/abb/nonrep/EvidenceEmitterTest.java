package eu.esense.test.abb.nonrep;

import eu.esens.abb.nonrep.*;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.api.UnorderedPolicyRepository;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.herasaf.xacml.core.utils.JAXBMarshallerConfiguration;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore("Test to revise - Exclude unit tests from test execution")
public class EvidenceEmitterTest {

    private static final String DATATYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
    private static final String DATATYPE_DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    private static final String IHE_ITI_XCA_RETRIEVE = "urn:ihe:iti:2007:CrossGatewayRetrieve";
    private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceEmitterTest.class);
    private static PDP simplePDP;
    private static X509Certificate cert;
    private static PrivateKey key;

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // The Policy Repository is here because an adHoc implementation can have more requirements than the default
        // from HERESAF, if needed.
        simplePDP = SimplePDPFactory.getSimplePDP();
        UnorderedPolicyRepository polrep = (UnorderedPolicyRepository) simplePDP.getPolicyRepository();

        // Populate the policy repository
        Document policy = readMessage("src/test/testData/samplePolicy.xml");
        polrep.deploy(PolicyMarshaller.unmarshal(policy));

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/test/testData/s1.keystore"), "spirit".toCharArray());
        cert = (X509Certificate) ks.getCertificate("server1");
        key = (PrivateKey) ks.getKey("server1", "spirit".toCharArray());
        org.apache.xml.security.Init.init();
    }

    /**
     * @param file
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static Document readMessage(String file) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(file));
    }

    /**
     * This test reads a sample message from the eHealth domain (XCA) and will issue an ATNA specific audit trail.
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     */
    public void testGenerateATNA() throws ParserConfigurationException, SAXException, IOException, MalformedIHESOAPException,
            URISyntaxException, TOElementException, EnforcePolicyException, ObligationDischargeException,
            TransformerException {

        testGenerateAtna();
    }

    /**
     * I had to add this method because I need the message, but Junit does not like non-void test methods.
     *
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     * @throws SOAPException
     */
    public Document testGenerateAtna() throws ParserConfigurationException, SAXException, IOException,
            MalformedIHESOAPException, URISyntaxException, TOElementException, EnforcePolicyException,
            ObligationDischargeException, TransformerException {

        // AlternativeUserID = IP of the machine
        // NetworkAccessPointID = IP of the machine
        // UserName = user from the SAML assertion
        // NetworkAccessPointID = IP of the remote
        // UserId = Subject of the assertion UserName = subject-id
        // ParticipantObjectID = xdsb:RepositoryUniqueId ParticipantObjectID = xdsb:DocumentUniqueId

        // The flow is as follows (imagine that the PEP is a facade in front of the Corner).
        // The message is inspected, the relevant information is retrieved and placed into the XACML request.
        // The PDP evaluates the request and returns the pointer of the obligation handler.

        // Read the message as it arrives at the facade
        Document incomingMsg = readMessage("src/test/testData/audit.xml");

        // Instantiate the message inspector, to see which type of message it is.
        MessageInspector messageInspector = new MessageInspector(incomingMsg);
        MessageType messageType = messageInspector.getMessageType();
        assertNotNull(messageType);

        // IHE Mock message.
        checkCorrectnessOfIHEXCA(messageType);

        // Now create the XACML request
        LinkedList<XACMLAttributes> actionList = new LinkedList<>();
        XACMLAttributes action = new XACMLAttributes();
        action.setDataType(new URI(DATATYPE_STRING));
        action.setIdentifier(new URI("urn:oasis:names:tc:xacml:1.0:action:action-id"));
        actionList.add(action);
        action.setValue(messageType instanceof IHEXCARetrieve ? IHE_ITI_XCA_RETRIEVE : "UNKNOWN");

        LinkedList<XACMLAttributes> environmentList = new LinkedList<>();
        XACMLAttributes environment = new XACMLAttributes();
        environment.setDataType(new URI(DATATYPE_DATETIME));
        environment.setIdentifier(new URI("urn:esens:2014:event"));
        environment.setValue(new DateTime().toString());
        environmentList.add(environment);

        XACMLRequestCreator requestCreator = new XACMLRequestCreator(messageType, null, null,
                actionList, environmentList);

        Element request = requestCreator.getRequest();
        assertNotNull(request);

        // Logger
        Utilities.serialize(request);

        // Call the XACML engine: The policy has been deployed in the setupBeforeClass.
        EnforcePolicy enforcePolicy = new EnforcePolicy(simplePDP);

        enforcePolicy.decide(request);
        assertNotNull(enforcePolicy.getResponseAsDocument());
        assertNotNull(enforcePolicy.getResponseAsObject());
        Utilities.serialize(enforcePolicy.getResponseAsDocument().getDocumentElement());

        List<ESensObligation> obligations = enforcePolicy.getObligationList();
        assertNotNull(obligations);

        Context context = new Context();
        context.setIncomingMsg(incomingMsg);
        // Here I pass the XML in order to give to the developers the possibility to use their own implementation.
        // Although an object is easier to get the relevant types (e.g., action environment).
        context.setRequest(request);
        context.setEnforcer(enforcePolicy);
        context.setUsername("demo2");
        context.setCurrentHost("127.0.0.1");
        context.setRemoteHost("192.168.10.1");

        ObligationHandlerFactory handlerFactory = ObligationHandlerFactory.getInstance();
        List<ObligationHandler> handlers = handlerFactory.createHandler(messageType, obligations, context);

        // Manual discharge. This behavior is to let free an implementation to still decide which handler to trigger.
        handlers.get(0).discharge();
        handlers.get(1).discharge();

        // Give me the ATNA, it's an ATNA test
        assertNotNull(handlers.get(0).getMessage());
        Utilities.serialize(handlers.get(0).getMessage().getDocumentElement());

        // Return handler.getMessage() which will be the audit, then go to the server and validated by another wrapper.
        return handlers.get(0).getMessage();
    }

    @Test
    public void testGenerateRemNRO() throws ParserConfigurationException, SAXException, IOException, MalformedIHESOAPException,
            URISyntaxException, TOElementException, EnforcePolicyException, ObligationDischargeException, SOAPException,
            TransformerException {

        testGenerateREMNRO();
    }

    /**
     * This method issue a REM NRO evidence
     *
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     * @throws SOAPException
     * @throws MalformedMIMEMessageException
     */
    public Document testGenerateREMNRO() throws ParserConfigurationException, SAXException, IOException, MalformedIHESOAPException,
            URISyntaxException, TOElementException, EnforcePolicyException, ObligationDischargeException, SOAPException,
            TransformerException {

        /*
         * The flow is as follows (imagine that the PEP is a facade in front of the Corner).
         * The message is inspected, the relevant information is retrieved and placed into the XACML request.
         * The PDP evaluates the request and returns the pointer of the obligation handler.
         */
        // Read the message as it arrives at the facade
        Document incomingMsg = readMessage("src/test/testData/incomingMsg.xml");
        SOAPMessage message = Utilities.toSoap(incomingMsg, null);

        /*
         * Instantiate the message inspector, to see which type of message is
         */
        MessageInspector messageInspector = new MessageInspector(message);
        MessageType messageType = messageInspector.getMessageType();
        assertNotNull(messageType);
        assertNotNull(messageInspector.getMessageUUID());
        assertEquals("uuid:C3F5A03D-1A0C-4F62-ADC7-F3C007CD50CF", messageInspector.getMessageUUID());

        // IHE Mock message.
        checkCorrectnessOfIHEXCA(messageType);

        // Now create the XACML request
        LinkedList<XACMLAttributes> actionList = new LinkedList<>();
        XACMLAttributes action = new XACMLAttributes();
        action.setDataType(new URI(DATATYPE_STRING));
        action.setIdentifier(new URI("urn:eSENS:outcome"));
        actionList.add(action);

        // Here I imagine a table lookup or similar
        action.setValue("success");

        LinkedList<XACMLAttributes> environmentList = new LinkedList<>();
        XACMLAttributes environment = new XACMLAttributes();
        environment.setDataType(new URI(DATATYPE_DATETIME));
        environment.setIdentifier(new URI("urn:esens:2014:event"));
        environment.setValue(new DateTime().toString());
        environmentList.add(environment);

        XACMLRequestCreator requestCreator = new XACMLRequestCreator(
                messageType, null, null, actionList, environmentList);

        Element request = requestCreator.getRequest();
        assertNotNull(request);

        // Logger
        Utilities.serialize(request);

        // Call the XACML engine: The policy has been deployed in the setupBeforeClass.
        EnforcePolicy enforcePolicy = new EnforcePolicy(simplePDP);

        enforcePolicy.decide(request);
        assertNotNull(enforcePolicy.getResponseAsDocument());
        assertNotNull(enforcePolicy.getResponseAsObject());
        Utilities.serialize(enforcePolicy.getResponseAsDocument().getDocumentElement());

        List<ESensObligation> obligations = enforcePolicy.getObligationList();
        assertNotNull(obligations);

        Context context = new Context();
        context.setIncomingMsg(message);
        context.setIssuerCertificate(cert);
        context.setSenderCertificate(cert);
        context.setRecipientCertificate(cert);
        context.setSigningKey(key);
        context.setSubmissionTime(new DateTime());
        context.setEvent("epSOS-31");

        context.setMessageUUID(messageInspector.getMessageUUID());
        context.setAuthenticationMethod("http://uri.etsi.org/REM/AuthMethod#Strong");
        // Here I pass the XML in order to give to the developers the possibility to use their own implementation.
        // Although an object is easier to get the relevant types (e.g., action environment).
        context.setRequest(request);
        context.setEnforcer(enforcePolicy);
        context.setUsername("demo2");
        context.setCurrentHost("127.0.0.1");
        context.setRemoteHost("192.168.10.1");

        ObligationHandlerFactory handlerFactory = ObligationHandlerFactory.getInstance();
        List<ObligationHandler> handlers = handlerFactory.createHandler(messageType, obligations, context);

        // Manual discharge. This behavior is to let free an implementation to still decide which handler to trigger.
        handlers.get(0).discharge();

        // Give me the ATNA, it's an ATNA test
        assertNotNull(handlers.get(0).getMessage());
        Utilities.serialize(handlers.get(0).getMessage().getDocumentElement());

        // Return handler.getMessage() which will be the audit, then go to the server and validated by another wrapper.
        return handlers.get(0).getMessage();
    }

    @Test
    public void testGenerateRemNRR() throws ParserConfigurationException, SAXException, IOException, MalformedIHESOAPException,
            URISyntaxException, TOElementException, EnforcePolicyException, ObligationDischargeException, SOAPException,
            SyntaxException, TransformerException {

        testGenerateREMNRR();
    }

    /**
     * This method issue a REM NRO evidence
     *
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     * @throws SOAPException
     * @throws SyntaxException
     */
    public Document testGenerateREMNRR() throws ParserConfigurationException, SAXException, IOException,
            MalformedIHESOAPException, URISyntaxException, TOElementException, EnforcePolicyException,
            ObligationDischargeException, SOAPException, SyntaxException, TransformerException {

        /*
         * The flow is as follows (imagine that the PEP is a facade in front of the Corner).
         * The message is inspected, the relevant information is retrieved and placed into the XACML request.
         * The PDP evaluates the request and returns the pointer of the obligation handler.
         */
        simplePDP = SimplePDPFactory.getSimplePDP();
        UnorderedPolicyRepository polrep = (UnorderedPolicyRepository) simplePDP.getPolicyRepository();

        JAXBMarshallerConfiguration conf = new JAXBMarshallerConfiguration();
        conf.setValidateParsing(false);
        conf.setValidateWriting(false);
        PolicyMarshaller.setJAXBMarshallerConfiguration(conf);

        // Populate the policy repository
        Document policy = readMessage("src/test/testData/samplePolicyNRR.xml");
        polrep.deploy(PolicyMarshaller.unmarshal(policy));


        // Read the message as it arrives at the facade
        Document incomingMsg = readMessage("src/test/testData/incomingMsg.xml");

        SOAPMessage message = Utilities.toSoap(incomingMsg, null);

        /*
         * Instantiate the message inspector, to see which type of message is
         */
        MessageInspector messageInspector = new MessageInspector(message);
        MessageType messageType = messageInspector.getMessageType();
        assertNotNull(messageType);

        // IHE Mock message.
        // checkCorrectnessOfIHEXCA(messageType);

        // Now create the XACML request
        LinkedList<XACMLAttributes> actionList = new LinkedList<>();
        XACMLAttributes action = new XACMLAttributes();
        action.setDataType(new URI(DATATYPE_STRING));
        action.setIdentifier(new URI("urn:eSENS:outcome"));
        actionList.add(action);

        // Here I imagine a table lookup or similar
        action.setValue("success");

        LinkedList<XACMLAttributes> environmentList = new LinkedList<>();
        XACMLAttributes environment = new XACMLAttributes();
        environment.setDataType(new URI(DATATYPE_DATETIME));
        environment.setIdentifier(new URI("urn:esens:2014:event"));
        environment.setValue(new DateTime().toString());
        environmentList.add(environment);

        XACMLRequestCreator requestCreator = new XACMLRequestCreator(
                messageType, null, null, actionList, environmentList);

        Element request = requestCreator.getRequest();
        assertNotNull(request);

        // Logger
        Utilities.serialize(request);

        // Call the XACML engine: The policy has been deployed in the setupBeforeClass.
        EnforcePolicy enforcePolicy = new EnforcePolicy(simplePDP);

        enforcePolicy.decide(request);
        assertNotNull(enforcePolicy.getResponseAsDocument());
        assertNotNull(enforcePolicy.getResponseAsObject());
        Utilities.serialize(enforcePolicy.getResponseAsDocument().getDocumentElement());

        List<ESensObligation> obligations = enforcePolicy.getObligationList();
        assertNotNull(obligations);

        Context context = new Context();
        context.setIncomingMsg(incomingMsg);
        context.setIssuerCertificate(cert);
        context.setSenderCertificate(cert);
        context.setRecipientCertificate(cert);
        context.setSigningKey(key);
        context.setSubmissionTime(new DateTime());
        context.setEvent("epSOS-31");
        context.setMessageUUID(messageInspector.getMessageUUID());
        context.setAuthenticationMethod("http://uri.etsi.org/REM/AuthMethod#Strong");
        // Here I pass the XML in order to give to the developers the possibility to use their own implementation.
        // Although an object is easier to get the relevant types (e.g., action environment).
        context.setRequest(request);
        context.setEnforcer(enforcePolicy);

        ObligationHandlerFactory handlerFactory = ObligationHandlerFactory
                .getInstance();
        List<ObligationHandler> handlers = handlerFactory.createHandler(
                messageType, obligations, context);

        // Manual discharge. This behavior is to let free an implementation to still decide which handler to trigger
        LOGGER.info(handlers.get(0).getClass().getName());

        handlers.get(0).discharge();

        // Give me the ATNA, it's an ATNA test
        assertNotNull(handlers.get(0).getMessage());
        Utilities.serialize(handlers.get(0).getMessage().getDocumentElement());

        // Return handler.getMessage() which will be the audit, then go to the server and validated by another wrapper.
        return handlers.get(0).getMessage();
    }

    /**
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     * @throws SOAPException
     * @throws SyntaxException
     * @throws TransformerException
     */
    @Test
    public void testGenerateRemNRD() throws ParserConfigurationException, SAXException, IOException,
            MalformedIHESOAPException, URISyntaxException, TOElementException, EnforcePolicyException,
            ObligationDischargeException, SOAPException, SyntaxException, TransformerException {

        testGenerateREMNRD();
    }

    /**
     * This method issue a REM NRD evidence
     *
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedIHESOAPException
     * @throws URISyntaxException
     * @throws TOElementException
     * @throws EnforcePolicyException
     * @throws ObligationDischargeException
     * @throws SOAPException
     * @throws SyntaxException
     */
    public Document testGenerateREMNRD() throws ParserConfigurationException, SAXException, IOException,
            MalformedIHESOAPException, URISyntaxException, TOElementException, EnforcePolicyException,
            ObligationDischargeException, SOAPException, SyntaxException, TransformerException {

        /*
         * The flow is as follows (imagine that the PEP is a facade in front of the Corner).
         * The message is inspected, the relevant information is retrieved and placed into the XACML request.
         * The PDP evaluates the request and returns the pointer of the obligation handler.
         */
        simplePDP = SimplePDPFactory.getSimplePDP();
        UnorderedPolicyRepository polrep = (UnorderedPolicyRepository) simplePDP.getPolicyRepository();

        JAXBMarshallerConfiguration conf = new JAXBMarshallerConfiguration();
        conf.setValidateParsing(false);
        conf.setValidateWriting(false);
        PolicyMarshaller.setJAXBMarshallerConfiguration(conf);

        // Populate the policy repository
        Document policy = readMessage("src/test/testData/samplePolicyNRD.xml");

        polrep.deploy(PolicyMarshaller.unmarshal(policy));

        // Read the message as it arrives at the facade
        Document incomingMsg = readMessage("src/test/testData/incomingMsg.xml");

        SOAPMessage message = Utilities.toSoap(incomingMsg, null);

        /*
         * Instantiate the message inspector, to see which type of message is
         */
        MessageInspector messageInspector = new MessageInspector(message);
        MessageType messageType = messageInspector.getMessageType();
        assertNotNull(messageType);

        // IHE Mock message.
        // checkCorrectnessOfIHEXCA(messageType);

        // Now create the XACML request
        LinkedList<XACMLAttributes> actionList = new LinkedList<>();
        XACMLAttributes action = new XACMLAttributes();
        action.setDataType(new URI(DATATYPE_STRING));
        action.setIdentifier(new URI("urn:eSENS:outcome"));
        actionList.add(action);

        // Here I imagine a table lookup or similar
        action.setValue("success");

        LinkedList<XACMLAttributes> environmentList = new LinkedList<>();
        XACMLAttributes environment = new XACMLAttributes();
        environment.setDataType(new URI(DATATYPE_DATETIME));
        environment.setIdentifier(new URI("urn:esens:2014:event"));
        environment.setValue(new DateTime().toString());
        environmentList.add(environment);

        XACMLRequestCreator requestCreator = new XACMLRequestCreator(
                messageType, null, null, actionList, environmentList);

        Element request = requestCreator.getRequest();
        assertNotNull(request);

        // Logger
        Utilities.serialize(request);

        // Call the XACML engine: The policy has been deployed in the setupBeforeClass.
        EnforcePolicy enforcePolicy = new EnforcePolicy(simplePDP);

        enforcePolicy.decide(request);
        assertNotNull(enforcePolicy.getResponseAsDocument());
        assertNotNull(enforcePolicy.getResponseAsObject());
        Utilities.serialize(enforcePolicy.getResponseAsDocument().getDocumentElement());

        List<ESensObligation> obligations = enforcePolicy.getObligationList();
        assertNotNull(obligations);

        Context context = new Context();
        context.setIncomingMsg(incomingMsg);
        context.setIssuerCertificate(cert);

        // Justice domain has them optional
        context.setSenderCertificate(cert);
        context.setRecipientCertificate(cert);
        context.setSigningKey(key);
        context.setSubmissionTime(new DateTime());
        // TODO: change to setEventCode
        context.setEvent("epSOS-31");
        context.setMessageUUID(messageInspector.getMessageUUID());
        context.setAuthenticationMethod("http://uri.etsi.org/REM/AuthMethod#Strong");
        // Here I pass the XML in order to give to the developers the possibility to use their own implementation.
        // Although an object is easier to get the relevant types (e.g., action environment).
        context.setRequest(request);
        context.setEnforcer(enforcePolicy);
        LinkedList<String> namesPostalAddress = new LinkedList<>();
        namesPostalAddress.add("Test");
        namesPostalAddress.add("Test2");

        context.setRecipientNamePostalAddress(namesPostalAddress);

        context.setRecipientNamePostalAddress(namesPostalAddress);
        LinkedList<String> senderNamesPostalAddress = new LinkedList<>();
        senderNamesPostalAddress.add("SenderTest");
        senderNamesPostalAddress.add("SenderTest2");
        context.setSenderNamePostalAddress(senderNamesPostalAddress);
        ObligationHandlerFactory handlerFactory = ObligationHandlerFactory.getInstance();
        List<ObligationHandler> handlers = handlerFactory.createHandler(messageType, obligations, context);

        // Manual discharge. This behavior is to let free an implementation to still decide which handler to trigger
        LOGGER.info(handlers.get(0).getClass().getName());

        handlers.get(0).discharge();

        // Give me the ATNA, it's an ATNA test
        assertNotNull(handlers.get(0).getMessage());
        Utilities.serialize(handlers.get(0).getMessage().getDocumentElement());

        // Return handler.getMessage() which will be the audit, then go to the server and validated by another wrapper.
        return handlers.get(0).getMessage();
    }

    /**
     * @param messageType
     */
    private void checkCorrectnessOfIHEXCA(final MessageType messageType) {

        assertTrue(messageType instanceof IHEXCARetrieve);

        IHEXCARetrieve xca = (IHEXCARetrieve) messageType;
        assertNotNull(xca.getDocumentUniqueId());
        assertNotNull(xca.getHomeCommunityID());
        assertNotNull(xca.getRepositoryUniqueId());

        assertEquals("urn:oid:2.16.840.1.113883.3.42.10001.100001.19", xca.getHomeCommunityID());
        assertEquals("2.16.840.1.113883.3.333.1", xca.getRepositoryUniqueId());
        assertEquals("4eb38f09-78da-43a8-a5b4-92b115c74add", xca.getDocumentUniqueId());
    }
}
