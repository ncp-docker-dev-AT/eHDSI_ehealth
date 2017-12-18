/*
 * Copyright (C) 2011, 2012 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik
 * Tic. Ltd. Sti. <epsos@srdc.com.tr>
 * <p>
 * This file is part of SRDC epSOS NCP.
 * <p>
 * SRDC epSOS NCP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SRDC epSOS NCP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SRDC epSOS NCP. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 * <p>
 * XCA_ServiceMessageReceiverInOut.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5.4
 * Built on : Dec 19, 2010 (08:18:42 CET)
 */
package _2007.xds_b.iti.ihe;

import com.spirit.epsos.cc.adc.EadcEntry;
import epsos.ccd.gnomon.auditmanager.AuditService;
import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.pt.eadc.EadcUtilWrapper;
import eu.epsos.pt.eadc.util.EadcUtil;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.xd.XdModel;
import eu.epsos.validation.services.XcaValidationService;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.XMLUtil;
import tr.com.srdc.epsos.util.http.HTTPUtil;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * XCA_ServiceMessageReceiverInOut message receiver
 */
public class XCA_ServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(XCA_ServiceMessageReceiverInOut.class);

    private static final JAXBContext wsContext;

    static {
        LOGGER.debug("Loading the WS-Security init libraries in XCA 2007");
        org.apache.xml.security.Init.init();
    }

    static {
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(
                    oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest.class,
                    oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse.class,
                    ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.class,
                    ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.class);
        } catch (JAXBException ex) {
            LOGGER.error("Unable to create JAXBContext: '{}'", ex.getMessage(), ex);
            Runtime.getRuntime().exit(-1);
        } finally {
            wsContext = jc;
        }
    }

    private String getIPofSender(MessageContext msgContext) {

        return (String) msgContext.getProperty("REMOTE_ADDR");
    }

    private String getMessageID(SOAPEnvelope envelope) {

        Iterator<OMElement> it = envelope.getHeader().getChildrenWithName(new QName("http://www.w3.org/2005/08/addressing", "MessageID"));
        if (it.hasNext()) {
            return it.next().getText();
        } else {
            // [Mustafa: May 8, 2012]: Should not be empty string, sch. gives error.
            return tr.com.srdc.epsos.util.Constants.UUID_PREFIX;
        }
    }

    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newMsgContext) throws AxisFault {

        ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType retrieveDocumentSetResponseType = null;
        try {
            Date startTime = new Date();
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            XCA_ServiceSkeleton skel = (XCA_ServiceSkeleton) obj;
            // Out Envelop
            SOAPEnvelope envelope;
            // Find the axisOperation that has been set by the Dispatch phase.
            AxisOperation op = msgContext.getOperationContext().getAxisOperation();

            if (op == null) {
                throw new AxisFault(
                        "Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }

            String randomUUID = tr.com.srdc.epsos.util.Constants.UUID_PREFIX + UUID.randomUUID().toString();
            String methodName;

            if ((op.getName() != null) && ((methodName = JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {

                SOAPHeader sh = msgContext.getEnvelope().getHeader();

                EventLog eventLog = new EventLog();

                String ip = getIPofSender(msgContext);
                eventLog.setSourceip(ip);
                eventLog.setReqM_ParticipantObjectID(getMessageID(msgContext.getEnvelope()));
                eventLog.setReqM_PatricipantObjectDetail(msgContext.getEnvelope().getHeader().toString().getBytes());

                HttpServletRequest req = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
                String clientDN = HTTPUtil.getClientCertificate(req);
                eventLog.setSC_UserID(clientDN);

                eventLog.setTargetip(req.getServerName());

                LOGGER.debug("Incoming XCA Request Message:\n{}", XMLUtil.prettyPrint(XMLUtils.toDOM(msgContext.getEnvelope())));

                if (StringUtils.equals("respondingGateway_CrossGatewayQuery", methodName)) {

                    /* Validate incoming query request */
                    String requestMessage = XMLUtil.prettyPrintForValidation(XMLUtils.toDOM(msgContext.getEnvelope().getBody().getFirstElement()));
                    XcaValidationService.getInstance().validateModel(requestMessage, XdModel.obtainModelXca(requestMessage).toString(), NcpSide.NCP_A);

                    oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse adhocQueryResponse1;
                    oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest wrappedParam = (oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest) fromOM(
                            msgContext.getEnvelope().getBody().getFirstElement(), oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest.class,
                            getEnvelopeNamespaces(msgContext.getEnvelope()));

                    adhocQueryResponse1 = skel.respondingGateway_CrossGatewayQuery(wrappedParam, sh, eventLog);
                    envelope = toEnvelope(getSOAPFactory(msgContext),
                            adhocQueryResponse1, false);
                    eventLog.setResM_ParticipantObjectID(randomUUID);
                    eventLog.setResM_PatricipantObjectDetail(envelope.getHeader().toString().getBytes());

                    AuditService auditService = new AuditService();
                    auditService.write(eventLog, "", "1");

                    /* Validate outgoing query response */
                    String responseMessage = XMLUtil.prettyPrintForValidation(XMLUtils.toDOM(envelope.getBody().getFirstElement()));
                    XcaValidationService.getInstance().validateModel(responseMessage, XdModel.obtainModelXca(responseMessage).toString(), NcpSide.NCP_A);

                    LOGGER.debug("Response Header:\n{}", envelope.getHeader().toString());
                    LOGGER.debug("Outgoing XCA Response Message:\n{}", XMLUtil.prettyPrint(XMLUtils.toDOM(envelope)));

                } else if (StringUtils.equals("respondingGateway_CrossGatewayRetrieve", methodName)) {

                    /* Validate incoming retrieve request */
                    String requestMessage = XMLUtil.prettyPrint(XMLUtils.toDOM(msgContext.getEnvelope().getBody().getFirstElement()));
                    XcaValidationService.getInstance().validateModel(requestMessage, XdModel.obtainModelXca(requestMessage).toString(), NcpSide.NCP_A);

                    ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType retrieveDocumentSetResponse3 = null;
                    ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType wrappedParam = (ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType) fromOM(
                            msgContext.getEnvelope().getBody().getFirstElement(),
                            ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.class,
                            getEnvelopeNamespaces(msgContext.getEnvelope()));

                    OMFactory factory = OMAbstractFactory.getOMFactory();
                    OMNamespace ns = factory.createOMNamespace("urn:ihe:iti:xds-b:2007", "");
                    OMElement omElement = factory.createOMElement("RetrieveDocumentSetResponse", ns);
                    skel.respondingGateway_CrossGatewayRetrieve(wrappedParam, sh, eventLog, omElement);

                    envelope = toEnvelope(getSOAPFactory(msgContext), omElement);

                    eventLog.setResM_ParticipantObjectID(randomUUID);
                    eventLog.setResM_PatricipantObjectDetail(envelope.getHeader().toString().getBytes());

                    AuditService auditService = new AuditService();
                    auditService.write(eventLog, "", "1");
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Outgoing XCA Response Message:\n{}", XMLUtil.prettyPrint(XMLUtils.toDOM(envelope)));
                    }

                    Options options = new Options();
                    options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
                    newMsgContext.setOptions(options);

                    /* Validate outgoing retrieve response */
                    String responseMessage = XMLUtil.prettyPrint(XMLUtils.toDOM(envelope.getBody().getFirstElement()));
                    XcaValidationService.getInstance().validateModel(responseMessage, XdModel.obtainModelXca(responseMessage).toString(), NcpSide.NCP_A);

                } else {
                    LOGGER.error("Method not found: '{}'", methodName);
                    throw new java.lang.RuntimeException("method not found");
                }

                Date endTime = new Date();
                newMsgContext.setEnvelope(envelope);
                newMsgContext.getOptions().setMessageId(randomUUID);

                //TODO: Review EADC specification for INBOUND/OUTBOUND [EHNCP-829]
                try {
                    EadcUtilWrapper.invokeEadc(msgContext, newMsgContext, null,
                            EadcUtilWrapper.getCDA(retrieveDocumentSetResponseType), startTime, endTime, "",
                            EadcEntry.DsTypes.XCA, EadcUtil.Direction.INBOUND);

                } catch (Exception e) {
                    LOGGER.error("EADC INVOCATION FAILED: '{}'", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            OMFactory factory = OMAbstractFactory.getOMFactory();

            JaxbRIDataSource source = new JaxbRIDataSource(oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest.class,
                    param, marshaller, "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AdhocQueryRequest");
            org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", null);

            return factory.createOMElement(source, "AdhocQueryRequest", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest param,
                                    boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    private OMElement toOM(oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            OMFactory factory = OMAbstractFactory.getOMFactory();

            JaxbRIDataSource source = new JaxbRIDataSource(oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse.class,
                    param, marshaller, "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AdhocQueryResponse");
            OMNamespace namespace = factory.createOMNamespace("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", null);

            return factory.createOMElement(source, "AdhocQueryResponse", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse param,
                                    boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    private OMElement toOM(ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            OMFactory factory = OMAbstractFactory.getOMFactory();

            JaxbRIDataSource source = new JaxbRIDataSource(ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.class, param,
                    marshaller, "urn:ihe:iti:xds-b:2007", "RetrieveDocumentSetRequest");
            OMNamespace namespace = factory.createOMNamespace("urn:ihe:iti:xds-b:2007", null);

            return factory.createOMElement(source, "RetrieveDocumentSetRequest", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType param,
                                    boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    private OMElement toOM(ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            OMFactory factory = OMAbstractFactory.getOMFactory();
            JaxbRIDataSource source = new JaxbRIDataSource(ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.class,
                    param, marshaller, "urn:ihe:iti:xds-b:2007", "RetrieveDocumentSetResponse");

            OMNamespace namespace = factory.createOMNamespace("urn:ihe:iti:xds-b:2007", null);

            return factory.createOMElement(source, "RetrieveDocumentSetResponse", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType param, boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, OMElement param) {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(param);

        return envelope;
    }

    /**
     * get the default envelope
     */
    private SOAPEnvelope toEnvelope(SOAPFactory factory) {

        return factory.getDefaultEnvelope();
    }

    private java.lang.Object fromOM(OMElement param, Class type, Map extraNamespaces) throws AxisFault {

        try {
            Unmarshaller unmarshaller = wsContext.createUnmarshaller();

            return unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), type).getValue();

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    /**
     * A utility method that copies the namespaces from the SOAPEnvelope
     */
    private Map getEnvelopeNamespaces(SOAPEnvelope env) {

        Map returnMap = new java.util.HashMap();
        Iterator namespaceIterator = env.getAllDeclaredNamespaces();

        while (namespaceIterator.hasNext()) {

            OMNamespace ns = (OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private AxisFault createAxisFault(Exception e) {

        AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new AxisFault(e.getMessage(), cause);
        } else {
            f = new AxisFault(e.getMessage());
        }

        return f;
    }

    class JaxbRIDataSource implements OMDataSource {

        /**
         * Bound object for output.
         */
        private final Object outObject;
        /**
         * Bound class for output.
         */
        private final Class outClazz;
        /**
         * Marshaller.
         */
        private final Marshaller marshaller;
        /**
         * Namespace
         */
        private String nsuri;
        /**
         * Local name
         */
        private String name;

        /**
         * Constructor from object and marshaller.
         *
         * @param obj
         * @param marshaller
         */
        public JaxbRIDataSource(Class clazz, Object obj, Marshaller marshaller, String nsuri, String name) {
            this.outClazz = clazz;
            this.outObject = obj;
            this.marshaller = marshaller;
            this.nsuri = nsuri;
            this.name = name;
        }

        public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {

            try {
                marshaller.marshal(new JAXBElement(new QName(nsuri, name), outObject.getClass(), outObject), output);

            } catch (JAXBException e) {
                throw new XMLStreamException("Error in JAXB marshalling", e);
            }
        }

        public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {

            try {
                marshaller.marshal(new JAXBElement(new QName(nsuri, name), outObject.getClass(), outObject), writer);
            } catch (JAXBException e) {
                throw new XMLStreamException("Error in JAXB marshalling", e);
            }
        }

        public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {

            try {
                marshaller.marshal(new JAXBElement(new QName(nsuri, name), outObject.getClass(), outObject), xmlWriter);

            } catch (JAXBException e) {
                throw new XMLStreamException("Error in JAXB marshalling", e);
            }
        }

        public XMLStreamReader getReader() throws XMLStreamException {

            try {

                SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                Marshaller marshaller = wsContext.createMarshaller();
                marshaller.marshal(new JAXBElement(new QName(nsuri, name), outObject.getClass(), outObject), builder);

                return builder.getRootElement().getXMLStreamReader();

            } catch (JAXBException e) {
                throw new XMLStreamException("Error in JAXB marshalling", e);
            }
        }
    }
}
