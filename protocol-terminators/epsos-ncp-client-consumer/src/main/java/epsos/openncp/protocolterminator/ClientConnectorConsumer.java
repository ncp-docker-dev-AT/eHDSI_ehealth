package epsos.openncp.protocolterminator;

import epsos.openncp.protocolterminator.clientconnector.*;
import epsos.openncp.pt.client.ClientConnectorServiceStub;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionHelper;
import eu.europa.ec.sante.ehdsi.openncp.evidence.utils.OutFlowEvidenceEmitterHandler;
import eu.europa.ec.sante.openncp.protocolterminator.commons.AssertionEnum;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
 *  ClientConnectorConsumer
 */
public class ClientConnectorConsumer {

    // Default timeout set to Three minutes.
    private static final Integer TIMEOUT = 180000;
    private final Logger logger = LoggerFactory.getLogger(ClientConnectorConsumer.class);
    private final String endpointReference;

    public ClientConnectorConsumer(String endpointReference) {

        this.endpointReference = endpointReference;
    }

    private static void addAssertions(ClientConnectorServiceStub clientConnectorServiceStub,
                                      Map<AssertionEnum, Assertion> assertions) throws Exception {

        if (!assertions.containsKey(AssertionEnum.CLINICIAN) || AssertionHelper.isExpired(assertions.get(AssertionEnum.CLINICIAN))) {
            throw new ClientConnectorConsumerException("HCP Assertion expired");
        }
        var omFactory = OMAbstractFactory.getOMFactory();
        OMElement omSecurityElement = omFactory.createOMElement(
                new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "Security", "wsse"), null);
        if (assertions.containsKey(AssertionEnum.NEXT_OF_KIN)) {
            var assertion = assertions.get(AssertionEnum.NEXT_OF_KIN);
            if (AssertionHelper.isExpired(assertion)) {
                throw new ClientConnectorConsumerException("Next of Kin Assertion is expired");
            }
            omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        }
        if (assertions.containsKey(AssertionEnum.TREATMENT)) {
            var assertion = assertions.get(AssertionEnum.TREATMENT);
            if (AssertionHelper.isExpired(assertion)) {
                throw new ClientConnectorConsumerException("Treatment Confirmation Assertion is expired");
            }
            omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        }
        var assertion = assertions.get(AssertionEnum.CLINICIAN);
        omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        clientConnectorServiceStub._getServiceClient().addHeader(omSecurityElement);
    }

