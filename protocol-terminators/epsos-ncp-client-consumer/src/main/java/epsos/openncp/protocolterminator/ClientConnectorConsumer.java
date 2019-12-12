package epsos.openncp.protocolterminator;

import epsos.openncp.protocolterminator.clientconnector.*;
import epsos.openncp.pt.client.ClientConnectorServiceStub;
import eu.europa.ec.sante.ehdsi.openncp.evidence.utils.OutFlowEvidenceEmitterHandler;
import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/*
 *  ClientConnectorConsumer
 */
public class ClientConnectorConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectorConsumer.class.getName());

    // Default timeout set to Three minutes.
    private static final long TIMEOUT = 180000;

    private static final String EXCEPTION_FORMATTER = "{}: {}";

    private String epr;

    public ClientConnectorConsumer(String epr) {

        this.epr = epr;
    }

    private static void addAssertions(ClientConnectorServiceStub stub, Assertion idAssertion, Assertion trcAssertion)
            throws Exception {

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement omSecurityElement = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "Security", "wsse"), null);
        if (trcAssertion != null) {
            omSecurityElement.addChild(XMLUtils.toOM(trcAssertion.getDOM()));
        }
        omSecurityElement.addChild(XMLUtils.toOM(idAssertion.getDOM()));
        stub._getServiceClient().addHeader(omSecurityElement);
    }

    private void registerEvidenceEmitterHandler(ClientConnectorServiceStub stub) {

        // Adding custom phase for evidence emitter processing.
        LOGGER.debug("Adding custom phase for outflow evidence emitter processing");
        HandlerDescription outFlowHandlerDescription = new HandlerDescription("OutFlowEvidenceEmitterHandler");
        outFlowHandlerDescription.setHandler(new OutFlowEvidenceEmitterHandler());
        AxisConfiguration axisConfiguration = stub._getServiceClient().getServiceContext().getConfigurationContext().getAxisConfiguration();
        List<Phase> outFlowPhasesList = axisConfiguration.getOutFlowPhases();
        Phase outFlowEvidenceEmitterPhase = new Phase("OutFlowEvidenceEmitterPhase");
        try {
            outFlowEvidenceEmitterPhase.addHandler(outFlowHandlerDescription);
        } catch (PhaseException ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
        }
        outFlowPhasesList.add(outFlowEvidenceEmitterPhase);
        LOGGER.debug("Resetting global Out phases");
        axisConfiguration.setGlobalOutPhase(outFlowPhasesList);
        LOGGER.debug("Ended phases restructuring");
    }

    public List<EpsosDocument1> queryDocuments(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                               PatientId patientId, GenericDocumentCode classCode) {

        LOGGER.info("[Portal]: queryDocuments(countryCode:'{}', patientId:'{}')", countryCode, patientId.getRoot());
        ClientConnectorServiceStub stub = initializeServiceStub();

        try {
            addAssertions(stub, idAssertion, trcAssertion);

            QueryDocumentsDocument queryDocumentsDocument = QueryDocumentsDocument.Factory.newInstance();
            QueryDocuments queryDocuments = queryDocumentsDocument.addNewQueryDocuments();
            QueryDocumentRequest queryDocumentRequest = queryDocuments.addNewArg0();
            queryDocumentRequest.setClassCode(classCode);
            queryDocumentRequest.setPatientId(patientId);
            queryDocumentRequest.setCountryCode(countryCode);

            QueryDocumentsResponseDocument queryDocumentsResponseDocument;
            queryDocumentsResponseDocument = stub.queryDocuments(queryDocumentsDocument);
            EpsosDocument1[] docArray = queryDocumentsResponseDocument.getQueryDocumentsResponse().getReturnArray();

            return Arrays.asList(docArray);
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    public List<PatientDemographics> queryPatient(Assertion idAssertion, String countryCode, PatientDemographics patientDemographics) {

        LOGGER.info("[Portal]: queryPatient(countryCode:'{}')", countryCode);
        ClientConnectorServiceStub stub = initializeServiceStub();
//        OMFactory factory = OMAbstractFactory.getOMFactory();
//        OMNamespace ns2 = factory.createOMNamespace(AddressingConstants.Final.WSA_NAMESPACE, "");
//
//        SOAPHeaderBlock action = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock(AddressingConstants.WSA_ACTION, ns2);
//        OMNode node = factory.createOMText("urn:ehdsi:queryPatient");
//        action.addChild(node);
//        // OMAttribute att2 = factory.createOMAttribute("mustUnderstand", ns2, "1");
//        // action.addAttribute(att2);
//
//        SOAPHeaderBlock headerBlock = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock(AddressingConstants.WSA_MESSAGE_ID, ns2);
//        OMNode messageIdNode = factory.createOMText(Constants.UUID_PREFIX + UUID.randomUUID().toString());
//        headerBlock.addChild(messageIdNode);
//
//        SOAPHeaderBlock to = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock(AddressingConstants.WSA_TO, ns2);
//        OMNode node3 = factory.createOMText(epr);
//        to.addChild(node3);
//
//        stub._getServiceClient().addHeader(action);
//        stub._getServiceClient().addHeader(headerBlock);
//        stub._getServiceClient().addHeader(to);

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(stub, idAssertion, null);
            QueryPatientRequest queryPatientRequest = QueryPatientRequest.Factory.newInstance();

            queryPatientRequest.setPatientDemographics(patientDemographics);
            queryPatientRequest.setCountryCode(countryCode);

            QueryPatientDocument queryPatientDocument = QueryPatientDocument.Factory.newInstance();
            queryPatientDocument.addNewQueryPatient().setArg0(queryPatientRequest);

            QueryPatientResponseDocument queryPatientResponseDocument = stub.queryPatient(queryPatientDocument);
            PatientDemographics[] pdArray = queryPatientResponseDocument.getQueryPatientResponse().getReturnArray();
            return Arrays.asList(pdArray);
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Default Webservice test method available mainly for configuration purpose.
     */
    public String sayHello(Assertion idAssertion, String name) {

        LOGGER.info("[Portal]: sayHello(name:'{}')", name);
        ClientConnectorServiceStub stub = initializeServiceStub();
        try {
            addAssertions(stub, idAssertion, null);
            SayHelloDocument sayHelloDocument = SayHelloDocument.Factory.newInstance();
            sayHelloDocument.addNewSayHello().setArg0(name);

            SayHelloResponseDocument sayHelloResponseDocument = stub.sayHello(sayHelloDocument);
            return sayHelloResponseDocument.getSayHelloResponse().getReturn();
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    public EpsosDocument1 retrieveDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode, String targetLanguage) {

        LOGGER.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}', targetLanguage:'{}')", countryCode, homeCommunityId, targetLanguage);
        ClientConnectorServiceStub clientConnectorStub = initializeServiceStub();

        try {

            addAssertions(clientConnectorStub, idAssertion, trcAssertion);
            RetrieveDocumentDocument1 retrieveDocumentDocument = RetrieveDocumentDocument1.Factory.newInstance();
            RetrieveDocument1 retrieveDocument = retrieveDocumentDocument.addNewRetrieveDocument();

            RetrieveDocumentRequest retrieveDocumentRequest = retrieveDocument.addNewArg0();
            retrieveDocumentRequest.setDocumentId(documentId);
            retrieveDocumentRequest.setHomeCommunityId(homeCommunityId);
            retrieveDocumentRequest.setCountryCode(countryCode);
            retrieveDocumentRequest.setClassCode(classCode);
            retrieveDocumentRequest.setTargetLanguage(targetLanguage);

            RetrieveDocumentResponseDocument retrieveDocumentResponseDocument = clientConnectorStub.retrieveDocument(retrieveDocumentDocument);

            return retrieveDocumentResponseDocument.getRetrieveDocumentResponse().getReturn();
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated Method has been deprecated in favor of the implementation of retrieveDocument() with language parameter.
     */
    @Deprecated
    public EpsosDocument1 retrieveDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode) {

        LOGGER.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}')", countryCode, homeCommunityId);
        return retrieveDocument(idAssertion, trcAssertion, countryCode, documentId, homeCommunityId, classCode, null);
    }

    public SubmitDocumentResponse submitDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                                 EpsosDocument1 document, PatientDemographics patientDemographics) {

        LOGGER.info("[Portal]: submitDocument(countryCode:'{}')", countryCode);
        ClientConnectorServiceStub stub = initializeServiceStub();

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(stub, idAssertion, trcAssertion);
            SubmitDocumentDocument1 submitDocumentDoc = SubmitDocumentDocument1.Factory.newInstance();
            SubmitDocument1 submitDocument = SubmitDocument1.Factory.newInstance();
            SubmitDocumentRequest submitDocRequest = SubmitDocumentRequest.Factory.newInstance();

            submitDocRequest.setPatientDemographics(patientDemographics);
            submitDocRequest.setDocument(document);
            submitDocRequest.setCountryCode(countryCode);
            submitDocument.setArg0(submitDocRequest);
            submitDocumentDoc.setSubmitDocument(submitDocument);

            return stub.submitDocument(submitDocumentDoc).getSubmitDocumentResponse();
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_FORMATTER, ex.getClass(), ex.getMessage(), ex);
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Initializing the ClientConnectorService client stubs to contact WSDL.
     *
     * @return Initialized ClientConnectorServiceStub set to the configured EPR and the SOAP version.
     */
    private ClientConnectorServiceStub initializeServiceStub() {

        try {
            LOGGER.debug("Initializing Client Connector Stub Services");
            ClientConnectorServiceStub clientConnectorStub = new ClientConnectorServiceStub(epr);
            clientConnectorStub._getServiceClient().getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            clientConnectorStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(TIMEOUT);
            this.registerEvidenceEmitterHandler(clientConnectorStub);
            return clientConnectorStub;
        } catch (AxisFault ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Trims the Patient Demographics sent by the client and received by the Client Connector.
     *
     * @param patientDemographics Identity Traits to be trimmed and provided by the by the client
     */
    private void trimPatientDemographics(PatientDemographics patientDemographics) {

        // Iterate over the Patient Ids
        for (PatientId patientId : patientDemographics.getPatientIdArray()) {

            patientId.setExtension(StringUtils.trim(patientId.getExtension()));
            patientId.setRoot(StringUtils.trim(patientId.getRoot()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getAdministrativeGender())) {
            patientDemographics.setAdministrativeGender(StringUtils.trim(patientDemographics.getAdministrativeGender()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getFamilyName())) {
            patientDemographics.setFamilyName(StringUtils.trim(patientDemographics.getFamilyName()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getGivenName())) {
            patientDemographics.setGivenName(StringUtils.trim(patientDemographics.getGivenName()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getEmail())) {
            patientDemographics.setEmail(StringUtils.trim(patientDemographics.getEmail()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getTelephone())) {
            patientDemographics.setTelephone(StringUtils.trim(patientDemographics.getTelephone()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getStreetAddress())) {
            patientDemographics.setStreetAddress(StringUtils.trim(patientDemographics.getStreetAddress()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getPostalCode())) {
            patientDemographics.setPostalCode(StringUtils.trim(patientDemographics.getPostalCode()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getCity())) {
            patientDemographics.setCity(StringUtils.trim(patientDemographics.getCity()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getCountry())) {
            patientDemographics.setCountry(StringUtils.trim(patientDemographics.getCountry()));
        }
    }
}
