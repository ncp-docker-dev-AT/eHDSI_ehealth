package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
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
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML2Validator;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ClientConnectorServiceServiceMessageReceiverInOut message receiver.
 */
public class ClientConnectorServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectorServiceMessageReceiverInOut.class);

    static {
        LOGGER.debug("Loading the WS-Security init libraries in ClientConnectorServiceMessageReceiverInOut 2009");
        org.apache.xml.security.Init.init();
    }

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
                LOGGER.debug(ex.getLocalizedMessage(), ex);
            }
        }

        /*
         * Body
         */
        try {

            /* get the implementation class for the Web Service */
            ClientConnectorServiceSkeletonInterface skel;
            Object obj = getTheImplementationObject(msgContext);
            skel = (ClientConnectorServiceSkeletonInterface) obj;

            /* Out Envelop */
            SOAPEnvelope envelope;

            /* Find the axisOperation that has been set by the Dispatch phase. */
            AxisOperation op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION "
                        + "should specified via the SOAP Action to use the RawXMLProvider");
            }

            String methodName;
            if ((op.getName() != null) && ((methodName = JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {

                /*
                 * Assertions
                 */
                List<Assertion> assertions;
                Element soapHeader = XMLUtils.toDOM(reqEnv.getHeader());
                assertions = SAML2Validator.getAssertions(soapHeader);

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

                    SubmitDocumentResponseDocument submitDocumentResponse11;
                    SubmitDocumentDocument1 wrappedParam = (SubmitDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            SubmitDocumentDocument1.class, getEnvelopeNamespaces(reqEnv));
                    submitDocumentResponse11 = skel.submitDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), submitDocumentResponse11);

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

                    QueryPatientResponseDocument queryPatientResponse13 = skel.queryPatient(wrappedParam, hcpAssertion);
                    envelope = toEnvelope(getSOAPFactory(msgContext), queryPatientResponse13);
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

                    QueryDocumentsResponseDocument queryDocumentsResponse17;

                    QueryDocumentsDocument wrappedParam;
                    wrappedParam = (QueryDocumentsDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            QueryDocumentsDocument.class, getEnvelopeNamespaces(reqEnv));
                    queryDocumentsResponse17 = skel.queryDocuments(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), queryDocumentsResponse17);
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

                    RetrieveDocumentResponseDocument retrieveDocumentResponse19;
                    RetrieveDocumentDocument1 wrappedParam;
                    wrappedParam = (RetrieveDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            RetrieveDocumentDocument1.class, getEnvelopeNamespaces(reqEnv));
                    retrieveDocumentResponse19 = skel.retrieveDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), retrieveDocumentResponse19);
                }
                // Say hello
                else if (StringUtils.equals(ClientConnectorOperation.SERVICE_TEST_SAY_HELLO, methodName)) {

                    SayHelloResponseDocument sayHelloResponseDocument;
                    SayHelloDocument wrappedParam;
                    wrappedParam = (SayHelloDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            SayHelloDocument.class, getEnvelopeNamespaces(reqEnv));
                    sayHelloResponseDocument = skel.sayHello(wrappedParam);

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
                        loggerClinical.info("Outgoing '{}' response message to portal:\n{}", operationName, logRequestMsg);

                    } catch (Exception ex) {
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                    }
                }
                newMsgContext.setEnvelope(envelope);
            }

        } catch (Exception e) {

            LOGGER.error("Exception: '{}'", e.getLocalizedMessage(), e);
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
