package eu.epsos.pt.ws.client.xca;

import ee.affecto.epsos.util.EventLogClientUtil;
import eu.epsos.dts.xds.AdhocQueryRequestCreator;
import eu.epsos.dts.xds.AdhocQueryResponseConverter;
import eu.epsos.exceptions.DocumentTransformationException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.pt.transformation.TMServices;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.DynamicDiscoveryService;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.ws.xca.client.RespondingGateway_ServiceStub;
import tr.com.srdc.epsos.ws.xca.client.retrieve.RetrieveDocumentSetRequestTypeCreator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * XCA Initiating Gateway
 * <p>
 * This is an implementation of a IHE XCA Initiation Gateway.
 * This class provides the necessary operations to query and retrieve documents.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class XcaInitGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcaInitGateway.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    /**
     * Private constructor to disable class instantiation.
     */
    private XcaInitGateway() {
    }

    public static QueryResponse crossGatewayQuery(final PatientId pid, final String countryCode, final GenericDocumentCode documentCode,
                                                  final Assertion idAssertion, final Assertion trcAssertion, String service) throws XCAException {


        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            LOGGER_CLINICAL.info("QueryResponse crossGatewayQuery('{}','{}','{}','{}','{}','{}')", pid.getExtension(), countryCode,
                    documentCode.getValue(), idAssertion.getID(), trcAssertion.getID(), service);
        }
        QueryResponse result = null;

        try {

            /* queryRequest */
            AdhocQueryRequest queryRequest;
            queryRequest = AdhocQueryRequestCreator.createAdhocQueryRequest(pid.getExtension(), pid.getRoot(), documentCode);

            /* Stub */
            RespondingGateway_ServiceStub stub = new RespondingGateway_ServiceStub();
            DynamicDiscoveryService dynamicDiscoveryService = new DynamicDiscoveryService();
            String epr = dynamicDiscoveryService.getEndpointUrl(countryCode.toLowerCase(Locale.ENGLISH), RegisteredService.fromName(service));
            stub.setAddr(epr);
            stub._getServiceClient().getOptions().setTo(new EndpointReference(epr));
            EventLogClientUtil.createDummyMustUnderstandHandler(stub);
            stub.setCountryCode(countryCode);

            /* queryResponse */
            AdhocQueryResponse queryResponse = stub.respondingGateway_CrossGatewayQuery(queryRequest, idAssertion, trcAssertion, documentCode.getValue());   // Request
            processRegistryErrors(queryResponse.getRegistryErrorList());

            if (queryResponse.getRegistryObjectList() != null) {
                result = AdhocQueryResponseConverter.convertAdhocQueryResponse(queryResponse);
            }

        } catch (RemoteException | RuntimeException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }

    public static DocumentResponse crossGatewayRetrieve(final XDSDocument document, final String homeCommunityId,
                                                        final String countryCode, final String targetLanguage,
                                                        final Assertion idAssertion, final Assertion trcAssertion,
                                                        String service) throws XCAException {

        LOGGER.info("QueryResponse crossGatewayQuery('{}','{}','{}','{}','{}','{}', '{}')", document.getDocumentUniqueId(),
                homeCommunityId, countryCode, targetLanguage, idAssertion.getID(), trcAssertion.getID(), service);

        DocumentResponse result = null;
        RetrieveDocumentSetResponseType queryResponse;
        String classCode = null;

        try {

            /* Request */
            RetrieveDocumentSetRequestType queryRequest;
            queryRequest = new RetrieveDocumentSetRequestTypeCreator().createRetrieveDocumentSetRequestType(document.getDocumentUniqueId(), homeCommunityId, document.getRepositoryUniqueId());

            /* Stub */
            RespondingGateway_ServiceStub stub;
            stub = new RespondingGateway_ServiceStub();
            DynamicDiscoveryService dynamicDiscoveryService = new DynamicDiscoveryService();
            String epr;
            if (service.equals(Constants.MroService)) {

                epr = dynamicDiscoveryService.getEndpointUrl(countryCode.toLowerCase(Locale.ENGLISH), RegisteredService.PATIENT_SERVICE);
            } else {

                epr = dynamicDiscoveryService.getEndpointUrl(countryCode.toLowerCase(Locale.ENGLISH), RegisteredService.fromName(service));
            }
            stub.setAddr(epr);
            stub._getServiceClient().getOptions().setTo(new EndpointReference(epr));
            stub.setCountryCode(countryCode);
            EventLogClientUtil.createDummyMustUnderstandHandler(stub);
            // This is a rather dirty hack, but document.getClassCode() returns null for some reason.
            switch (service) {
                case Constants.OrderService:
                    classCode = Constants.EP_CLASSCODE;
                    break;
                case Constants.PatientService:
                    classCode = Constants.PS_CLASSCODE;
                    break;
                case Constants.MroService:
                    classCode = Constants.MRO_CLASSCODE;
                    break;
            }

            /* Request */
            queryResponse = stub.respondingGateway_CrossGatewayRetrieve(queryRequest, idAssertion, trcAssertion, classCode);

            if (queryResponse.getRegistryResponse() != null) {

                RegistryErrorList registryErrorList = queryResponse.getRegistryResponse().getRegistryErrorList();
                processRegistryErrors(registryErrorList);
            }

        } catch (AxisFault ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }

        if (!queryResponse.getDocumentResponse().isEmpty()) {
            if (queryResponse.getDocumentResponse().size() > 1) {
                LOGGER.error("More than one documents where retrieved for the current request with parameters document ID: '{}' " +
                        "- homeCommunityId: '{}' - registry: ", document.getDocumentUniqueId(), homeCommunityId, document.getRepositoryUniqueId());
            }
            //TODO: review this try - catch - finally mechanism and the transformation/translation mechanism.
            try {
                LOGGER.info("******************************************");
                /* Validate CDA epSOS Pivot */
//                Element elementNormalize = TMServices.byteToDocument(queryResponse.getDocumentResponse().get(0).getDocument()).getDocumentElement();
//                elementNormalize.normalize();
//                cdaValidationService.validateModel(XMLUtils.toOM(elementNormalize).toString(), CdaModel.obtainCdaModel(document.getClassCode().getValue(), true), NcpSide.NCP_B);
                //String cdaModel = CdaModel.obtainCdaModel(document.getClassCode().getValue(), true);
                if (OpenNCPValidation.isValidationEnable()) {
                    OpenNCPValidation.validateCdaDocument(XMLUtils.toOM(TMServices.byteToDocument(
                            queryResponse.getDocumentResponse().get(0).getDocument()).getDocumentElement()).toString(),
                            NcpSide.NCP_B, document.getClassCode().getValue(), true);
                }
//                //Resets the response document to a translated version.
                queryResponse.getDocumentResponse().get(0).setDocument(TMServices.transformDocument(
                        queryResponse.getDocumentResponse().get(0).getDocument(), targetLanguage));
//
                /* Validate CDA epSOS Friendly-B */
//                cdaValidationService.validateModel(XMLUtils.toOM(TMServices.byteToDocument(
//                        queryResponse.getDocumentResponse().get(0).getDocument()).getDocumentElement()).toString(),
//                        CdaModel.obtainCdaModel(document.getClassCode().getValue(), false), NcpSide.NCP_B);

                //String cdaModel2 = CdaModel.obtainCdaModel(document.getClassCode().getValue(), false);
                if (OpenNCPValidation.isValidationEnable()) {
                    OpenNCPValidation.validateCdaDocument(XMLUtils.toOM(TMServices.byteToDocument(
                            queryResponse.getDocumentResponse().get(0).getDocument()).getDocumentElement()).toString(),
                            NcpSide.NCP_B, document.getClassCode().getValue(), false);
                }

            } catch (DocumentTransformationException e) {
                LOGGER.warn("DocumentTransformationException: document cannot be translated:\n{}", e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
            } finally {
                LOGGER.debug("[XCA Init Gateway] Returns Original Document");
                //  Returns the original document, even if the translation process fails.
                result = queryResponse.getDocumentResponse().get(0);
            }
        }
        return result;
    }

    /**
     * Processes registry errors from the {@link AdhocQueryResponse} message, by
     * reporting them to the logging system.
     *
     * @param registryErrorList the list of errors from the
     *                          {@link AdhocQueryResponse} message.
     * @throws Exception thrown when an error has a severity of
     *                   "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error" type.
     */
    private static void processRegistryErrors(RegistryErrorList registryErrorList) throws XCAException {
        // A.R. ++ Error processing. For retrieve. Is it needed?
        // We don't want to break on TSAM errors anyway...

        if (registryErrorList != null) {
            List<RegistryError> errorList = registryErrorList.getRegistryError();

            if (errorList != null) {
                StringBuilder msg = new StringBuilder();
                boolean hasError = false;
                for (RegistryError error : errorList) {
                    String errorCode = error.getErrorCode();
                    String value = error.getValue();
                    String location = error.getLocation();
                    String severity = error.getSeverity();
                    String codeContext = error.getCodeContext();
                    LOGGER.error("errorCode=" + errorCode + "\ncodeContext=" + codeContext
                            + "\nlocation=" + location + "\nseverity=" + severity + "\n" + value + "\n");

                    // Marcelo Fonseca: Added error situation where no document is found or registered, 1101/2.
                    // (Needs to be revised according to new error communication strategy to the portal).
                    if ("urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error".equals(severity) || errorCode.equals("1101") || errorCode.equals("1102")) {
                        msg.append(errorCode).append(" ").append(codeContext).append(" ").append(value);
                        hasError = true;
                    }

                    // Avoid the transformation errors to abort process - this way they are only logged in the upper instructions
                    if (checkTransformationErrors(errorCode)) {
                        continue;
                    }

                    //Throw all the remaining errors
                    if (hasError) {
                        LOGGER.error(msg.toString());
                        throw new XCAException(errorCode);
                    }
                }
            }
        }
    }

    /**
     * This method will check if a given code is related to the document transformation errors
     *
     * @param errorCode
     * @return
     */
    private static boolean checkTransformationErrors(String errorCode) {

        List<String> errorCodes = new ArrayList<>();
        errorCodes.add("4500");
        errorCodes.add("4501");
        errorCodes.add("4502");
        errorCodes.add("4503");
        errorCodes.add("4504");
        errorCodes.add("4505");
        errorCodes.add("4506");
        errorCodes.add("4507");
        errorCodes.add("4508");
        errorCodes.add("4509");
        errorCodes.add("4510");
        errorCodes.add("4511");
        errorCodes.add("4512");
        errorCodes.add("2500");
        errorCodes.add("2501");
        errorCodes.add("2502");
        errorCodes.add("2503");
        errorCodes.add("2504");
        errorCodes.add("2505");
        errorCodes.add("2506");
        errorCodes.add("2507");
        errorCodes.add("2508");

        return errorCodes.contains(errorCode);
    }
}
