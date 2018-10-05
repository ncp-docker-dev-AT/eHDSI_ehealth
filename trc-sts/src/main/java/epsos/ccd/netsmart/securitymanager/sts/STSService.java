package epsos.ccd.netsmart.securitymanager.sts;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import epsos.ccd.gnomon.auditmanager.*;
import epsos.ccd.netsmart.securitymanager.SamlTRCIssuer;
import epsos.ccd.netsmart.securitymanager.SignatureManager;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.sts.util.STSUtils;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.ws.*;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
@ServiceMode(value = Mode.MESSAGE)
@WebServiceProvider(targetNamespace = "http://epsos.eu/", serviceName = "SecurityTokenService", portName = "ISecurityTokenService_Port")
@BindingType(value = "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class STSService implements Provider<SOAPMessage> {

    private final Logger logger = LoggerFactory.getLogger(STSService.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static final QName Messaging_To = new QName("http://www.w3.org/2005/08/addressing", "To");
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion"; // What
    // can be only requested from the STS
    private static final String SUPPORTED_ACTION_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue"; // Only
    // Issuance is supported
    private static final String TRC_NS = "http://epsos.eu/trc"; // TRC
    // Parameters Namespace
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512"; // WS-Trust
    // Namespace
    private static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing"; // WSA
    // Namespace
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_SEC_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    @Resource
    private WebServiceContext context;

    @Override
    public SOAPMessage invoke(SOAPMessage source) {

        if (logger.isDebugEnabled()) {
            logger.debug("Incoming SOAP Message request: '{}'", source);
            log(source);
            String value = System.getProperty("javax.net.ssl.key.alias");
            logger.debug("Certificate Alias: '{}'", value);
        }

        SOAPBody body;
        SOAPHeader header;
        try {
            body = source.getSOAPBody();
            header = source.getSOAPHeader();
        } catch (SOAPException ex) {
            throw new WebServiceException("Cannot get Soap Message Parts", ex);
        }

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ex) {
            logger.error(null, ex);
        }
        try {
            if (!SUPPORTED_ACTION_URI.equals(getRSTAction(body))) {
                throw new WebServiceException("Only ISSUE action is supported");
            }
        } catch (WSTrustException ex) {
            throw new WebServiceException(ex);
        }
        try {
            if (!SAML20_TOKEN_URN.equals(getRequestedToken(body))) {
                throw new WebServiceException("Only SAML2.0 Tokens are Issued");
            }
        } catch (WSTrustException ex) {
            throw new WebServiceException(ex);
        }

        try {
            // these calls are both getters and checkers of message.
            // So we call them first
            String purposeOfUse = STSUtils.getPurposeOfUse(body);
            String patientID = getPatientID(body);
            String mid = getMessageIdFromHeader(header);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();

            // The response TRC Assertion Issuer.
            SamlTRCIssuer samlTRCIssuer = new SamlTRCIssuer();
            Assertion hcpIdAssertion = getIdAssertionFromHeader(header);
            if (hcpIdAssertion != null) {
                logger.info("hcpIdAssertion: '{}'", hcpIdAssertion.getID());
                if (hcpIdAssertion.getIssueInstant() != null) {
                    logger.info("hcpIdAssertion Issue Instant: '{}'", hcpIdAssertion.getIssueInstant());
                }
            }
            Assertion trc = samlTRCIssuer.issueTrcToken(hcpIdAssertion, patientID, purposeOfUse, null);
            if (hcpIdAssertion != null) {
                logger.info("HCP Assertion Date: '{}' TRC Assertion Date: '{}' -- '{}'",
                        hcpIdAssertion.getIssueInstant().withZone(DateTimeZone.UTC),
                        trc.getIssueInstant().withZone(DateTimeZone.UTC), trc.getAuthnStatements().isEmpty());
            }

            Document signedDoc = builder.newDocument();
            Configuration.getMarshallerFactory().getMarshaller(trc).marshall(trc, signedDoc);

            SOAPMessage response = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
            response.getSOAPBody().addDocument(STSUtils.createRSTRC(signedDoc));
            createResponseHeader(response.getSOAPHeader(), mid);

            String strRespHeader = STSUtils.domElementToString(response.getSOAPHeader());
            String strReqHeader = STSUtils.domElementToString(header);

            String tls_cn = STSUtils.getSSLCertPeer(context.getMessageContext());
            logger.info("tls_cn: '{}'", tls_cn);

            if (context.getUserPrincipal() != null) {
                tls_cn = context.getUserPrincipal().getName();
            }

            audit(samlTRCIssuer.getPointofCare(), samlTRCIssuer.getHumanRequestorNameId(),
                    samlTRCIssuer.getHumanRequestorSubjectId(), samlTRCIssuer.getHRRole(), patientID,
                    samlTRCIssuer.getFacilityType(), trc.getID(), tls_cn, mid,
                    strReqHeader.getBytes(StandardCharsets.UTF_8),
                    getMessageIdFromHeader(response.getSOAPHeader()),
                    strRespHeader.getBytes(StandardCharsets.UTF_8));

            if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                loggerClinical.debug("Outgoing SOAP Message response: '{}'", response);
                log(response);
            }
            return response;

        } catch (SOAPException | WSTrustException | MarshallingException | SMgrException | ParserConfigurationException ex) {
            logger.error(null, ex);
            throw new WebServiceException(ex);
        }
    }


    private String getPatientID(SOAPElement body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "PatientId").item(0) == null) {
            // Cannot be null!
            throw new WebServiceException("Patient ID is Missing from the RST");
        }
        return trcDetails.getElementsByTagNameNS(TRC_NS, "PatientId").item(0).getTextContent();
    }

    private Assertion getIdAssertionFromHeader(SOAPHeader header) throws WSTrustException {
        try {
            // First, find the assertion from the header.
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (header.getElementsByTagNameNS(SAML20_TOKEN_URN, "Assertion").getLength() != 1) {
                throw new WSTrustException("SAML Identity Assertion is missing from the Security Header");
            }
            SOAPElement assertion = (SOAPElement) header.getElementsByTagNameNS(SAML20_TOKEN_URN, "Assertion").item(0);
            Document assertDoc = builder.newDocument();

            Node dupBody = assertDoc.importNode(assertion, true);
            assertDoc.appendChild(dupBody);
            if (assertion == null) {
                return null;
            } else {
                assertDoc.getDocumentElement().setIdAttribute("ID", true);
                SignatureManager sm = new SignatureManager();
                try {
                    sm.verifyEnvelopedSignature(assertDoc);
                } catch (SMgrException ex) {
                    logger.error(null, ex);
                    throw new WSTrustException("Error validating SAML Assertion signature", ex);
                }
            }
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(assertion);
            return (Assertion) unmarshaller.unmarshall(assertDoc.getDocumentElement());

        } catch (ParserConfigurationException | UnmarshallingException ex) {
            logger.error(null, ex);
            throw new WSTrustException("Error Parsing SAML Assertion in Message Header", ex);
        }
    }

    private String getMessageIdFromHeader(SOAPHeader header) {
        if (header.getElementsByTagNameNS(ADDRESSING_NS, "MessageID").getLength() < 1) {
            throw new WebServiceException("Message ID not found in Header");
        }
        String mid = header.getElementsByTagNameNS(ADDRESSING_NS, "MessageID").item(0).getTextContent();
        // PT-236 has to be checked again why is coming like this from soap header
        if (mid.startsWith("uuid"))
            mid = "urn:" + mid;
        return mid;
    }

    private void createResponseHeader(SOAPHeader header, String messageId) {
        try {

            DateTime now = new DateTime();

            SOAPFactory fac = SOAPFactory.newInstance();
            SOAPElement messageIdElem = header.addHeaderElement(new QName(ADDRESSING_NS, "MessageID", "wsa"));
            messageIdElem.setTextContent("uuid:" + UUID.randomUUID().toString());
            SOAPElement securityHeaderElem = header.addHeaderElement(new QName(WS_SEC_NS, "Security", "wsse"));

            SOAPElement timeStampElem = fac.createElement("Timestamp", "wsu", WS_SEC_UTIL_NS);
            SOAPElement ltCreated = fac.createElement("Created", "wsu", WS_SEC_UTIL_NS);
            ltCreated.setTextContent(now.toDateTime(DateTimeZone.UTC).toString());

            SOAPElement ltExpires = fac.createElement("Expires", "wsu", WS_SEC_UTIL_NS);
            ltExpires.setTextContent(now.plusHours(2).toDateTime(DateTimeZone.UTC).toString());

            timeStampElem.addChildElement(ltCreated);
            timeStampElem.addChildElement(ltExpires);

            securityHeaderElem.addChildElement(timeStampElem);

            SOAPElement actionElem = header.addHeaderElement(new QName(ADDRESSING_NS, "Action", "wsa"));
            actionElem.setTextContent("urn:IssueTokenResponse");

            SOAPElement relatesToElem = header.addHeaderElement(new QName(ADDRESSING_NS, "RelatesTo", "wsa"));
            relatesToElem.setTextContent(messageId);

        } catch (SOAPException ex) {
            logger.error(null, ex);
            throw new WebServiceException("Could not create Response Header");
        }
    }

    private String getRSTAction(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").getLength() < 1) {
            throw new WSTrustException("No Request Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").item(0).getTextContent();
    }

    private String getRequestedToken(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").getLength() < 1) {
            throw new WSTrustException("No Token Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").item(0).getTextContent();

    }

    private void audit(String pointOfCareID, String humanRequestorNameID, String humanRequestorSubjectID,
                       String humanRequestorRole, String patientID, String facilityType, String assertionId,
                       String tls_cn, String reqMid, byte[] reqSecHeader, String resMid, byte[] resSecHeader) {

        AuditService auditService = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            logger.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        ConfigurationManager cms = ConfigurationManagerFactory.getConfigurationManager();

        EventLog evLogTRC = EventLog.createEventLogTRCA(TransactionName.epsosTRCAssertion, EventActionCode.EXECUTE,
                date2, EventOutcomeIndicator.FULL_SUCCESS, pointOfCareID, facilityType, cms.getProperty("ncp.country")
                        + "<" + humanRequestorNameID + "@" + cms.getProperty("ncp.country") + ">", humanRequestorRole,
                humanRequestorSubjectID, tls_cn, STSUtils.getServerIP(), cms.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION"),
                patientID, "urn:uuid:" + assertionId, reqMid, reqSecHeader, resMid, resSecHeader,
                STSUtils.getServerIP(), getClientIP(), NcpSide.NCP_B);

        evLogTRC.setEventType(EventType.epsosTRCAssertion);
        auditService.write(evLogTRC, "13", "2");
    }

    private String getClientIP() {

        MessageContext mc = context.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        return req.getRemoteAddr();
    }

    private void log(SOAPMessage message) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.writeTo(out);
        } catch (IOException | SOAPException e) {
            if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                loggerClinical.error("Exception: '{}'", e.getMessage(), e);
            }
        }
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            loggerClinical.info("SOAPMessage:\n{}", out.toString());
        }
    }
}
