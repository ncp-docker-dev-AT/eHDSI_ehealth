package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.exceptions.XDRException;
import eu.epsos.pt.cc.dts.axis2.*;
import eu.epsos.pt.cc.stub.*;
import eu.epsos.util.IheConstants;
import eu.europa.ec.sante.openncp.protocolterminator.commons.AssertionEnum;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.spms.epsos.utils.logging.LoggingSlf4j;
import tr.com.srdc.epsos.data.model.XdrResponse;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.util.Constants;

import java.text.ParseException;
import java.util.*;

/**
 * ClientConnectorServiceSkeleton java skeleton for the axisService.
 * <p>
 * This class implements the contact point into the NCP-B, allowing the Portal-B to contact and perform requests in NCP-B.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class ClientConnectorServiceSkeleton implements ClientConnectorServiceSkeletonInterface {

    private static final String UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION = "Unsupported Class Code scheme: ";
    private static final String UNSUPPORTED_CLASS_CODE_EXCEPTION = "Unsupported Class Code: ";
    private final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceSkeleton.class);

    /**
     * Performs international search for a patient, filtering by a set of demographics.
     * This method is an adapter for usage of a XCPD client.
     *
     * @param queryPatient axis wrapper for element: <code>queryPatient</code>. This encapsulates, destination Country
     *                     Code and Patient's demographics.
     * @return a QueryPatientResponseDocument containing the query response(s).
     * @throws ParseException Exception thrown while the Payload cannot be parsed.
     */
    @Override
    public QueryPatientResponseDocument queryPatient(final QueryPatientDocument queryPatient, Map<AssertionEnum, Assertion> assertionMap)
            throws NoPatientIdDiscoveredException, ParseException {

        final var methodName = "queryPatient";
        LoggingSlf4j.start(logger, methodName);

        var queryPatientResponseDocument = QueryPatientResponseDocument.Factory.newInstance();

        try {
            /* Creating request */
            List<tr.com.srdc.epsos.data.model.PatientDemographics> xcpdResp;
            var queryPatientRequest = queryPatient.getQueryPatient().getArg0();
            var patientDemographics = queryPatientRequest.getPatientDemographics();
            tr.com.srdc.epsos.data.model.PatientDemographics request = eu.epsos.pt.cc.dts.PatientDemographicsDts.newInstance(patientDemographics);
            String countryCode = queryPatientRequest.getCountryCode();

            // Calling XCPD Client
            xcpdResp = IdentificationService.findIdentityByTraits(request, assertionMap, countryCode);

            //  Response
            List<PatientDemographics> aux = eu.epsos.pt.cc.dts.axis2.PatientDemographicsDts.newInstance(xcpdResp);
            var queryPatientResponse = QueryPatientResponseDts.newInstance(aux);
            queryPatientResponseDocument.setQueryPatientResponse(queryPatientResponse);

        } catch (ClientConnectorException ex) {
            LoggingSlf4j.error(logger, methodName, ex);
            throw ex;
        }
        LoggingSlf4j.end(logger, methodName);
        return queryPatientResponseDocument;
    }

    /**
     * Performs international search for documents. Filtering by patient and document code.
     * This method is an adapter for the usage of a XCA client.
     *
     * @param queryDocumentsDocument axis wrapper for element: <code>queryDocuments</code>. This encapsulates, destination
     *                               Country Code, patient's identification and documents class code.
     * @return a QueryDocumentsResponseDocument containing the query response(s).
     */
    @Override
    public QueryDocumentsResponseDocument queryDocuments(QueryDocumentsDocument queryDocumentsDocument,
                                                         Map<AssertionEnum, Assertion> assertionMap)
            throws XCAException {

        final var methodName = "queryDocuments";
        LoggingSlf4j.start(logger, methodName);
        var queryDocumentsResponse = QueryDocumentsResponse.Factory.newInstance();

        /* retrieve data from parameters */
        var queryDocuments = queryDocumentsDocument.getQueryDocuments();
        var queryDocumentRequest = queryDocuments.getArg0();
        String countryCode = queryDocumentRequest.getCountryCode();


        PatientId tmp = queryDocumentRequest.getPatientId();
        tr.com.srdc.epsos.data.model.PatientId patientId = eu.epsos.pt.cc.dts.PatientIdDts.newInstance(tmp);

        List<GenericDocumentCode> classCodes = Arrays.asList(queryDocumentRequest.getClassCodeArray());
        List<tr.com.srdc.epsos.data.model.GenericDocumentCode> documentCodes = eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(classCodes);

        FilterParams filterParamsReceived = queryDocumentRequest.getFilterParams();
        var patientId = eu.epsos.pt.cc.dts.PatientIdDts.newInstance(queryDocumentRequest.getPatientId());
        GenericDocumentCode tmpCode = queryDocumentRequest.getClassCode();
        tr.com.srdc.epsos.data.model.GenericDocumentCode documentCode = eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(tmpCode);

        tr.com.srdc.epsos.data.model.FilterParams filterParams = eu.epsos.pt.cc.dts.FilterParamsDts.newInstance(filterParamsReceived);

        for (tr.com.srdc.epsos.data.model.GenericDocumentCode documentCode: documentCodes) {
            if (!documentCode.getSchema().equals(IheConstants.CLASSCODE_SCHEME)) {
                throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + documentCode.getSchema());
            }
        }

        //  Performing call to Web Service:
        try {
            QueryResponse response;
            if (documentCodes.size()==1) {
                switch (documentCodes.get(0).getValue()) {
                    case Constants.PS_CLASSCODE:
                        response = PatientService.list(patientId, countryCode, documentCodes.get(0), hcpAssertion, trcAssertion);
                        break;
                    case Constants.EP_CLASSCODE:
                        response = OrderService.list(patientId, countryCode, documentCodes.get(0), hcpAssertion, trcAssertion);
                        break;
                    case Constants.MRO_CLASSCODE:
                        response = MroService.list(patientId, countryCode, documentCodes.get(0), hcpAssertion, trcAssertion);
                        break;
                    case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                    case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                    case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                        response = OrCDService.list(patientId, countryCode, Arrays.asList(documentCodes.get(0)), filterParams, hcpAssertion, trcAssertion);
                        break;
                    default:
                        throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + Arrays.toString(documentCodes.toArray()));
                }
            } else {
                if (!documentCodes.contains(Constants.EP_CLASSCODE)
                        && !documentCodes.contains(Constants.PS_CLASSCODE)
                        && !documentCodes.contains(Constants.MRO_CLASSCODE)) {
                response = OrCDService.list(patientId, countryCode, documentCodes, filterParams, hcpAssertion, trcAssertion);
                } else {
                    throw new ClientConnectorException("Invalid combination of document codes provided: only OrCD document codes can be combined.");
                }
            }

            if (response.getDocumentAssociations() != null && !response.getDocumentAssociations().isEmpty()) {
                queryDocumentsResponse.setReturnArray(DocumentDts.newInstance(response.getDocumentAssociations()));
            }

        } catch (RuntimeException e) {
            LoggingSlf4j.error(logger, methodName);
            throw e;
        } catch (RuntimeException ex) {
            LoggingSlf4j.error(logger, methodName, ex);
            throw ex;
        }

        // create return wrapper
        QueryDocumentsResponseDocument wrapper = QueryDocumentsResponseDocument.Factory.newInstance();
        wrapper.setQueryDocumentsResponse(queryDocumentsResponse);

        LoggingSlf4j.end(logger, methodName);
        return wrapper;
    }

    /**
     * Performs international search for documents. Filtering by patient and document code.
     * This method is an adapter for usage of a XCA client.
     * <p>
     * It makes use of the XCA Service Client library.
     *
     * @param retrieveDocument axis wrapper for element: <code>retrieveDocument</code>. This encapsulates, destination
     *                         Country Code, patient's identification and document's identification.
     * @return the retrieved document.
     */
    @Override
    public RetrieveDocumentResponseDocument retrieveDocument(RetrieveDocumentDocument1 retrieveDocument,
                                                             Map<AssertionEnum, Assertion> assertionMap)
            throws XCAException {
        /*
         * Setup
         */
        final var methodName = "retrieveDocument";
        LoggingSlf4j.start(logger, methodName);

        RetrieveDocumentResponse result;
        /*
         * Body
         */
        final var retrieveDocumentRequest = retrieveDocument.getRetrieveDocument().getArg0();
        String countryCode = retrieveDocumentRequest.getCountryCode();
        var documentId = retrieveDocumentRequest.getDocumentId();
        String homeCommunityId = retrieveDocumentRequest.getHomeCommunityId();
        String targetLanguage = retrieveDocumentRequest.getTargetLanguage();

        GenericDocumentCode tmpCode = retrieveDocumentRequest.getClassCode();
        tr.com.srdc.epsos.data.model.GenericDocumentCode documentCode = eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(tmpCode);

        if (!documentCode.getSchema().equals(IheConstants.CLASSCODE_SCHEME)) {
            throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + documentCode.getSchema());
        }

        try {
            DocumentResponse response;
            var xdsDocument = XdsDocumentDts.newInstance(documentId);
            xdsDocument.setClassCode(documentCode);

            logger.info("[ClientConnector retrieveDocument()] homeCommunityId: '{}' targetLanguage: '{}'", homeCommunityId, targetLanguage);
            switch (documentCode.getValue()) {
                case Constants.PS_CLASSCODE:
                    response = PatientService.retrieve(xdsDocument, homeCommunityId, countryCode, targetLanguage,
                            assertionMap);
                    break;
                case Constants.EP_CLASSCODE:
                    response = OrderService.retrieve(xdsDocument, homeCommunityId, countryCode, targetLanguage,
                            assertionMap);
                    break;
                case Constants.MRO_CLASSCODE:
                    response = MroService.retrieve(xdsDocument, homeCommunityId, countryCode, targetLanguage,
                            assertionMap);
                    response = MroService.retrieve(xdsDocument, homeCommunityId, countryCode, targetLanguage,
                            assertionMap);
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    response = OrCDService.retrieve(xdsDocument, homeCommunityId, countryCode, targetLanguage,
                            assertionMap);
                    break;
                default:
                    throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + documentCode.getValue());
            }

            result = RetrieveDocumentResponseDTS.newInstance(response);

        } catch (ClientConnectorException ex) {
            LoggingSlf4j.error(logger, methodName, ex);
            throw ex;
        }

        // create return wrapper
        RetrieveDocumentResponseDocument wrapper = RetrieveDocumentResponseDocument.Factory.newInstance();
        wrapper.setRetrieveDocumentResponse(result);
        LoggingSlf4j.end(logger, methodName);
        return wrapper;
    }

    /**
     * Submits a document to a foreign country. This method is an adapter for usage of a XDR client.
     * <p>
     * This method makes use of the XDR Service Client library.
     *
     * @param submitDocument axis wrapper for element: <code>submitDocument</code>. This encapsulates, destination
     *                       Country Code and the document to submit with some Metadata.
     * @return a SubmitDocumentResponseDocument object.
     * @throws ParseException Exception thrown while the Payload cannot be parsed.
     */
    @Override
    public SubmitDocumentResponseDocument submitDocument(final SubmitDocumentDocument1 submitDocument,
                                                         Map<AssertionEnum, Assertion> assertionMap)
            throws XDRException, ParseException {

        final var methodName = "submitDocument";
        LoggingSlf4j.start(logger, methodName);

        SubmitDocumentResponseDocument result = SubmitDocumentResponseDocument.Factory.newInstance();

        try {
            /*  create Xdr request */
            var submitDocument1 = submitDocument.getSubmitDocument();
            var submitDocumentRequest = submitDocument1.getArg0();
            String countryCode = submitDocumentRequest.getCountryCode();
            EpsosDocument1 document = submitDocumentRequest.getDocument();
            var patientDemographics = submitDocumentRequest.getPatientDemographics();
            GenericDocumentCode classCode = document.getClassCode();
            if (!classCode.getSchema().equals(IheConstants.CLASSCODE_SCHEME)) {
                throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + classCode.getSchema());
            }
            String classCodeNode = classCode.getNodeRepresentation();
            String nodeRepresentation = document.getFormatCode().getNodeRepresentation();
            logger.info("[Document] ClassCode: '{}' NodeRepresentation: '{}'", classCodeNode, nodeRepresentation);
            //TODO: CDA as input needs to be validated according XSD, Schematron or Validators.
            XdrResponse response;
            switch (classCodeNode) {

                // call XDR Client for Consent
                case Constants.CONSENT_CLASSCODE:
                    response = ConsentService.put(document, patientDemographics, countryCode, assertionMap);
                    break;
                // call XDR Client for eP
                case Constants.ED_CLASSCODE:
                    if (StringUtils.equals(nodeRepresentation, "urn:eHDSI:ed:discard:2020")) {
                        response = DispensationService.discard(document, patientDemographics, countryCode, assertionMap);
                    } else {
                        response = DispensationService.initialize(document, patientDemographics, countryCode, assertionMap);
                    }
                    break;
                // call XDR Client for HCER
                case Constants.HCER_CLASSCODE:
                    response = HcerService.submit(document, patientDemographics, countryCode, assertionMap);
                    break;
                case Constants.EDD_CLASSCODE:
                    response = DispensationService.discard(document, patientDemographics, countryCode, assertionMap);
                    break;
                default:
                    throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + classCodeNode);
            }

            result.setSubmitDocumentResponse(SubmitDocumentResponseDts.newInstance(response));

        } catch (RuntimeException ex) {
            LoggingSlf4j.error(logger, methodName, ex);
            throw ex;
        }
        LoggingSlf4j.end(logger, methodName);
        return result;
    }

    /**
     * Greets someone by saying hello. This is an auxiliary operation for diagnosis purposes.
     *
     * @param sayHello axis wrapper for element: <code>sayHello</code>. This encapsulates a <code>String</code>.
     * @return a text in the format: Hello + <code>sayHello</code>.
     */
    @Override
    public SayHelloResponseDocument sayHello(SayHelloDocument sayHello) {

        final var methodName = "sayHello";
        LoggingSlf4j.start(logger, methodName);

        var sayHelloResponseDocument = SayHelloResponseDocument.Factory.newInstance();
        var sayHelloResponse = SayHelloResponse.Factory.newInstance();
        sayHelloResponse.setReturn("Hello " + sayHello.getSayHello().getArg0());

        sayHelloResponseDocument.setSayHelloResponse(sayHelloResponse);

        LoggingSlf4j.end(logger, methodName);
        return sayHelloResponseDocument;
    }
}
