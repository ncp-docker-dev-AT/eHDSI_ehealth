package _2009.xcpd.iti.ihe;

import com.spirit.epsos.cc.adc.EadcEntry;
import epsos.ccd.gnomon.auditmanager.AuditService;
import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.pt.eadc.EadcUtilWrapper;
import eu.epsos.pt.eadc.util.EadcUtil;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.eadc.ServiceType;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;
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
import java.util.*;

/**
 * XCPD_ServiceMessageReceiverInOut message receiver
 */
public class XCPD_ServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(XCPD_ServiceMessageReceiverInOut.class);
    private static final javax.xml.bind.JAXBContext wsContext;

    static {

        LOGGER.debug("Loading the WS-Security init libraries in XCPD_ServiceMessageReceiverInOut xcpd 2009");
        org.apache.xml.security.Init.init();
    }

    static {

        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(org.hl7.v3.PRPAIN201305UV02.class, org.hl7.v3.PRPAIN201306UV02.class);
        } catch (javax.xml.bind.JAXBException ex) {
            LOGGER.error("Unable to create JAXBContext: '{}'", ex.getMessage(), ex);
        } finally {
            wsContext = jc;
        }
    }

    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private String getIPofSender(MessageContext messageContext) {

        return (String) messageContext.getProperty(MessageContext.REMOTE_ADDR);
    }

    private String getMessageID(SOAPEnvelope envelope) {

        Iterator<OMElement> it = envelope.getHeader().getChildrenWithName(
                new QName("http://www.w3.org/2005/08/addressing", "MessageID"));
        if (it.hasNext()) {
            return it.next().getText();
        } else {
            // [Mustafa: May 8, 2012]: Should not be empty string, sch. gives error.
            return Constants.UUID_PREFIX;
        }
    }

    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newMsgContext) throws AxisFault {

        try {
            Date startTime = new Date();

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);
            SOAPHeader sh = msgContext.getEnvelope().getHeader();

            EventLog eventLog = new EventLog();
            String ip = getIPofSender(msgContext);
            eventLog.setSourceip(ip);
            eventLog.setReqM_ParticipantObjectID(getMessageID(msgContext.getEnvelope()));
            eventLog.setReqM_PatricipantObjectDetail(msgContext.getEnvelope().getHeader().toString().getBytes());

            HttpServletRequest req = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            String clientDN = HTTPUtil.getClientCertificate(req);
            eventLog.setSC_UserID(clientDN);
            eventLog.setTargetip(HTTPUtil.getHostIpAddress(req.getServerName()));

            if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                loggerClinical.debug("Incoming XCPD Request Message:\n{}", XMLUtil.prettyPrint(XMLUtils.toDOM(msgContext.getEnvelope())));
            }

            /* Validate incoming request message */
            String message = XMLUtil.prettyPrint(XMLUtils.toDOM(msgContext.getEnvelope().getBody().getFirstElement()));
            if (OpenNCPValidation.isValidationEnable()) {
                OpenNCPValidation.validatePatientDemographicRequest(message, NcpSide.NCP_A);
            }

            XCPD_ServiceSkeleton skeleton = (XCPD_ServiceSkeleton) obj;
            // Out Envelop
            SOAPEnvelope envelope;
            // Find the axisOperation that has been set by the Dispatch phase.
            AxisOperation op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION " +
                        "should specified via the SOAP Action to use the RawXMLProvider");
            }

            String randomUUID = Constants.UUID_PREFIX + UUID.randomUUID().toString();
            String methodName;

            if ((op.getName() != null) && ((methodName = JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {

                if (StringUtils.equals("respondingGateway_PRPA_IN201305UV02", methodName)) {

                    PRPAIN201305UV02 wrappedParam = (PRPAIN201305UV02) fromOM(msgContext.getEnvelope().getBody().getFirstElement(),
                            PRPAIN201305UV02.class, getEnvelopeNamespaces(msgContext.getEnvelope()));

                    PRPAIN201306UV02 prpain201306UV02 = skeleton.respondingGateway_PRPA_IN201305UV02(wrappedParam, sh, eventLog);

                    envelope = toEnvelope(getSOAPFactory(msgContext), prpain201306UV02, false);

                    /* Validate response message */
                    if (OpenNCPValidation.isValidationEnable()) {
                        OpenNCPValidation.validatePatientDemographicResponse(XMLUtil.prettyPrint(XMLUtils.toDOM(
                                envelope.getBody().getFirstElement())),
                                NcpSide.NCP_A);
                    }
                    eventLog.setResM_ParticipantObjectID(randomUUID);
                    eventLog.setResM_PatricipantObjectDetail(envelope.getHeader().toString().getBytes());
                    eventLog.setNcpSide(NcpSide.NCP_A);
                    LOGGER.info("EventLog: '{}'", eventLog.getEventType());
                    AuditService auditService = AuditServiceFactory.getInstance();
                    auditService.write(eventLog, "", "1");

                    if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                        loggerClinical.debug("Outgoing XCPD Response Message:\n{}", XMLUtil.prettyPrint(XMLUtils.toDOM(envelope)));
                    }

                } else {
                    LOGGER.error("Method not Found: '{}'", methodName);
                    throw new RuntimeException("method not found");
                }

                Date endTime = new Date();
                newMsgContext.setEnvelope(envelope);
                newMsgContext.getOptions().setMessageId(randomUUID);

                //TODO: Review EADC specification for INBOUND/OUTBOUND [EHNCP-829]
                EadcUtilWrapper.invokeEadc(msgContext, newMsgContext, null, null, startTime,
                        endTime, Constants.COUNTRY_CODE, EadcEntry.DsTypes.XCPD, EadcUtil.Direction.INBOUND,
                        ServiceType.PATIENT_IDENTIFICATION_RESPONSE);
            }
        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement toOM(org.hl7.v3.PRPAIN201305UV02 param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            OMFactory factory = OMAbstractFactory.getOMFactory();
            JaxbRIDataSource source = new JaxbRIDataSource(PRPAIN201305UV02.class, param, marshaller,
                    "urn:hl7-org:v3", "PRPA_IN201305UV02");
            OMNamespace namespace = factory.createOMNamespace("urn:hl7-org:v3", null);

            return factory.createOMElement(source, "PRPA_IN201305UV02", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, org.hl7.v3.PRPAIN201305UV02 param, boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    private OMElement toOM(PRPAIN201306UV02 param, boolean optimizeContent) throws AxisFault {

        try {

            Marshaller marshaller = wsContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            OMFactory factory = OMAbstractFactory.getOMFactory();
            JaxbRIDataSource source = new JaxbRIDataSource(PRPAIN201306UV02.class, param, marshaller, "urn:hl7-org:v3", "PRPA_IN201306UV02");
            OMNamespace namespace = factory.createOMNamespace("urn:hl7-org:v3", null);

            return factory.createOMElement(source, "PRPA_IN201306UV02", namespace);

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    private SOAPEnvelope toEnvelope(SOAPFactory factory, PRPAIN201306UV02 param, boolean optimizeContent) throws AxisFault {

        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(toOM(param, optimizeContent));

        return envelope;
    }

    /**
     * get the default envelope
     */
    private SOAPEnvelope toEnvelope(SOAPFactory factory) {

        return factory.getDefaultEnvelope();
    }

    private Object fromOM(OMElement param, Class type, Map extraNamespaces) throws AxisFault {

        try {
            Unmarshaller unmarshaller = wsContext.createUnmarshaller();

            return unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), type).getValue();

        } catch (JAXBException bex) {
            throw AxisFault.makeFault(bex);
        }
    }

    /**
     * A utility method that copies the namepaces from the SOAPEnvelope
     */
    private Map getEnvelopeNamespaces(SOAPEnvelope env) {

        Map returnMap = new HashMap();
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

                SAXOMBuilder builder = new SAXOMBuilder();
                Marshaller marshaller = wsContext.createMarshaller();
                marshaller.marshal(new JAXBElement(new QName(nsuri, name), outObject.getClass(), outObject), builder);

                return builder.getRootElement().getXMLStreamReader();

            } catch (JAXBException e) {
                throw new XMLStreamException("Error in JAXB marshalling", e);
            }
        }
    }
}
