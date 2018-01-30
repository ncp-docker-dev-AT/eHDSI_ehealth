package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.exceptions.XdrException;
import org.opensaml.saml2.core.Assertion;

import java.text.ParseException;

/**
 * ClientConnectorServiceSkeletonInterface java skeleton interface for the Axis Service
 * <p>
 * This Interface represents the contact point into the NCP-B, allowing the Portal-B to contact and perform requests in NCP-B.
 */
public interface ClientConnectorServiceSkeletonInterface {

    /*
     * XCPD
     */

    /**
     * Specifies the signature of the operation responsible for patient
     * querying. It receives some demographic data to perform the query.
     *
     * @param queryPatient represents the query object.
     * @return a QueryPatientResponseDocument containing the query response(s).
     * @see QueryPatientResponseDocument
     * @see QueryPatientDocument
     */
    QueryPatientResponseDocument queryPatient(QueryPatientDocument queryPatient, Assertion hcpAssertion)
            throws NoPatientIdDiscoveredException, ParseException;

    /*
     * XCA
     */

    /**
     * Specifies the signature of the operation responsible for document querying, receiving as parameter
     * the required query object.
     *
     * @param queryDocuments represents the query object.
     * @return a QueryDocumentsResponseDocument containing the query
     * response(s).
     * @see QueryDocumentsResponseDocument
     * @see QueryDocumentsDocument
     */
    QueryDocumentsResponseDocument queryDocuments(QueryDocumentsDocument queryDocuments, Assertion hcpAssertion, Assertion trcAssertion) throws XCAException;

    /**
     * Specifies the signature of the operation responsible for document retrieval, receiving the specific documents
     * to retrieve as parameters.
     *
     * @param retrieveDocument the specific document to retrieve.
     * @return the retrieved document.
     * @see RetrieveDocumentResponseDocument
     * @see RetrieveDocumentDocument1
     */
    RetrieveDocumentResponseDocument retrieveDocument(RetrieveDocumentDocument1 retrieveDocument, Assertion hcpAssertion, Assertion trcAssertion) throws XCAException;

    /*
     * XDR
     */

    /**
     * Specifies the signature of the operation responsible for document submitting, accepting the documents to submit
     * as parameter.
     *
     * @param submitDocument the document to submit.
     * @return a SubmitDocumentResponseDocument object.
     * @see SubmitDocumentResponseDocument
     * @see SubmitDocumentDocument1
     */
    SubmitDocumentResponseDocument submitDocument(SubmitDocumentDocument1 submitDocument, Assertion hcpAssertion, Assertion trcAssertion) throws XdrException, ParseException;

    /*
     * Auxiliar
     */

    /**
     * This is a test method signature.
     *
     * @param sayHello a sayHello document.
     * @return a test response.
     */
    SayHelloResponseDocument sayHello(SayHelloDocument sayHello);
}
