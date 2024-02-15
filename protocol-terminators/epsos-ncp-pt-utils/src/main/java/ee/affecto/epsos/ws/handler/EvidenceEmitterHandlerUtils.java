package ee.affecto.epsos.ws.handler;

import epsos.ccd.gnomon.auditmanager.EventType;
import eu.epsos.util.xca.XCAConstants;
import eu.epsos.util.xcpd.XCPDConstants;
import eu.epsos.util.xdr.XDRConstants;
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
 * Ancillary methods to the In/Out-FlowEvidenceEmitter classes
 *
 * @author jgoncalves
 */
public class EvidenceEmitterHandlerUtils {

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
    // maps the message type to its related IHE event
    private static final Map<String, String> iheEvents;
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
        // ITI-55
        map.put(XCPDConstants.PATIENT_DISCOVERY_REQUEST, EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getIheCode());
        map.put(XCPDConstants.PATIENT_DISCOVERY_RESPONSE, EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getIheCode());

        // ITI-38: same for PS or eP List
        map.put(XCAConstants.ADHOC_QUERY_REQUEST, EventType.PATIENT_SERVICE_LIST.getIheCode());
        map.put(XCAConstants.ADHOC_QUERY_RESPONSE, EventType.PATIENT_SERVICE_LIST.getIheCode());

        // ITI-39: same for PS or eP Retrieve
        map.put(XCAConstants.RETRIEVE_DOCUMENT_SET_REQUEST, EventType.PATIENT_SERVICE_RETRIEVE.getIheCode());
        map.put(XCAConstants.RETRIEVE_DOCUMENT_SET_RESPONSE, EventType.PATIENT_SERVICE_RETRIEVE.getIheCode());

        // ITI-41: same for Dispensation Initialize/Discard
        map.put(XDRConstants.PROVIDE_AND_REGISTER_DOCUMENT_SET_REQ_STR, EventType.DISPENSATION_SERVICE_INITIALIZE.getIheCode());

        // ITI-41: same for Dispensation Initialize/Discard and Consent Put/Discard

        map.put(XDRConstants.REGISTRY_RESPONSE_STR, EventType.DISPENSATION_SERVICE_INITIALIZE.getIheCode());

        // Portal-NCP interactions
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST, "PORTAL_PD_REQ");
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE, "NCPB_XDR_RES");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST, "PORTAL_PD_REQ");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE, "NCPB_PD_RES");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST, "PORTAL_DQ_REQ");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE, "NCPB_DQ_RES");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST, "PORTAL_DR_REQ");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE, "NCPB_DR_RES");
        iheEvents = Collections.unmodifiableMap(map);
    }

    static {

        Map<String, String> map = new HashMap<>();
        map.put(XCPDConstants.PATIENT_DISCOVERY_REQUEST, "XCPD_REQ");
        map.put(XCPDConstants.PATIENT_DISCOVERY_RESPONSE, "XCPD_RES");
        map.put(XCAConstants.ADHOC_QUERY_REQUEST, "XCA_LIST_REQ");
        map.put(XCAConstants.ADHOC_QUERY_RESPONSE, "XCA_LIST_RES");
        map.put(XCAConstants.RETRIEVE_DOCUMENT_SET_REQUEST, "XCA_RETRIEVE_REQ");
        map.put(XCAConstants.RETRIEVE_DOCUMENT_SET_RESPONSE, "XCA_RETRIEVE_RES");
        map.put(XDRConstants.PROVIDE_AND_REGISTER_DOCUMENT_SET_REQ_STR, "XDR_SUBMIT_REQ");
        map.put(XDRConstants.DOC_RCP_PRVDANDRGSTDOCSETB_STR, "XDR_SUBMIT_REQ");
        map.put(XDRConstants.REGISTRY_RESPONSE_STR, "XDR_SUBMIT_RES");
        // Portal-NCP interactions
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_REQUEST, "PORTAL_XDR_REQ_RECEIVED");
        map.put(CLIENT_CONNECTOR_SUBMIT_DOCUMENT_RESPONSE, "NCPB_XDR_RES_SENT");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_REQUEST, "PORTAL_PD_REQ_RECEIVED");
        map.put(CLIENT_CONNECTOR_QUERY_PATIENT_RESPONSE, "NCPB_PD_RES_SENT");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_REQUEST, "PORTAL_DQ_REQ_RECEIVED");
        map.put(CLIENT_CONNECTOR_QUERY_DOCUMENTS_RESPONSE, "NCPB_DQ_RES_SENT");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_REQUEST, "PORTAL_DR_REQ_RECEIVED");
        map.put(CLIENT_CONNECTOR_RETRIEVE_DOCUMENT_RESPONSE, "NCPB_DR_RES_SENT");
        transactionNames = Collections.unmodifiableMap(map);
    }

    private final Logger logger = LoggerFactory.getLogger(EvidenceEmitterHandlerUtils.class);

    public EvidenceEmitterHandlerUtils() {
    }

    public String getEventTypeFromMessage(SOAPBody soapBody) {

        String messageElement = soapBody.getFirstElementLocalName();
        logger.debug("Message body element: '{}'", messageElement);
        return iheEvents.get(messageElement);
    }

    public String getTransactionNameFromMessage(SOAPBody soapBody) {

        String messageElement = soapBody.getFirstElementLocalName();
        logger.debug("Message body element: '{}'", messageElement);
        return transactionNames.get(messageElement);
    }

    public String getServerSideTitle(SOAPBody soapBody) {

        String operation = soapBody.getFirstElementLocalName();
        String title = transactionNames.get(operation);
        if (!this.isClientConnectorOperation(operation)) {
            title = "NCPA_" + title;
        }
        return title;
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
        return XMLUtil.canonicalize(envAsDom.getOwnerDocument());
    }
}
