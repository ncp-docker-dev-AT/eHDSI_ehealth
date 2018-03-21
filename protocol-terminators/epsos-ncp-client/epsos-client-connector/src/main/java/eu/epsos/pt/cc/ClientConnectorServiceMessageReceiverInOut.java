package eu.epsos.pt.cc;

import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.saml.AssertionSchematron;
import eu.epsos.validation.services.AssertionValidationService;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
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
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.securityman.SAML2Validator;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.Map;

/**
 * ClientConnectorServiceServiceMessageReceiverInOut message receiver.
 */
public class ClientConnectorServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectorServiceMessageReceiverInOut.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("FILE_CLINICAL");

    static {
        LOGGER.debug("Loading the WS-Security init libraries in ClientConnectorServiceMessageReceiverInOut 2009");
        org.apache.xml.security.Init.init(); // Joao added 10/03/2017. 
    }

    @Override
    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newMsgContext) throws AxisFault {

        SOAPEnvelope reqEnv = msgContext.getEnvelope();
        String operationName = msgContext.getOperationContext().getOperationName();

        /*
         * Log soap request
         */
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            try {
                String logRequestMsg = XMLUtil.prettyPrint(XMLUtils.toDOM(reqEnv));
                LOGGER_CLINICAL.debug("Incoming '{}' request message from portal:\n{}", operationName, logRequestMsg);

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
                throw new AxisFault("Operation is not located,"
                        + " if this is doclit style the SOAP-ACTION "
                        + "should specified via the SOAP Action to use the RawXMLProvider");
            }

            java.lang.String methodName;
            if ((op.getName() != null)
                    && ((methodName = JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {

                /*
                 * Assertions
                 */
                List<Assertion> assertions;
                Element soapHeader = XMLUtils.toDOM(reqEnv.getHeader());
                assertions = SAML2Validator.getAssertions(soapHeader);
                Assertion mainHcpAssertion = null;
                for (Assertion ass : assertions) {
                    if (ass.getAdvice() == null) {
                        mainHcpAssertion = ass;
                    }
                }

                /*
                 * Call to service
                 */
                //  Submit Document
                if ("submitDocument".equals(methodName)) {

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
                    SubmitDocumentDocument1 wrappedParam;
                    wrappedParam = (SubmitDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            SubmitDocumentDocument1.class, getEnvelopeNamespaces(reqEnv));
                    submitDocumentResponse11 = skel.submitDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), submitDocumentResponse11);

                }
                //  Query Patient
                else if ("queryPatient".equals(methodName)) {

                    //  SAML2Validator.validateXCPDHeader(soapHeader);
                    Assertion hcpAssertion = null;
                    for (Assertion ass : assertions) {
                        hcpAssertion = ass;
                        if (hcpAssertion.getAdvice() == null) {
                            break;
                        }
                    }

                    String assertionsSTR = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(hcpAssertion.getDOM()).getBytes());
//                    LOGGER.info("[Validation Service: Assertion Validator: '{}']", ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.new"));
//                    if (ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.new")) {
//
//                        SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
//                        boolean validated = schematronValidator.validateObject(XMLUtil.prettyPrint(hcpAssertion.getDOM()), AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString(), NcpSide.NCP_B.getName());
//
//                        LOGGER.info("[Validation Service: Assertion Validator: '{}']", validated);
//                        //                        AssertionValidator assertionValidator = GazelleValidatorFactory.getAssertionValidator();
////                        assertionValidator.validateBase64Document(assertionsSTR, AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString());
//                    }

                    if (StringUtils.equalsIgnoreCase(ConfigurationManagerFactory.getConfigurationManager().getProperty("automated.validation"), "true")) {

                        AssertionValidationService.getInstance().validateSchematron(XMLUtil.prettyPrint(hcpAssertion.getDOM()),
                                AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString(), NcpSide.NCP_B);
                    }
                    QueryPatientDocument wrappedParam;
                    wrappedParam = (QueryPatientDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            QueryPatientDocument.class,
                            getEnvelopeNamespaces(reqEnv));

                    QueryPatientResponseDocument queryPatientResponse13 = skel.queryPatient(wrappedParam, hcpAssertion);
                    envelope = toEnvelope(getSOAPFactory(msgContext), queryPatientResponse13);
                }
                // Say hello
                else if ("sayHello".equals(methodName)) {
                    SayHelloResponseDocument sayHelloResponse15;
                    SayHelloDocument wrappedParam;
                    wrappedParam = (SayHelloDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            SayHelloDocument.class,
                            getEnvelopeNamespaces(reqEnv));
                    sayHelloResponse15 = skel.sayHello(wrappedParam);

                    envelope = toEnvelope(getSOAPFactory(msgContext), sayHelloResponse15);
                }
                // Query Documents
                else if ("queryDocuments".equals(methodName)) {

                    //  SAML2Validator.validateXCAHeader(soapHeader, Constants.PS_CLASSCODE);
                    Assertion hcpAssertion = null;
                    Assertion trcAssertion = null;
                    String assertionSchematron = null;
                    for (Assertion ass : assertions) {
                        if (ass.getAdvice() == null) {
                            hcpAssertion = ass;
                            assertionSchematron = AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString();
                        } else {
                            trcAssertion = ass;
                            assertionSchematron = AssertionSchematron.EPSOS_TRC_ASSERTION.toString();
                        }
                    }
                    AssertionValidationService.getInstance().validateSchematron(XMLUtil.prettyPrint(hcpAssertion.getDOM()), assertionSchematron
                            , NcpSide.NCP_B);

                    QueryDocumentsResponseDocument queryDocumentsResponse17;

                    QueryDocumentsDocument wrappedParam;
                    wrappedParam = (QueryDocumentsDocument) fromOM(reqEnv.getBody().getFirstElement(),
                            QueryDocumentsDocument.class,
                            getEnvelopeNamespaces(reqEnv));
                    queryDocumentsResponse17 = skel.queryDocuments(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), queryDocumentsResponse17);
                }
                //  Retrieve Document
                else if ("retrieveDocument".equals(methodName)) {

                    //  SAML2Validator.validateXCAHeader(soapHeader, Constants.PS_CLASSCODE);
                    Assertion hcpAssertion = null;
                    Assertion trcAssertion = null;
                    String assertionSchematron = null;
                    for (Assertion ass : assertions) {
                        if (ass.getAdvice() == null) {
                            hcpAssertion = ass;
                            assertionSchematron = AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString();
                        } else {
                            trcAssertion = ass;
                            assertionSchematron = AssertionSchematron.EPSOS_TRC_ASSERTION.toString();
                        }
                    }

                    AssertionValidationService.getInstance().validateSchematron(XMLUtil.prettyPrint(hcpAssertion.getDOM()),
                            assertionSchematron, NcpSide.NCP_B);

                    RetrieveDocumentResponseDocument retrieveDocumentResponse19;
                    RetrieveDocumentDocument1 wrappedParam;
                    wrappedParam = (RetrieveDocumentDocument1) fromOM(reqEnv.getBody().getFirstElement(),
                            RetrieveDocumentDocument1.class,
                            getEnvelopeNamespaces(reqEnv));
                    retrieveDocumentResponse19 = skel.retrieveDocument(wrappedParam, hcpAssertion, trcAssertion);

                    envelope = toEnvelope(getSOAPFactory(msgContext), retrieveDocumentResponse19);
                } else {
                    // Else Method not Available
                    throw new java.lang.RuntimeException("method not found");
                }

                /*
                 * Log soap request
                 */
                if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                    try {
                        String logRequestMsg = XMLUtil.prettyPrint(XMLUtils.toDOM(envelope));
                        LOGGER_CLINICAL.debug("Outgoing '{}' response message to portal:\n{}", operationName, logRequestMsg);

                    } catch (Exception ex) {
                        LOGGER.debug(ex.getLocalizedMessage(), ex);
                    }
                }
                newMsgContext.setEnvelope(envelope);
            }

        } catch (java.lang.Exception e) {

            LOGGER.error(e.getLocalizedMessage(), e);
            throw AxisFault.makeFault(e);
        }
    }

    /*
     * ELEMENT
     */
    private OMElement toOM(final SubmitDocumentResponseDocument param) throws AxisFault {

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new javax.xml.transform.sax.SAXSource(new XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final QueryPatientResponseDocument param) throws AxisFault {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new javax.xml.transform.sax.SAXSource(new XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final SayHelloResponseDocument param) throws AxisFault {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new javax.xml.transform.sax.SAXSource(new XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final QueryDocumentsResponseDocument param) throws AxisFault {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new javax.xml.transform.sax.SAXSource(new XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(final RetrieveDocumentResponseDocument param) throws AxisFault {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNoXmlDecl();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
                new javax.xml.transform.sax.SAXSource(new XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
        try {
            return builder.getDocumentElement(true);
        } catch (java.lang.Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /*
     * ENVELOP
     */
    private SOAPEnvelope toEnvelope(SOAPFactory factory, SubmitDocumentResponseDocument param) throws AxisFault {
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, QueryPatientResponseDocument param) throws AxisFault {
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, SayHelloResponseDocument param) throws AxisFault {
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, QueryDocumentsResponseDocument param) throws AxisFault {
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, RetrieveDocumentResponseDocument param) throws AxisFault {
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        if (param != null) {
            envelope.getBody().addChild(toOM(param));
        }
        return envelope;
    }

    /*
     * OTHER
     */
    public XmlObject fromOM(OMElement param, Class type, Map extraNamespaces) throws AxisFault {
        try {

            if (SubmitDocumentDocument1.class.equals(type)) {

                if (extraNamespaces != null) {
                    return SubmitDocumentDocument1.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SubmitDocumentDocument1.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SubmitDocumentResponseDocument.class.equals(type)) {

                if (extraNamespaces != null) {
                    return SubmitDocumentResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SubmitDocumentResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryPatientDocument.class.equals(type)) {

                if (extraNamespaces != null) {
                    return QueryPatientDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryPatientDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryPatientResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryPatientResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryPatientResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SayHelloDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    XMLStreamReader xmlStreamReaderWithoutCaching = param.getXMLStreamReaderWithoutCaching();
                    XmlOptions setLoadAdditionalNamespaces = new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces);
                    return SayHelloDocument.Factory.parse(xmlStreamReaderWithoutCaching, setLoadAdditionalNamespaces);

                } else {
                    return SayHelloDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (SayHelloResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return SayHelloResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return SayHelloResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryDocumentsDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryDocumentsDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryDocumentsDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (QueryDocumentsResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return QueryDocumentsResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return QueryDocumentsResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (RetrieveDocumentDocument1.class.equals(type)) {
                if (extraNamespaces != null) {
                    return RetrieveDocumentDocument1.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
                            new XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));

                } else {
                    return RetrieveDocumentDocument1.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching());
                }
            }

            if (RetrieveDocumentResponseDocument.class.equals(type)) {
                if (extraNamespaces != null) {
                    return RetrieveDocumentResponseDocument.Factory.parse(
                            param.getXMLStreamReaderWithoutCaching(),
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
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace ns = (OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }
}
