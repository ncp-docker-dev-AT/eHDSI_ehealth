package epsos.openncp.protocolterminator;

import epsos.openncp.protocolterminator.clientconnector.*;
import epsos.openncp.pt.client.ClientConnectorServiceStub;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionHelper;
import eu.europa.ec.sante.ehdsi.openncp.evidence.utils.OutFlowEvidenceEmitterHandler;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 *  ClientConnectorConsumer
 */
public class ClientConnectorConsumer {

    // Default timeout set to Three minutes.
    private static final long TIMEOUT = 180000;
    private final Logger logger = LoggerFactory.getLogger(ClientConnectorConsumer.class);
    private final String endpointReference;

    public ClientConnectorConsumer(String endpointReference) {

        this.endpointReference = endpointReference;
    }

    private static void addAssertions(ClientConnectorServiceStub stub, Assertion idAssertion, Assertion trcAssertion)
            throws Exception {

        if (AssertionHelper.isExpired(idAssertion)) {
            throw new ClientConnectorConsumerException("HCP Assertion expired");
        }
        var omFactory = OMAbstractFactory.getOMFactory();
        OMElement omSecurityElement = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "Security", "wsse"), null);
        if (trcAssertion != null) {
            if (AssertionHelper.isExpired(trcAssertion)) {
                throw new ClientConnectorConsumerException("TRC Assertion expired");
            }
            omSecurityElement.addChild(XMLUtils.toOM(trcAssertion.getDOM()));
        }
        omSecurityElement.addChild(XMLUtils.toOM(idAssertion.getDOM()));
        stub._getServiceClient().addHeader(omSecurityElement);
    }

    private void registerEvidenceEmitterHandler(ClientConnectorServiceStub stub) {

        // Adding custom phase for evidence emitter processing.
        logger.debug("Adding custom phase for outflow evidence emitter processing");
        var outFlowHandlerDescription = new HandlerDescription("OutFlowEvidenceEmitterHandler");
        outFlowHandlerDescription.setHandler(new OutFlowEvidenceEmitterHandler());
        var axisConfiguration = stub._getServiceClient().getServiceContext().getConfigurationContext().getAxisConfiguration();
        List<Phase> outFlowPhasesList = axisConfiguration.getOutFlowPhases();
        var outFlowEvidenceEmitterPhase = new Phase("OutFlowEvidenceEmitterPhase");
        try {
            outFlowEvidenceEmitterPhase.addHandler(outFlowHandlerDescription);
        } catch (PhaseException ex) {
            logger.error("PhaseException: '{}'", ex.getMessage(), ex);
        }
        outFlowPhasesList.add(outFlowEvidenceEmitterPhase);
        logger.debug("Resetting global Out phases");
        axisConfiguration.setGlobalOutPhase(outFlowPhasesList);
        logger.debug("Ended phases restructuring");
    }

    public List<EpsosDocument1> queryDocuments(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                               PatientId patientId, List<GenericDocumentCode> classCodes) {

        logger.info("[Portal]: queryDocuments(countryCode:'{}', patientId:'{}')", countryCode, patientId.getRoot());
        ClientConnectorServiceStub stub = initializeServiceStub();

        try {
            addAssertions(stub, idAssertion, trcAssertion);

            var queryDocumentsDocument = QueryDocumentsDocument.Factory.newInstance();
            var queryDocuments = queryDocumentsDocument.addNewQueryDocuments();
            var queryDocumentRequest = queryDocuments.addNewArg0();
            GenericDocumentCode[] array = new GenericDocumentCode[classCodes.size()];
            for(int i = 0; i < classCodes.size(); i++) {
                array[i] = classCodes.get(i);
            }
            queryDocumentRequest.setClassCodeArray(array);
//            queryDocumentRequest.setClassCodeArray(classCodes.toArray(GenericDocumentCode[]::new));
            queryDocumentRequest.setPatientId(patientId);
            queryDocumentRequest.setCountryCode(countryCode);

            QueryDocumentsResponseDocument queryDocumentsResponseDocument;
            queryDocumentsResponseDocument = stub.queryDocuments(queryDocumentsDocument);
            EpsosDocument1[] docArray = queryDocumentsResponseDocument.getQueryDocumentsResponse().getReturnArray();

            return Arrays.asList(docArray);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * @param idAssertion         - HCP assertions
     * @param countryCode         - Country of treatment
     * @param patientDemographics - Identifiers of the requested patient
     * @return List of patients found (only 1 patient is expected in eHDSI)
     */
    public List<PatientDemographics> queryPatient(Assertion idAssertion, String countryCode, PatientDemographics patientDemographics) {

        logger.info("[Portal]: queryPatient(countryCode:'{}')", countryCode);
        ClientConnectorServiceStub stub = initializeServiceStub();

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(stub, idAssertion, null);
            var queryPatientRequest = QueryPatientRequest.Factory.newInstance();
            queryPatientRequest.setPatientDemographics(patientDemographics);
            queryPatientRequest.setCountryCode(countryCode);

            var queryPatientDocument = QueryPatientDocument.Factory.newInstance();
            queryPatientDocument.addNewQueryPatient().setArg0(queryPatientRequest);

            var queryPatientResponseDocument = stub.queryPatient(queryPatientDocument);
            PatientDemographics[] pdArray = queryPatientResponseDocument.getQueryPatientResponse().getReturnArray();
            return Arrays.asList(pdArray);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Default Webservice test method available mainly for configuration purpose.
     */
    public String sayHello(Assertion idAssertion, String name) {

        logger.info("[Portal]: sayHello(name:'{}')", name);
        ClientConnectorServiceStub stub = initializeServiceStub();
        try {
            addAssertions(stub, idAssertion, null);
            var sayHelloDocument = SayHelloDocument.Factory.newInstance();
            sayHelloDocument.addNewSayHello().setArg0(name);

            var sayHelloResponseDocument = stub.sayHello(sayHelloDocument);
            return sayHelloResponseDocument.getSayHelloResponse().getReturn();
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    public EpsosDocument1 retrieveDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode, DocumentId documentId,
                                           String homeCommunityId, GenericDocumentCode classCode, String targetLanguage) {

        logger.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}', targetLanguage:'{}')",
                countryCode, homeCommunityId, targetLanguage);
        ClientConnectorServiceStub clientConnectorStub = initializeServiceStub();

        try {
            addAssertions(clientConnectorStub, idAssertion, trcAssertion);
            RetrieveDocumentDocument1 retrieveDocumentDocument = RetrieveDocumentDocument1.Factory.newInstance();
            RetrieveDocument1 retrieveDocument = retrieveDocumentDocument.addNewRetrieveDocument();

            var retrieveDocumentRequest = retrieveDocument.addNewArg0();
            retrieveDocumentRequest.setDocumentId(documentId);
            retrieveDocumentRequest.setHomeCommunityId(homeCommunityId);
            retrieveDocumentRequest.setCountryCode(countryCode);
            retrieveDocumentRequest.setClassCode(classCode);
            retrieveDocumentRequest.setTargetLanguage(targetLanguage);

            var retrieveDocumentResponseDocument = clientConnectorStub.retrieveDocument(retrieveDocumentDocument);
            return retrieveDocumentResponseDocument.getRetrieveDocumentResponse().getReturn();

        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated Method has been deprecated in favor of the implementation of retrieveDocument() with language parameter.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public EpsosDocument1 retrieveDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode) {

        logger.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}')", countryCode, homeCommunityId);
        return retrieveDocument(idAssertion, trcAssertion, countryCode, documentId, homeCommunityId, classCode, null);
    }

    public SubmitDocumentResponse submitDocument(Assertion idAssertion, Assertion trcAssertion, String countryCode,
                                                 EpsosDocument1 document, PatientDemographics patientDemographics) {

        logger.info("[Portal]: submitDocument(countryCode:'{}')", countryCode);
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
            logger.debug("Initializing Client Connector Stub Services");
            var clientConnectorStub = new ClientConnectorServiceStub(endpointReference);
            clientConnectorStub._getServiceClient().getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            clientConnectorStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(TIMEOUT);
            clientConnectorStub._getServiceClient().engageModule("addressing");
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
        List<PatientId> patientIds = new ArrayList<>();
        for (PatientId patientId : patientDemographics.getPatientIdArray()) {
            if (StringUtils.isNotBlank(patientId.getExtension())) {
                patientId.setExtension(StringUtils.trim(patientId.getExtension()));
                patientId.setRoot(StringUtils.trim(patientId.getRoot()));
                patientIds.add(patientId);
            }
        }
        patientDemographics.setPatientIdArray(patientIds.toArray(new PatientId[0]));
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
