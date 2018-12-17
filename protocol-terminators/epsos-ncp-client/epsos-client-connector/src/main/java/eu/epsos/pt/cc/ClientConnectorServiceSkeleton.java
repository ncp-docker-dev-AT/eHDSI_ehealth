package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.exceptions.XdrException;
import eu.epsos.pt.cc.dts.axis2.*;
import eu.epsos.pt.cc.stub.*;
import eu.epsos.util.IheConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.spms.epsos.utils.logging.LoggingSlf4j;
import tr.com.srdc.epsos.data.model.XdrResponse;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;
import tr.com.srdc.epsos.util.Constants;

import java.text.ParseException;
import java.util.List;

/**
 * ClientConnectorServiceSkeleton java skeleton for the axisService.
 * <p>
 * This class implements the contact point into the NCP-B, allowing the Portal-B to contact and perform requests in NCP-B.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class ClientConnectorServiceSkeleton implements ClientConnectorServiceSkeletonInterface {

    private final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceSkeleton.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static final String UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION = "Unsupported Class Code scheme: ";
    private static final String UNSUPPORTED_CLASS_CODE_EXCEPTION = "Unsupported Class Code: ";

    /*
     * XCPD
     */

    /**
     * Performs international search for a patient, filtering by a set of demographics.
     * This method is an adapter for usage of a XCPD client.
     *
     * @param queryPatient axis wrapper for element: <code>queryPatient</code>.
     *                     This encapsulates, destination Country Code and Patient's demographics.
     * @return a QueryPatientResponseDocument containing the query response(s).
     * @throws ParseException Exception thrown while the Payload cannot be parsed.
     */
    @Override
    public QueryPatientResponseDocument queryPatient(final QueryPatientDocument queryPatient, Assertion assertion)
            throws NoPatientIdDiscoveredException, ParseException {

        final String methodName = "queryPatient";
        LoggingSlf4j.start(logger, methodName);

        QueryPatientResponseDocument result = QueryPatientResponseDocument.Factory.newInstance();

        try {
            /* create request */
            List<tr.com.srdc.epsos.data.model.PatientDemographics> xcpdResp;

            tr.com.srdc.epsos.data.model.PatientDemographics request;
            QueryPatientRequest arg0 = queryPatient.getQueryPatient().getArg0();
            PatientDemographics pDemographic = arg0.getPatientDemographics();
            request = eu.epsos.pt.cc.dts.PatientDemographicsDts.newInstance(pDemographic);
            if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                loggerClinical.info("Patient Demographics: '{}', '{}', '{}'",
                        ((pDemographic.getPatientIdArray() == null) ? "N/A" : pDemographic.getPatientIdArray()[0]),
                        pDemographic.getBirthDate(), pDemographic.getGivenName());
                loggerClinical.info("Patient Demographics Request: '{}', '{}', '{}'", request.getId(), request.getGivenName(),
                        request.getBirthDate());
            }
            String countryCode = arg0.getCountryCode();

            // call XCPD Client
            xcpdResp = IdentificationService.findIdentityByTraits(request, assertion, countryCode);

            /* result */
            QueryPatientResponse response;
            List<PatientDemographics> aux;
            aux = eu.epsos.pt.cc.dts.axis2.PatientDemographicsDts.newInstance(xcpdResp);
            response = QueryPatientResponseDts.newInstance(aux);

            result.setQueryPatientResponse(response);

        } catch (ClientConnectorException ex) {
            LoggingSlf4j.error(logger, methodName);
            throw ex;
        }

        LoggingSlf4j.end(logger, methodName);
        return result;
    }

    /*
     * XCA
     */

    /**
     * Performs international search for documents.
     * Filtering by patient and document code.
     * This method is an adapter for the usage of a XCA client.
     *
     * @param queryDocuments axis wrapper for * * * * * * *
     *                       element: <code>queryDocuments</code>. This encapsulates, destination
     *                       Country Code, patient's identification and documents class code.
     * @return a QueryDocumentsResponseDocument containing the query
     * response(s).
     */
    @Override
    public QueryDocumentsResponseDocument queryDocuments(QueryDocumentsDocument queryDocuments, Assertion hcpAssertion,
                                                         Assertion trcAssertion) throws XCAException {

        final String methodName = "queryDocuments";
        LoggingSlf4j.start(logger, methodName);

        QueryDocumentsResponse result = QueryDocumentsResponse.Factory.newInstance();

        /* retrieve data from parameters */
        QueryDocuments queryDocuments1 = queryDocuments.getQueryDocuments();
        QueryDocumentRequest queryDocumentRequest = queryDocuments1.getArg0();
        String countryCode = queryDocumentRequest.getCountryCode();


        PatientId tmp = queryDocumentRequest.getPatientId();
        tr.com.srdc.epsos.data.model.PatientId patientId = eu.epsos.pt.cc.dts.PatientIdDts.newInstance(tmp);

        GenericDocumentCode tmpCode = queryDocumentRequest.getClassCode();
        tr.com.srdc.epsos.data.model.GenericDocumentCode documentCode = eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(tmpCode);

        if (!documentCode.getSchema().equals(IheConstants.ClASSCODE_SCHEME)) {
            throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + documentCode.getSchema());
        }

        /* perform the call */
        try {
            QueryResponse response;

            switch (documentCode.getValue()) {
                case Constants.PS_CLASSCODE:
                    response = PatientService.list(patientId, countryCode, documentCode, hcpAssertion, trcAssertion);
                    break;
                case Constants.EP_CLASSCODE:
                    response = OrderService.list(patientId, countryCode, documentCode, hcpAssertion, trcAssertion);
                    break;
                case Constants.MRO_CLASSCODE:
                    response = MroService.list(patientId, countryCode, documentCode, hcpAssertion, trcAssertion);
                    break;
                default:
                    throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + documentCode.getValue());
            }

            if (response.getDocumentAssociations() != null && !response.getDocumentAssociations().isEmpty()) {
                result.setReturnArray(DocumentDts.newInstance(response.getDocumentAssociations()));
            }

        } catch (RuntimeException ex) {
            LoggingSlf4j.error(logger, methodName);
            throw ex;
        }


        /* create return wrapper */
        QueryDocumentsResponseDocument wapper = QueryDocumentsResponseDocument.Factory.newInstance();
        wapper.setQueryDocumentsResponse(result);

        LoggingSlf4j.end(logger, methodName);
        return wapper;
    }

    /**
     * Performs international search for documents. Filtering by patient and document code.
     * This method is an adapter for usage of a XCA client.
     * <p>
     * It makes use of the XCA Service Client library.
     *
     * @param retrieveDocument axis wrapper for * * * * * * *
     *                         element: <code>retrieveDocument</code>. This encapsulates, destination
     *                         Country Code, patient's identification and document's identification.
     * @return the retrieved document.
     */
    @Override
    public RetrieveDocumentResponseDocument retrieveDocument(RetrieveDocumentDocument1 retrieveDocument,
                                                             Assertion hcpAssertion, Assertion trcAssertion)
            throws XCAException {
        /*
         * Setup
         */
        final String methodName = "retrieveDocument";
        LoggingSlf4j.start(logger, methodName);


        RetrieveDocumentResponse result;
        /*
         * Body
         */
        RetrieveDocument1 retrieveDocument1 = retrieveDocument.getRetrieveDocument();
        RetrieveDocumentRequest arg0 = retrieveDocument1.getArg0();
        String countryCode = arg0.getCountryCode();
        DocumentId xdsDocument = arg0.getDocumentId();
        String homeCommunityId = arg0.getHomeCommunityId();
        String targetLanguage = arg0.getTargetLanguage();

        GenericDocumentCode tmpCode = arg0.getClassCode();
        tr.com.srdc.epsos.data.model.GenericDocumentCode documentCode = eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(tmpCode);

        if (!documentCode.getSchema().equals(IheConstants.ClASSCODE_SCHEME)) {
            throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + documentCode.getSchema());
        }

        try {
            DocumentResponse response;
            XDSDocument request = XdsDocumentDts.newInstance(xdsDocument);
            request.setClassCode(documentCode);

            logger.info("[ClientConnector retrieveDocument()] Document: '{}' homeCommunityId: '{}' targetLanguage: '{}'",
                    request.getDocumentUniqueId(), homeCommunityId, targetLanguage);
            switch (documentCode.getValue()) {
                case Constants.PS_CLASSCODE:
                    response = PatientService.retrieve(request, homeCommunityId, countryCode, targetLanguage,
                            hcpAssertion, trcAssertion);
                    break;
                case Constants.EP_CLASSCODE:
                    response = OrderService.retrieve(request, homeCommunityId, countryCode, targetLanguage,
                            hcpAssertion, trcAssertion);
                    break;
                case Constants.MRO_CLASSCODE:
                    response = MroService.retrieve(request, homeCommunityId, countryCode, targetLanguage,
                            hcpAssertion, trcAssertion);
                    break;
                default:
                    throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + documentCode.getValue());
            }

            result = RetrieveDocumentResponseDTS.newInstance(response);

        } catch (ClientConnectorException ex) {
            LoggingSlf4j.error(logger, methodName);
            throw ex;
        }

        /* create return wrapper */
        RetrieveDocumentResponseDocument wrapper = RetrieveDocumentResponseDocument.Factory.newInstance();
        wrapper.setRetrieveDocumentResponse(result);
        LoggingSlf4j.end(logger, methodName);
        return wrapper;
    }

    /*
     * XDR
     */

    /**
     * Submits a document to a foreign country. This method is an adapter for usage of a XDR client.
     * <p>
     * This method makes use of the XDR Service Client library.
     *
     * @param submitDocument axis wrapper for * * * * * * *
     *                       element: <code>submitDocument</code>. This encapsulates, destination
     *                       Country Code and the document to submit with some Metadata.
     * @return a SubmitDocumentResponseDocument object.
     * @throws ParseException Exception thrown while the Payload cannot be parsed.
     */
    @Override
    public SubmitDocumentResponseDocument submitDocument(final SubmitDocumentDocument1 submitDocument,
                                                         Assertion hcpAssertion, Assertion trcAssertion)
            throws XdrException, ParseException {

        final String methodName = "submitDocument";
        LoggingSlf4j.start(logger, methodName);

        SubmitDocumentResponseDocument result = SubmitDocumentResponseDocument.Factory.newInstance();

        try {

            XdrResponse response;

            /*  create Xdr request */
            SubmitDocument1 submitDocument1 = submitDocument.getSubmitDocument();
            SubmitDocumentRequest arg0 = submitDocument1.getArg0();
            String countryCode = arg0.getCountryCode();
            EpsosDocument1 document = arg0.getDocument();
            PatientDemographics patient = arg0.getPatientDemographics();

            String classCodeNode;
            GenericDocumentCode classCode = document.getClassCode();
            if (!classCode.getSchema().equals(IheConstants.ClASSCODE_SCHEME)) {
                throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_SCHEME_EXCEPTION + classCode.getSchema());
            }
            classCodeNode = classCode.getNodeRepresentation();

            switch (classCodeNode) {

                // call XDR Client for Consent
                case Constants.CONSENT_CLASSCODE:
                    response = ConsentService.put(document, patient, countryCode, hcpAssertion, trcAssertion);
                    break;
                // call XDR Client for eP
                case Constants.ED_CLASSCODE:
                    response = DispensationService.initialize(document, patient, countryCode, hcpAssertion, trcAssertion);
                    break;
                // call XDR Client for HCER
                case Constants.HCER_CLASSCODE:
                    response = HcerService.submit(document, patient, countryCode, hcpAssertion, trcAssertion);
                    break;
                default:
                    throw new ClientConnectorException(UNSUPPORTED_CLASS_CODE_EXCEPTION + classCodeNode);
            }

            result.setSubmitDocumentResponse(SubmitDocumentResponseDts.newInstance(response));

        } catch (RuntimeException ex) {
            LoggingSlf4j.error(logger, methodName);
            throw ex;
        }

        LoggingSlf4j.end(logger, methodName);
        return result;
    }

    /**
     * Greets someone by saying hello. This is an auxiliary operation for
     * diagnosis purposes.
     *
     * @param sayHello axis wrapper for element: <code>sayHello</code>. This
     *                 encapsulates a <code>String</code>.
     * @return a text in the format: Hello + <code>sayHello</code>.
     */
    @Override
    public SayHelloResponseDocument sayHello(SayHelloDocument sayHello) {

        final String methodName = "sayHello";
        LoggingSlf4j.start(logger, methodName);

        SayHelloResponseDocument result = SayHelloResponseDocument.Factory.newInstance();

        SayHelloResponse resp = SayHelloResponse.Factory.newInstance();
        resp.setReturn("Hello " + sayHello.getSayHello().getArg0());

        result.setSayHelloResponse(resp);

        LoggingSlf4j.end(logger, methodName);
        return result;
    }
}
