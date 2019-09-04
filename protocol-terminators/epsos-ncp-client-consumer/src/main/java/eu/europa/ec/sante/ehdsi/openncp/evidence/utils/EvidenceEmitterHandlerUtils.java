package eu.europa.ec.sante.ehdsi.openncp.evidence.utils;

import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.util.XMLUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.Helper;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

import java.util.*;

/**
 * Ancillary methods to the EvidenceEmitter class supporting In-Out flows in the Portal
 *
 * @author jgoncalves
 */
public class EvidenceEmitterHandlerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceEmitterHandlerUtils.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static final String CLIENT_CONNECTOR_XML_NAMESPACE = "http://clientconnector.protocolterminator.openncp.epsos/";
    private static final String CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST = "submitDocument";
    private static final String CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE = "submitDocumentResponse";
    private static final String CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST = "queryPatient";
    private static final String CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE = "queryPatientResponse";
    private static final String CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST = "queryDocuments";
    private static final String CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE = "queryDocumentsResponse";
    private static final String CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST = "retrieveDocument";
    private static final String CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE = "retrieveDocumentResponse";

    private static final List<String> clientConnectorOperations;
    // maps the message type to its related ad-hoc event
    private static final Map<String, String> events;
    // maps the message type to the ad-hoc transaction name to be placed in the evidence filename
    private static final Map<String, String> transactionNames;

    static {

        List<String> list = new ArrayList<>();
        list.add(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST);
        list.add(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE);
        list.add(CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST);
        list.add(CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE);
        list.add(CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST);
        list.add(CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE);
        list.add(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST);
        list.add(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE);
        clientConnectorOperations = Collections.unmodifiableList(list);
    }

    static {

        Map<String, String> map = new HashMap<>();
        // Portal-NCP interactions
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST, "NI_XDR_REQ");
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE, "NI_XDR_RES");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST, "NI_PD_REQ");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE, "NI_PD_RES");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST, "NI_DQ_REQ");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE, "NI_DQ_RES");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST, "NI_DR_REQ");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE, "NI_DR_RES");
        events = Collections.unmodifiableMap(map);
    }

    static {

        Map<String, String> map = new HashMap<>();
        // Portal-NCP interactions
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST, "NI_XDR_REQ_SENT");
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE, "NI_XDR_RES_RECEIVED");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST, "NI_PD_REQ_SENT");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE, "NI_PD_RES_RECEIVED");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST, "NI_DQ_REQ_SENT");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE, "NI_DQ_RES_RECEIVED");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST, "NI_DR_REQ_SENT");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE, "NI_DR_RES_RECEIVED");
        transactionNames = Collections.unmodifiableMap(map);
    }

    public EvidenceEmitterHandlerUtils() {
    }

    public String getEventTypeFromMessage(SOAPBody soapBody) {

        String messageElement = soapBody.getFirstElementLocalName();
        LOGGER.debug("Message body element: '{}'", messageElement);
        return events.get(messageElement);
    }

    public String getTransactionNameFromMessage(SOAPBody soapBody) {

        String messageElement = soapBody.getFirstElementLocalName();
        LOGGER.debug("Message body element: '{}'", messageElement);
        return transactionNames.get(messageElement);
    }

    private boolean isClientConnectorOperation(String operation) {
        return clientConnectorOperations.contains(operation);
    }

    public String getMsgUUID(SOAPHeader soapHeader, SOAPBody soapBody) throws Exception {

        String msguuid = null;
        Element elemSoapHeader = XMLUtils.toDOM(soapHeader);
        String operation = soapBody.getFirstElementLocalName();
        if (isClientConnectorOperation(operation)) {
            // we're in a Portal-NCPB interaction
            Assertion identityAssertion = Helper.getHCPAssertion(elemSoapHeader);
            Assertion trca = Helper.getTRCAssertion(elemSoapHeader);
            if (identityAssertion != null && trca == null) {
                // this is a XCPD request from Portal to NCP-B, we don't yet have the TRCA
                msguuid = identityAssertion.getID();
            } else if (identityAssertion != null && trca != null) {
                // this is a XCA Query or Retrieve from Portal to NCP-B, we already have the TRCA
                msguuid = trca.getID();
            } else {
                //response to Portal doesn't have IdA, only the request from the Portal has it
                // we don't have the IdA nor SOAP message ID, so we generate one UUID
                msguuid = Constants.UUID_PREFIX + UUID.randomUUID().toString();
            }
        }
        return msguuid;
    }

    public Document canonicalizeAxiomSoapEnvelope(SOAPEnvelope env) throws Exception {

        Element envAsDom = XMLUtils.toDOM(env);
        Document envCanonicalized = XMLUtil.canonicalize(envAsDom.getOwnerDocument());
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && LOGGER_CLINICAL.isDebugEnabled()) {
            LOGGER_CLINICAL.debug("Pretty printing canonicalized:\n{}", XMLUtil.prettyPrint(envCanonicalized));
        }
        return envCanonicalized;
    }
}