//    private static void addAssertions(ClientConnectorServiceStub clientConnectorServiceStub, Assertion idAssertion,
//                                      Optional<Assertion> nokAssertion, Assertion trcAssertion) throws Exception {
//
//        if (AssertionHelper.isExpired(idAssertion)) {
//            throw new ClientConnectorConsumerException("HCP Assertion expired");
//        }
//        var omFactory = OMAbstractFactory.getOMFactory();
//        OMElement omSecurityElement = omFactory.createOMElement(
//                new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
//                        "Security", "wsse"), null);
//        var assertion = nokAssertion.orElse(null);
//        if (assertion != null) {
//            if (AssertionHelper.isExpired(assertion)) {
//                throw new ClientConnectorConsumerException("Next of Kin Assertion is expired");
//            }
//            omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
//        }
//        if (trcAssertion != null) {
//            if (AssertionHelper.isExpired(trcAssertion)) {
//                throw new ClientConnectorConsumerException("Treatment Confirmation Assertion is expired");
//            }
//            omSecurityElement.addChild(XMLUtils.toOM(trcAssertion.getDOM()));
//        }
//        omSecurityElement.addChild(XMLUtils.toOM(idAssertion.getDOM()));
//        clientConnectorServiceStub._getServiceClient().addHeader(omSecurityElement);
//    }

    private void registerEvidenceEmitterHandler(ClientConnectorServiceStub clientConnectorServiceStub) {

        // Adding custom phase for evidence emitter processing.
        logger.debug("Adding custom phase for Outflow Evidence Emitter processing");
        var outFlowHandlerDescription = new HandlerDescription("OutFlowEvidenceEmitterHandler");
        outFlowHandlerDescription.setHandler(new OutFlowEvidenceEmitterHandler());
        var axisConfiguration = clientConnectorServiceStub._getServiceClient().getServiceContext()
                .getConfigurationContext().getAxisConfiguration();
        List<Phase> outFlowPhasesList = axisConfiguration.getOutFlowPhases();
        var outFlowEvidenceEmitterPhase = new Phase("OutFlowEvidenceEmitterPhase");
        try {
            outFlowEvidenceEmitterPhase.addHandler(outFlowHandlerDescription);
        } catch (PhaseException ex) {
            logger.error("PhaseException: '{}'", ex.getMessage(), ex);
        }
        outFlowPhasesList.add(outFlowEvidenceEmitterPhase);
        axisConfiguration.setGlobalOutPhase(outFlowPhasesList);
    }

    public List<EpsosDocument1> queryDocuments(Map<AssertionEnum, Assertion> assertions,
                                               String countryCode, PatientId patientId,
                                               List<GenericDocumentCode> classCodes, FilterParams filterParams) throws ClientConnectorConsumerException {

        logger.info("[Portal]: queryDocuments(countryCode:'{}', patientId:'{}')", countryCode, patientId.getRoot());
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            addAssertions(clientConnectorServiceStub, assertions);

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
            queryDocumentRequest.setFilterParams(filterParams);

            QueryDocumentsResponseDocument queryDocumentsResponseDocument;
            queryDocumentsResponseDocument = clientConnectorServiceStub.queryDocuments(queryDocumentsDocument);
            EpsosDocument1[] docArray = queryDocumentsResponseDocument.getQueryDocumentsResponse().getReturnArray();

            return Arrays.asList(docArray);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * @param assertions          - Map of assertions used to identify clinician and next of kin.
     * @param countryCode         - Country of treatment
     * @param patientDemographics - Identifiers of the requested patient
     * @return List of patients found (only 1 patient is expected in eHDSI)
     */
    public List<PatientDemographics> queryPatient(Map<AssertionEnum, Assertion> assertions,
                                                  String countryCode, PatientDemographics patientDemographics) throws ClientConnectorConsumerException {

        logger.info("[Portal]: queryPatient(countryCode:'{}')", countryCode);
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(clientConnectorServiceStub, assertions);
            var queryPatientRequest = QueryPatientRequest.Factory.newInstance();
            queryPatientRequest.setPatientDemographics(patientDemographics);
            queryPatientRequest.setCountryCode(countryCode);

            var queryPatientDocument = QueryPatientDocument.Factory.newInstance();
            queryPatientDocument.addNewQueryPatient().setArg0(queryPatientRequest);

            var queryPatientResponseDocument =
                    clientConnectorServiceStub.queryPatient(queryPatientDocument);
            PatientDemographics[] pdArray = queryPatientResponseDocument.getQueryPatientResponse().getReturnArray();
            return Arrays.asList(pdArray);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Default Webservice test method available mainly for configuration purpose.
     */
    public String sayHello(Map<AssertionEnum, Assertion> assertions, String name) throws ClientConnectorConsumerException {

        logger.info("[Portal]: sayHello(name:'{}')", name);
        var clientConnectorServiceStub = initializeServiceStub();
        try {
            addAssertions(clientConnectorServiceStub, assertions);
            var sayHelloDocument = SayHelloDocument.Factory.newInstance();
            sayHelloDocument.addNewSayHello().setArg0(name);

            var sayHelloResponseDocument = clientConnectorServiceStub.sayHello(sayHelloDocument);
            return sayHelloResponseDocument.getSayHelloResponse().getReturn();
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    public EpsosDocument1 retrieveDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode,
                                           String targetLanguage) throws ClientConnectorConsumerException {

        logger.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}', targetLanguage:'{}')",
                countryCode, homeCommunityId, targetLanguage);
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            addAssertions(clientConnectorServiceStub, assertions);
            RetrieveDocumentDocument1 retrieveDocumentDocument = RetrieveDocumentDocument1.Factory.newInstance();
            RetrieveDocument1 retrieveDocument = retrieveDocumentDocument.addNewRetrieveDocument();

            var retrieveDocumentRequest = retrieveDocument.addNewArg0();
            retrieveDocumentRequest.setDocumentId(documentId);
            retrieveDocumentRequest.setHomeCommunityId(homeCommunityId);
            retrieveDocumentRequest.setCountryCode(countryCode);
            retrieveDocumentRequest.setClassCode(classCode);
            retrieveDocumentRequest.setTargetLanguage(targetLanguage);

            var retrieveDocumentResponseDocument =
                    clientConnectorServiceStub.retrieveDocument(retrieveDocumentDocument);
            return retrieveDocumentResponseDocument.getRetrieveDocumentResponse().getReturn();

        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated Method has been deprecated in favor of the implementation of retrieveDocument() with language parameter.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public EpsosDocument1 retrieveDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode) throws ClientConnectorConsumerException {

        logger.info("[Portal]: retrieveDocument(countryCode:'{}', homeCommunityId:'{}')", countryCode, homeCommunityId);
        return retrieveDocument(assertions, countryCode, documentId, homeCommunityId, classCode, null);
    }

    public SubmitDocumentResponse submitDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                                 EpsosDocument1 document, PatientDemographics patientDemographics) throws ClientConnectorConsumerException {

        logger.info("[Portal]: submitDocument(countryCode:'{}')", countryCode);
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(clientConnectorServiceStub, assertions);
            SubmitDocumentDocument1 submitDocumentDoc = SubmitDocumentDocument1.Factory.newInstance();
            SubmitDocument1 submitDocument = SubmitDocument1.Factory.newInstance();
            SubmitDocumentRequest submitDocRequest = SubmitDocumentRequest.Factory.newInstance();
            submitDocRequest.setPatientDemographics(patientDemographics);
            submitDocRequest.setDocument(document);
            submitDocRequest.setCountryCode(countryCode);
            submitDocument.setArg0(submitDocRequest);
            submitDocumentDoc.setSubmitDocument(submitDocument);

            return clientConnectorServiceStub.submitDocument(submitDocumentDoc).getSubmitDocumentResponse();
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(ex.getMessage(), ex);
        }
    }

    /**
     * Initializing the ClientConnectorService client stubs to contact WSDL.
     *
     * @return Initialized ClientConnectorServiceStub set to the configured EPR and the SOAP version.
     */
    private ClientConnectorServiceStub initializeServiceStub() throws ClientConnectorConsumerException {

        try {

            logger.info("Initializing Client Connector Stub Services");
            var clientConnectorStub = new ClientConnectorServiceStub(endpointReference);
            clientConnectorStub._getServiceClient().getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            clientConnectorStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(TIMEOUT);
            clientConnectorStub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT);
            clientConnectorStub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT);
            clientConnectorStub._getServiceClient().engageModule("addressing");
            this.registerEvidenceEmitterHandler(clientConnectorStub);
            logger.debug("[Axis2 configuration] Default Timeout: '{}'",
                    clientConnectorStub._getServiceClient().getOptions().getTimeOutInMilliSeconds());
            logger.debug("[Axis2 configuration] HTTPConstants.SO_TIMEOUT: '{}'",
                    clientConnectorStub._getServiceClient().getOptions().getProperty(HTTPConstants.SO_TIMEOUT));
            logger.debug("[Axis2 configuration] HTTPConstants.CONNECTION_TIMEOUT: '{}'",
                    clientConnectorStub._getServiceClient().getOptions().getProperty(HTTPConstants.CONNECTION_TIMEOUT));
            return clientConnectorStub;
        } catch (AxisFault e) {
            throw new ClientConnectorConsumerException(e.getMessage(), e);
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
