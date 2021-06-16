package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML2Validator;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * ClientConnectorServiceServiceMessageReceiverInOut message receiver.
 */
public class ClientConnectorServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    static {
        org.apache.xml.security.Init.init();
    }

    private final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceMessageReceiverInOut.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Override
    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newMsgContext) throws AxisFault {

        SOAPEnvelope reqEnv = msgContext.getEnvelope();
        String operationName = msgContext.getOperationContext().getOperationName();

        /*
         * Log soap request
         */
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            try {
                String logRequestMsg = XMLUtil.prettyPrint(XMLUtils.toDOM(reqEnv));
                loggerClinical.debug("Incoming '{}' request message from portal:\n{}", operationName, logRequestMsg);

            } catch (Exception ex) {
                logger.debug(ex.getLocalizedMessage(), ex);
            }
        }

        try {

            // Retrieving the implementation class for the Web Service.
            Object implementationObject = getTheImplementationObject(msgContext);
            ClientConnectorServiceSkeletonInterface clientConnectorServiceSkeletonInterface = (ClientConnectorServiceSkeletonInterface) implementationObject;

            /* Out Envelop */
            SOAPEnvelope envelope;

            /* Find the axisOperation that has been set by the Dispatch phase. */
            AxisOperation axisOperation = msgContext.getOperationContext().getAxisOperation();
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

                Element soapHeader = XMLUtils.toDOM(reqEnv.getHeader());
                List<Assertion> assertions = SAML2Validator.getAssertions(soapHeader);

                //  Submit Document
                if (StringUtils.equals(ClientConnectorOperation.SERVICE_SUBMIT_DOCUMENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    // SAML2Validator.validateXDRHeader(soapHeader, Constants.CONSENT_CLASSCODE);

                    Assertion hcpAssertion = null;
                    Assertion trcAssertion = null;
                    for (Assertion ass : assertions) {
                        if (ass.getAdvice() == null) {
                            hcpAssertion = ass;
                        } else {
                            trcAssertion = ass;
                        }
                    }

                    SubmitDocumentDocument1 wrappedParam = (SubmitDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            SubmitDocumentDocument1.class, getEnvelopeNamespaces(reqEnv));
                    SubmitDocumentResponseDocument submitDocumentResponseDocument = clientConnectorServiceSkeletonInterface
                            .submitDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), submitDocumentResponseDocument);

                }
                //  Query Patient
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_QUERY_PATIENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    //  SAML2Validator.validateXCPDHeader(soapHeader);

                    Assertion hcpAssertion = null;
                    for (Assertion ass : assertions) {
                        hcpAssertion = ass;
                        if (hcpAssertion.getAdvice() == null) {
                            break;
                        }
                    }

                    if (OpenNCPValidation.isValidationEnable()) {
                        OpenNCPValidation.validateHCPAssertion(hcpAssertion, NcpSide.NCP_B);
                    }

                    QueryPatientDocument wrappedParam = (QueryPatientDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            QueryPatientDocument.class, getEnvelopeNamespaces(reqEnv));

                    QueryPatientResponseDocument queryPatientResponseDocument = clientConnectorServiceSkeletonInterface
                            .queryPatient(wrappedParam, hcpAssertion);
                    envelope = toEnvelope(getSOAPFactory(msgContext), queryPatientResponseDocument);
                }
                // Query Documents
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_QUERY_DOCUMENTS, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    //  SAML2Validator.validateXCAHeader(soapHeader, Constants.PS_CLASSCODE);

                    Assertion hcpAssertion = null;
                    Assertion trcAssertion = null;

                    for (Assertion ass : assertions) {

                        if (ass.getAdvice() == null) {
                            hcpAssertion = ass;

                        } else {
                            trcAssertion = ass;
                        }
                    }
                    if (OpenNCPValidation.isValidationEnable()) {
                        OpenNCPValidation.validateHCPAssertion(hcpAssertion, NcpSide.NCP_B);
                    }

                    QueryDocumentsDocument wrappedParam = (QueryDocumentsDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            QueryDocumentsDocument.class, getEnvelopeNamespaces(reqEnv));
                    QueryDocumentsResponseDocument queryDocumentsResponseDocument = clientConnectorServiceSkeletonInterface
                            .queryDocuments(wrappedParam, hcpAssertion, trcAssertion);

                    Writer writer = new StringWriter();
                    toOM(wrappedParam).serialize(writer);
                    logger.error("wrappedParam : " + writer.toString());

                    envelope = toEnvelope(getSOAPFactory(msgContext), queryDocumentsResponseDocument);
                }
                //  Retrieve Document
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_RETRIEVE_DOCUMENT, methodName)) {

                    //TODO: Analysis if the Validation of the Assertions Header has been required at this step.
                    //  SAML2Validator.validateXCAHeader(soapHeader, Constants.PS_CLASSCODE);

                    Assertion hcpAssertion = null;
                    Assertion trcAssertion = null;

                    for (Assertion ass : assertions) {

                        if (ass.getAdvice() == null) {
                            hcpAssertion = ass;
                        } else {
                            trcAssertion = ass;
                        }
                    }
                    if (OpenNCPValidation.isValidationEnable()) {
                        OpenNCPValidation.validateHCPAssertion(hcpAssertion, NcpSide.NCP_B);
                    }

                    RetrieveDocumentDocument1 wrappedParam = (RetrieveDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            RetrieveDocumentDocument1.class, getEnvelopeNamespaces(reqEnv));
                    RetrieveDocumentResponseDocument retrieveDocumentResponseDocument = clientConnectorServiceSkeletonInterface
                            .retrieveDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), retrieveDocumentResponseDocument);
                }
                // Say hello
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_TEST_SAY_HELLO, methodName)) {

                    SayHelloDocument wrappedParam = (SayHelloDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            SayHelloDocument.class, getEnvelopeNamespaces(reqEnv));
                    SayHelloResponseDocument sayHelloResponseDocument = clientConnectorServiceSkeletonInterface.sayHello(wrappedParam);

                    envelope = toEnvelope(getSOAPFactory(msgContext), sayHelloResponseDocument);
                } else {
                    // Else Method not Available
                    throw new ClientConnectorException("Client Connector Error: Method not found");
                }

                /*
                 * Log SOAP response
                 */
                if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                    try {
                        String logRequestMsg = XMLUtil.prettyPrint(XMLUtils.toDOM(envelope));
                        loggerClinical.debug("Outgoing '{}' response message to portal:\n{}", operationName, logRequestMsg);

                    } catch (Exception ex) {
                        logger.error(ex.getLocalizedMessage(), ex);
                    }
                }
                // Soap message: HTTP header set.
                String randomUUID = Constants.UUID_PREFIX + UUID.randomUUID();
                newMsgContext.setEnvelope(envelope);
                newMsgContext.getOptions().setMessageId(randomUUID);
            }
        } catch (Exception e) {

            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final XmlObject param) throws AxisFault {

        XmlOptions xmlOptions = new XmlOptions();
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

    /**
     * @param param
     * @param type
     * @param extraNamespaces
     * @return
     * @throws AxisFault
     */
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

    /**
     * A utility method that copies the namespaces from the SOAPEnvelope.
     */
    private Map getEnvelopeNamespaces(SOAPEnvelope env) {

        Map<String, String> returnMap = new HashMap<>();
        Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace ns = (OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }
}
