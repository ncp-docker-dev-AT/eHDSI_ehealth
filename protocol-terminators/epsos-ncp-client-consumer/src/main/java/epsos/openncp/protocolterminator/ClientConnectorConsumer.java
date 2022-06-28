package epsos.openncp.protocolterminator;

import epsos.openncp.protocolterminator.clientconnector.*;
import epsos.openncp.pt.client.ClientConnectorServiceStub;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.XcpdErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.ErrorCode;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.AssertionHelper;
import eu.europa.ec.sante.ehdsi.openncp.evidence.utils.OutFlowEvidenceEmitterHandler;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.SSLContext;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
 *  Client Service class providing access to the MyHealth@EU workflows (Patient Summary, ePrescription, OrCD etc.).
 */
public class ClientConnectorConsumer {

    // Default timeout set to Three minutes.
    private static final Integer TIMEOUT = 180000;
    private static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    // Logger
    private final Logger logger = LoggerFactory.getLogger(ClientConnectorConsumer.class);
    // URL of the targeted NCP-B - ClientConnectorService.wsdl
    private final String endpointReference;

    public ClientConnectorConsumer(String endpointReference) {

        this.endpointReference = endpointReference;
    }

    /**
     * Returns a list of clinical documents related to the patient demographics provided.
     *
     * @param assertions   - Map of assertions required by the transaction (HCP, TRC and NoK optional).
     * @param countryCode  - ISO Country code of the patient country of origin.
     * @param patientId    - Unique Patient Identifier retrieved from NCP-A.
     * @param classCodes   - Class Codes of the documents to retrieve.
     * @param filterParams - Extra parameters for search filtering.
     * @return List of clinical documents and metadata searched by the clinician.
     */
    public List<EpsosDocument1> queryDocuments(Map<AssertionEnum, Assertion> assertions,
                                               String countryCode, PatientId patientId,
                                               List<GenericDocumentCode> classCodes, FilterParams filterParams)
            throws ClientConnectorConsumerException {

        logger.info("[National Connector] queryDocuments(countryCode:'{}')", countryCode);
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            addAssertions(clientConnectorServiceStub, assertions);

            var queryDocumentsDocument = QueryDocumentsDocument.Factory.newInstance();
            var queryDocuments = queryDocumentsDocument.addNewQueryDocuments();
            var queryDocumentRequest = queryDocuments.addNewArg0();
            GenericDocumentCode[] array = new GenericDocumentCode[classCodes.size()];
            for (int i = 0; i < classCodes.size(); i++) {
                array[i] = classCodes.get(i);
            }
            queryDocumentRequest.setClassCodeArray(array);
            queryDocumentRequest.setPatientId(patientId);
            queryDocumentRequest.setCountryCode(countryCode);
            queryDocumentRequest.setFilterParams(filterParams);

            QueryDocumentsResponseDocument queryDocumentsResponseDocument;
            queryDocumentsResponseDocument = clientConnectorServiceStub.queryDocuments(queryDocumentsDocument);
            EpsosDocument1[] docArray = queryDocumentsResponseDocument.getQueryDocumentsResponse().getReturnArray();

            return Arrays.asList(docArray);
        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_GENERIC, ex.getMessage(), null, ex);
        }
    }

    /**
     * Returns demographics of the patient corresponding to the identity traits provided.
     *
     * @param assertions          - Map of assertions required by the transaction (HCP, TRC and NoK optional)
     * @param countryCode         - ISO Country code of the patient country of origin.
     * @param patientDemographics - Identifiers of the requested patient
     * @return List of patients found (only 1 patient is expected in MyHealth@EU)
     */
    public List<PatientDemographics> queryPatient(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                                  PatientDemographics patientDemographics) throws ClientConnectorConsumerException {

        logger.info("[National Connector] queryPatient(countryCode:'{}')", countryCode);
        var clientConnectorServiceStub = initializeServiceStub();

        try {
            trimPatientDemographics(patientDemographics);
            addAssertions(clientConnectorServiceStub, assertions);
            var queryPatientRequest = QueryPatientRequest.Factory.newInstance();
            queryPatientRequest.setPatientDemographics(patientDemographics);
            queryPatientRequest.setCountryCode(countryCode);

            var queryPatientDocument = QueryPatientDocument.Factory.newInstance();
            queryPatientDocument.addNewQueryPatient().setArg0(queryPatientRequest);

            QueryPatientResponseDocument queryPatientResponseDocument = clientConnectorServiceStub.queryPatient(queryPatientDocument);
            PatientDemographics[] pdArray = queryPatientResponseDocument.getQueryPatientResponse().getReturnArray();
            return Arrays.asList(pdArray);
        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_PI_GENERIC, ex.getMessage(), null,  ex);
        }
    }

    /**
     * Default Webservice test method available mainly for configuration and testing purpose.
     *
     * @param assertions - Map of assertions required by the transaction (HCP, TRC and NoK optional).
     * @param name       - Token sent for testing.
     * @return Hello message concatenated with the token passed as parameter.
     */
    public String sayHello(Map<AssertionEnum, Assertion> assertions, String name) throws ClientConnectorConsumerException {

        logger.info("[National Connector] sayHello(name:'{}')", name);
        var clientConnectorServiceStub = initializeServiceStub();
        try {
            addAssertions(clientConnectorServiceStub, assertions);
            var sayHelloDocument = SayHelloDocument.Factory.newInstance();
            sayHelloDocument.addNewSayHello().setArg0(name);

            var sayHelloResponseDocument = clientConnectorServiceStub.sayHello(sayHelloDocument);
            return sayHelloResponseDocument.getSayHelloResponse().getReturn();
        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_GENERIC, ex.getMessage(), null, ex);
        }
    }

    /**
     * Retrieves the clinical document of an identified patient (prescription, patient summary or original clinical document).
     *
     * @param assertions      - Map of assertions required by the transaction (HCP, TRC and NoK optional).
     * @param countryCode     - ISO Country code of the patient country of origin.
     * @param documentId      - Unique identifier of the CDA document.
     * @param homeCommunityId - HL7 Home Community ID of the country of origin.
     * @param classCode       - HL7 ClassCode of the document type to be retrieved.
     * @param targetLanguage  - Expected target language of the CDA translation.
     * @return Clinical Document and metadata returned by the Country of Origin.
     */
    public EpsosDocument1 retrieveDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode,
                                           String targetLanguage) throws ClientConnectorConsumerException {

        logger.info("[National Connector] retrieveDocument(countryCode:'{}', homeCommunityId:'{}', targetLanguage:'{}')",
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

        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_GENERIC, ex.getMessage(), null, ex);
        }
    }

    /**
     * @deprecated Method has been deprecated in favor of the implementation of retrieveDocument() with language parameter.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public EpsosDocument1 retrieveDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                           DocumentId documentId, String homeCommunityId, GenericDocumentCode classCode)
            throws ClientConnectorConsumerException {

        logger.info("[National Connector] retrieveDocument(countryCode:'{}', homeCommunityId:'{}')", countryCode, homeCommunityId);
        return retrieveDocument(assertions, countryCode, documentId, homeCommunityId, classCode, null);
    }

    /**
     * Submits Clinical Document to the patient country of origin (dispense and discard).
     *
     * @param assertions          - Map of assertions required by the transaction (HCP, TRC and NoK optional).
     * @param countryCode         - ISO Country code of the patient country of origin.
     * @param document            - Clinical document and metadata to be submitted to the patient country of origin.
     * @param patientDemographics - Demographics of the patient linked to the document submission.
     * @return Acknowledge and status of the document submission.
     */
    public SubmitDocumentResponse submitDocument(Map<AssertionEnum, Assertion> assertions, String countryCode,
                                                 EpsosDocument1 document, PatientDemographics patientDemographics)
            throws ClientConnectorConsumerException {

        logger.info("[National Connector] submitDocument(countryCode:'{}')", countryCode);
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
        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        } catch (Exception ex) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_ED_GENERIC, ex.getMessage(), null, ex);
        }
    }

    /**
     * Adds the different types of Assertions to the SOAP Header.
     *
     * @param clientConnectorServiceStub - Client Service stub.
     * @param assertions                 - Map of assertions required by the transaction (HCP, TRC and NoK optional).
     */
    private void addAssertions(ClientConnectorServiceStub clientConnectorServiceStub,
                               Map<AssertionEnum, Assertion> assertions) throws Exception {

        if (!assertions.containsKey(AssertionEnum.CLINICIAN) || AssertionHelper.isExpired(assertions.get(AssertionEnum.CLINICIAN))) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED, "HCP Assertion expired", null);
        }

        var omFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPHeaderBlock omSecurityElement = omFactory.createSOAPHeaderBlock(
                omFactory.createOMElement(new QName(WSSE_NS, "Security", "wsse"), null));

        if (assertions.containsKey(AssertionEnum.NEXT_OF_KIN)) {
            var assertion = assertions.get(AssertionEnum.NEXT_OF_KIN);
            if (AssertionHelper.isExpired(assertion)) {
                throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED, "Next of Kin Assertion is expired", null);
            }
            omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        }
        if (assertions.containsKey(AssertionEnum.TREATMENT)) {
            var assertion = assertions.get(AssertionEnum.TREATMENT);
            if (AssertionHelper.isExpired(assertion)) {
                throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED, "Treatment Confirmation Assertion is expired", null);
            }
            omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        }
        var assertion = assertions.get(AssertionEnum.CLINICIAN);
        omSecurityElement.addChild(XMLUtils.toOM(assertion.getDOM()));
        clientConnectorServiceStub._getServiceClient().addHeader(omSecurityElement);
    }

    /**
     * Configures the SSL Context to support Two Way SLL and using the Service Consumer TLS certificate.
     *
     * @return Initialized SSL Context supporting Two Way SSL.
     */
    private SSLContext buildSSLContext() throws ClientConnectorConsumerException {

        try {
            SSLContextBuilder builder = SSLContextBuilder.create();
            builder.setKeyStoreType("JKS");
            builder.setKeyManagerFactoryAlgorithm("SunX509");

            builder.loadKeyMaterial(ResourceUtils.getFile(Constants.SC_KEYSTORE_PATH),
                    Constants.SC_KEYSTORE_PASSWORD.toCharArray(),
                    Constants.SC_PRIVATEKEY_PASSWORD.toCharArray());

            builder.loadTrustMaterial(ResourceUtils.getFile(Constants.TRUSTSTORE_PATH),
                    Constants.TRUSTSTORE_PASSWORD.toCharArray(), TrustAllStrategy.INSTANCE);

            return builder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | CertificateException |
                IOException | KeyManagementException e) {
            throw new ClientConnectorConsumerException(OpenncpErrorCode.ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED, "SSL Context cannot be initialized: " + e.getMessage(), null, e);
        }
    }

    /**
     * Returns Apache HttpClient supporting Two Way SSL and using TLS protocol 1.2 and 1.3.
     *
     * @return Secured HttpClient initialized.
     */
    private HttpClient getSSLClient() throws ClientConnectorConsumerException {

        SSLContext sslContext = buildSSLContext();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslContext, new String[]{"TLSv1.2", "TLSv1.3"}, null, NoopHostnameVerifier.INSTANCE);
        HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);
        builder.setSSLContext(sslContext);
        builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        return builder.build();
    }

    /**
     * Initializes the ClientConnectorService client stubs to contact WSDL.
     *
     * @return Initialized ClientConnectorServiceStub set to the configured EPR and the SOAP version.
     */
    private ClientConnectorServiceStub initializeServiceStub() throws ClientConnectorConsumerException {

        try {

            logger.info("[National Connector] Initializing ClientConnectorService Stub");
            var clientConnectorStub = new ClientConnectorServiceStub(endpointReference);
            clientConnectorStub._getServiceClient().getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            clientConnectorStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(TIMEOUT);
            clientConnectorStub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT);
            clientConnectorStub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT);
            // Enabling WS Addressing module.
            clientConnectorStub._getServiceClient().engageModule("addressing");
            this.registerEvidenceEmitterHandler(clientConnectorStub);

            // Enabling Axis2 - SSL 2 ways communication (not active by default).
            clientConnectorStub._getServiceClient().getServiceContext().getConfigurationContext()
                    .setProperty(HTTPConstants.CACHED_HTTP_CLIENT, getSSLClient());
            clientConnectorStub._getServiceClient().getServiceContext().getConfigurationContext()
                    .setProperty(HTTPConstants.REUSE_HTTP_CLIENT, false);

            return clientConnectorStub;
        } catch (AxisFault axisFault) {
            throw createClientConnectorConsumerException(axisFault);
        }
    }

    /**
     * Configures the Non Repudiation process into the Apache Axis2 phase.
     *
     * @param clientConnectorServiceStub - Client Service stub.
     */
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

    /**
     * Trims the Patient Demographics sent by the client and received by the Client Connector.
     *
     * @param patientDemographics Identity Traits to be trimmed and provided by the client
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

    private ClientConnectorConsumerException createClientConnectorConsumerException(AxisFault axisFault){

        String errorCode = axisFault.getFaultCode() != null ? axisFault.getFaultCode().getLocalPart() : null;
        String message  = axisFault.getMessage();
        String context = axisFault.getDetail() != null ? axisFault.getDetail().getText() : null;

        OpenncpErrorCode openncpErrorCode = OpenncpErrorCode.getErrorCode(errorCode);

        return new ClientConnectorConsumerException(openncpErrorCode, message, context, axisFault);
    }
}
