package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.exceptions.XDRException;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML2Validator;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import eu.europa.ec.sante.openncp.protocolterminator.commons.AssertionEnum;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.xmlbeans.XmlBeansXMLReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import java.text.ParseException;
import java.util.*;

/**
 * ClientConnectorServiceServiceMessageReceiverInOut message receiver.
 */
public class ClientConnectorServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static final String LOG_INCOMING_REQUEST = "Incoming '{}' request message from portal:\n{}";
    private static final String LOG_OUTGOING_REQUEST = "Outgoing '{}' response message to portal:\n{}";

    static {
        org.apache.xml.security.Init.init();
    }

    private final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceMessageReceiverInOut.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Override
    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newMsgContext) throws AxisFault {

        var requestSoapEnvelope = msgContext.getEnvelope();
        String operationName = msgContext.getOperationContext().getOperationName();

        //  Log soap request.
        logSoapMessage(operationName, requestSoapEnvelope, LOG_INCOMING_REQUEST);

        try {

            /* Out Envelop */
            SOAPEnvelope responseSoapEnvelope;

            /* Find the axisOperation that has been set by the Dispatch phase. */
            var axisOperation = msgContext.getOperationContext().getAxisOperation();
            logger.info("[ClientConnector] Axis Operation: '{}' - Target Namespace: {}' - WSAddressing Action: '{}'",
                    msgContext.getOperationContext().getAxisOperation().getName().getLocalPart(),
                    msgContext.getOperationContext().getAxisOperation().getName().getNamespaceURI(),
                    msgContext.getOptions().getAction());
            if (axisOperation == null) {
                throw new AxisFault("Operation is not located; if this is DocLit style, the SOAP-ACTION should " +
                        "specified via the SOAP Action to use the RawXMLProvider");
            }

            String methodName;
            if ((axisOperation.getName() != null) && ((methodName = JavaUtils.xmlNameToJavaIdentifier(axisOperation.getName().getLocalPart())) != null)) {

                Element soapHeader = XMLUtils.toDOM(requestSoapEnvelope.getHeader());
                List<Assertion> assertions = SAML2Validator.getAssertions(soapHeader);

                //  Submit Document
                if (StringUtils.equals(ClientConnectorOperation.SERVICE_SUBMIT_DOCUMENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    responseSoapEnvelope = processSubmitDocumentOperation(msgContext, requestSoapEnvelope, assertions);
                }
                //  Query Patient
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_QUERY_PATIENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    responseSoapEnvelope = processQueryPatientOperation(msgContext, requestSoapEnvelope, assertions);
                }
                // Query Documents
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_QUERY_DOCUMENTS, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    responseSoapEnvelope = processQueryDocumentsOperation(msgContext, requestSoapEnvelope, assertions);
                }
                //  Retrieve Document
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_RETRIEVE_DOCUMENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    responseSoapEnvelope = processRetrieveDocumentOperation(msgContext, requestSoapEnvelope, assertions);
                }
                // Say hello
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_TEST_SAY_HELLO, methodName)) {

                    responseSoapEnvelope = processTestOperation(msgContext, requestSoapEnvelope);
                } else {
                    // Else Method not Available
                    throw new ClientConnectorException("Client Connector Error: Method not found");
                }

                //  Log SOAP response
                logSoapMessage(operationName, responseSoapEnvelope, LOG_OUTGOING_REQUEST);
                // Soap message: HTTP header set.
                String randomUUID = Constants.UUID_PREFIX + UUID.randomUUID();
                newMsgContext.setEnvelope(responseSoapEnvelope);
                newMsgContext.getOptions().setMessageId(randomUUID);
            }
        } catch (Exception e) {

            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final XmlObject param) throws AxisFault {

        var xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new SAXSource(new XmlBeansXMLReader(param, xmlOptions), new InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, XmlObject param) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    public XmlObject fromOM(OMElement param, Class type, Map extraNamespaces) throws AxisFault {

        try {

            if (SubmitDocumentDocument1.class.equals(type)) {

                if (extraNamespaces != null) {
                    return SubmitDocumentDocument1.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SubmitDocumentDocument1.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SubmitDocumentResponseDocument.class.equals(type)) {

                if (extraNamespaces != null) {
                    return SubmitDocumentResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SubmitDocumentResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryPatientDocument.class.equals(type)) {

                if (extraNamespaces != null) {
                    return QueryPatientDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryPatientDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryPatientResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryPatientResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryPatientResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SayHelloDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    XMLStreamReader xmlStreamReaderWithoutCaching = param.getXMLStreamReaderWithoutCaching();
                    XmlOptions setLoadAdditionalNamespaces = new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces);
                    return SayHelloDocument.Factory.parse(xmlStreamReaderWithoutCaching, setLoadAdditionalNamespaces);

                } else {
                    return SayHelloDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SayHelloResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return SayHelloResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SayHelloResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryDocumentsDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryDocumentsDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryDocumentsDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryDocumentsResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryDocumentsResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryDocumentsResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (RetrieveDocumentDocument1.class.equals(type)) {
                if (extraNamespaces != null) {
                    return RetrieveDocumentDocument1.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return RetrieveDocumentDocument1.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (RetrieveDocumentResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return RetrieveDocumentResponseDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return RetrieveDocumentResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
        return null;
    }

    private ClientConnectorServiceSkeletonInterface getClientConnectorServiceSkeleton(MessageContext messageContext) throws AxisFault {

        // Retrieving the implementation class of the Web Service.
        var implementationObject = getTheImplementationObject(messageContext);
        return (ClientConnectorServiceSkeletonInterface) implementationObject;
    }

    /**
     * A utility method that copies the namespaces from the SOAPEnvelope.
     */
    private Map<String, String> getEnvelopeNamespaces(SOAPEnvelope env) {

        Map<String, String> returnMap = new HashMap<>();
        var namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace ns = (OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private void logSoapMessage(String operationName, SOAPEnvelope soapEnvelope, String message) {

        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            try {
                String logRequestMsg = XMLUtil.prettyPrint(XMLUtils.toDOM(soapEnvelope));
                loggerClinical.debug(message, operationName, logRequestMsg);

            } catch (Exception ex) {
                logger.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private Map<AssertionEnum, Assertion> processAssertionList(List<Assertion> assertionList) {

        logger.info("[ClientConnector] Processing Assertions list from SOAP Header:");
        Map<AssertionEnum, Assertion> assertionEnumMap = new EnumMap<>(AssertionEnum.class);
        for (Assertion assertion : assertionList) {
            if (StringUtils.equals(assertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:hcp")) {
                assertionEnumMap.put(AssertionEnum.CLINICIAN, assertion);
            } else if (StringUtils.equals(assertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:nok")) {
                assertionEnumMap.put(AssertionEnum.NEXT_OF_KIN, assertion);
            } else if (StringUtils.equals(assertion.getIssuer().getNameQualifier(), "urn:ehdsi:assertions:trc")) {
                assertionEnumMap.put(AssertionEnum.TREATMENT, assertion);
            }
        }
        return assertionEnumMap;
    }

    private SOAPEnvelope processQueryDocumentsOperation(MessageContext messageContext, SOAPEnvelope requestEnvelope,
                                                        List<Assertion> assertions) throws AxisFault, XCAException {

        logger.info("[ClientConnector] Process Query Documents:");
        Map<AssertionEnum, Assertion> assertionMap = processAssertionList(assertions);
        if (OpenNCPValidation.isValidationEnable()) {
            OpenNCPValidation.validateHCPAssertion(assertionMap.get(AssertionEnum.CLINICIAN), NcpSide.NCP_B);
        }

        QueryDocumentsDocument wrappedParam = (QueryDocumentsDocument) fromOM(requestEnvelope.getBody().getFirstElement(),
                QueryDocumentsDocument.class, getEnvelopeNamespaces(requestEnvelope));
        var queryDocumentsResponseDocument = getClientConnectorServiceSkeleton(messageContext)
                .queryDocuments(wrappedParam, assertionMap);

        return toEnvelope(getSOAPFactory(messageContext), queryDocumentsResponseDocument);
    }

    private SOAPEnvelope processQueryPatientOperation(MessageContext messageContext, SOAPEnvelope requestEnvelope,
                                                      List<Assertion> assertions)
            throws AxisFault, ParseException, NoPatientIdDiscoveredException {

        Map<AssertionEnum, Assertion> assertionMap = processAssertionList(assertions);
        if (OpenNCPValidation.isValidationEnable()) {
            OpenNCPValidation.validateHCPAssertion(assertionMap.get(AssertionEnum.CLINICIAN), NcpSide.NCP_B);
        }

        var queryPatientDocument = (QueryPatientDocument) fromOM(requestEnvelope.getBody().getFirstElement(),
                QueryPatientDocument.class, getEnvelopeNamespaces(requestEnvelope));
        var queryPatientResponseDocument = getClientConnectorServiceSkeleton(messageContext)
                .queryPatient(queryPatientDocument, assertionMap);
        return toEnvelope(getSOAPFactory(messageContext), queryPatientResponseDocument);
    }

    private SOAPEnvelope processRetrieveDocumentOperation(MessageContext messageContext, SOAPEnvelope requestEnvelope,
                                                          List<Assertion> assertions) throws AxisFault, XCAException {

        Map<AssertionEnum, Assertion> assertionMap = processAssertionList(assertions);
        if (OpenNCPValidation.isValidationEnable()) {
            OpenNCPValidation.validateHCPAssertion(assertionMap.get(AssertionEnum.CLINICIAN), NcpSide.NCP_B);
        }

        var retrieveDocumentDocument1 = (RetrieveDocumentDocument1) fromOM(requestEnvelope.getBody().getFirstElement(),
                RetrieveDocumentDocument1.class, getEnvelopeNamespaces(requestEnvelope));
        var retrieveDocumentResponseDocument = getClientConnectorServiceSkeleton(messageContext)
                .retrieveDocument(retrieveDocumentDocument1, assertionMap);

        return toEnvelope(getSOAPFactory(messageContext), retrieveDocumentResponseDocument);
    }

    private SOAPEnvelope processSubmitDocumentOperation(MessageContext messageContext, SOAPEnvelope requestEnvelope,
                                                        List<Assertion> assertions) throws AxisFault, XDRException, ParseException {

        Map<AssertionEnum, Assertion> assertionMap = processAssertionList(assertions);
//        if (OpenNCPValidation.isValidationEnable()) {
//            OpenNCPValidation.validateHCPAssertion(assertionMap.get(AssertionEnum.CLINICIAN), NcpSide.NCP_B);
//        }

        SubmitDocumentDocument1 wrappedParam = (SubmitDocumentDocument1) fromOM(requestEnvelope.getBody().getFirstElement(),
                SubmitDocumentDocument1.class, getEnvelopeNamespaces(requestEnvelope));
        var submitDocumentResponseDocument = getClientConnectorServiceSkeleton(messageContext)
                .submitDocument(wrappedParam, assertionMap);

        return toEnvelope(getSOAPFactory(messageContext), submitDocumentResponseDocument);
    }

    private SOAPEnvelope processTestOperation(MessageContext messageContext, SOAPEnvelope requestEnvelope) throws AxisFault {

        SayHelloDocument wrappedParam = (SayHelloDocument) fromOM(requestEnvelope.getBody().getFirstElement(),
                SayHelloDocument.class, getEnvelopeNamespaces(requestEnvelope));
        var sayHelloResponseDocument = getClientConnectorServiceSkeleton(messageContext).sayHello(wrappedParam);

        return toEnvelope(getSOAPFactory(messageContext), sayHelloResponseDocument);
    }
}
