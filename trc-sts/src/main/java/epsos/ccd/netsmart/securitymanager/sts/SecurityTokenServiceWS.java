package epsos.ccd.netsmart.securitymanager.sts;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import epsos.ccd.netsmart.securitymanager.SignatureManager;
import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.sts.util.STSUtils;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.util.CertUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

public class SecurityTokenServiceWS {

    public static final String MESSAGE_ID = "MessageID";
    public static final QName Messaging_To = new QName("http://www.w3.org/2005/08/addressing", "To");
    public static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    public static final String SUPPORTED_ACTION_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue";
    public static final String NOK_NS = "https://ehdsi.eu/assertion/nok";
    public static final String TRC_NS = "https://ehdsi.eu/assertion/trc";
    public static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static final String WS_SEC_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            // SAML Framework cannot be initialized correctly.
        }
    }

    private final Logger logger = LoggerFactory.getLogger(SecurityTokenServiceWS.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Resource
    public WebServiceContext context;

    public SecurityTokenServiceWS() {
        // Default empty constructor.
    }

    public void createResponseHeader(SOAPHeader header, String messageId) {

        try {
            var now = new DateTime();

            var soapFactory = SOAPFactory.newInstance();
            SOAPElement messageIdElem = header.addHeaderElement(new QName(ADDRESSING_NS, MESSAGE_ID, "wsa"));
            messageIdElem.setTextContent("uuid:" + UUID.randomUUID());
            SOAPElement securityHeaderElem = header.addHeaderElement(new QName(WS_SEC_NS, "Security", "wsse"));

            SOAPElement timeStampElem = soapFactory.createElement("Timestamp", "wsu", WS_SEC_UTIL_NS);
            SOAPElement ltCreated = soapFactory.createElement("Created", "wsu", WS_SEC_UTIL_NS);
            ltCreated.setTextContent(now.toDateTime(DateTimeZone.UTC).toString());

            SOAPElement ltExpires = soapFactory.createElement("Expires", "wsu", WS_SEC_UTIL_NS);
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

    public String getCertificateCommonName(Assertion hcpIdAssertion) {

        //TODO: Test Certificate CN retrieve
        var keyInfo = hcpIdAssertion.getSignature().getKeyInfo();
        for (X509Data x509Data : keyInfo.getX509Datas()) {
            for (org.opensaml.xmlsec.signature.X509Certificate x509Certificate : x509Data.getX509Certificates()) {
                logger.info("[SAML] Signature certificate:\n'{}' ", x509Certificate.getValue());

                byte[] encodedCert = Base64.getDecoder().decode(removeDisplayCharacter(x509Certificate.getValue()));
                InputStream inputStream = new ByteArrayInputStream(encodedCert);

                CertificateFactory certFactory;
                try {
                    certFactory = CertificateFactory.getInstance("X.509");
                    java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) certFactory
                            .generateCertificate(inputStream);
                    logger.info(getCommonName(cert));
                    return getCommonName(cert);
                } catch (CertificateException e) {
                    logger.error("CertificateException: '{}'", e.getMessage());
                }
            }
        }
        return STSUtils.NO_CLIENT_CERTIFICATE;
    }

    public String getClientIP() {

        try {
            var messageContext = context.getMessageContext();
            var httpServletRequest = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
            var clientIpAddressAsString = httpServletRequest.getRemoteAddr();
            logger.debug("clientIpAddress: '{}'", clientIpAddressAsString);
            var clientIpAddress = InetAddress.getByName(clientIpAddressAsString);
            if (!clientIpAddress.isLinkLocalAddress() && !clientIpAddress.isLoopbackAddress()) {
                return clientIpAddressAsString;
            } else {
                return STSUtils.getSTSServerIP();
            }
        } catch (UnknownHostException ex) {
            logger.error("UnknownHostException: '{}'", ex.getMessage());
        }
        return "Could not get client IP address!";
    }

    public String getCommonName(X509Certificate certificate) {
        return CertUtil.subjectCN(certificate);
    }

    public Assertion getIdAssertionFromHeader(SOAPHeader header) throws WSTrustException {

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
                var signatureManager = new SignatureManager();
                signatureManager.verifyEnvelopedSignature(assertDoc);
            }
            var unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            var unmarshaller = unmarshallerFactory.getUnmarshaller(assertion);
            return (Assertion) unmarshaller.unmarshall(assertDoc.getDocumentElement());

        } catch (SMgrException ex) {
            throw new WSTrustException("Error validating SAML Assertion signature", ex);
        } catch (ParserConfigurationException | UnmarshallingException ex) {
            throw new WSTrustException("Error Parsing SAML Assertion in Message Header", ex);
        }
    }

    public String getMessageIdFromHeader(SOAPHeader header) {

        if (header.getElementsByTagNameNS(ADDRESSING_NS, MESSAGE_ID).getLength() < 1) {
            throw new WebServiceException("Message ID not found in Header");
        }
        String messageID = header.getElementsByTagNameNS(ADDRESSING_NS, MESSAGE_ID).item(0).getTextContent();
        if (messageID.startsWith("uuid"))
            messageID = "urn:" + messageID;
        return messageID;
    }

    public String getPatientID(SOAPElement body) {

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

    public String getRSTAction(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").getLength() < 1) {
            throw new WSTrustException("No Request Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").item(0).getTextContent();
    }

    public String getRequestedToken(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").getLength() < 1) {
            throw new WSTrustException("No Token Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").item(0).getTextContent();
    }

    public void log(SOAPMessage message) {
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            try (var out = new ByteArrayOutputStream()) {
                message.writeTo(out);
                loggerClinical.debug("SOAPMessage:\n{}", out);
            } catch (IOException | SOAPException e) {
                loggerClinical.error("Exception: '{}'", e.getMessage(), e);
            }
        }
    }

    private String removeDisplayCharacter(String certificateValue) {

        String certificatePEM = RegExUtils.removeAll(certificateValue, "-----BEGIN CERTIFICATE-----");
        certificatePEM = RegExUtils.removeAll(certificatePEM, "-----END CERTIFICATE-----");
        certificatePEM = RegExUtils.removeAll(certificatePEM, StringUtils.LF);
        certificatePEM = RegExUtils.removeAll(certificatePEM, StringUtils.CR);
        return certificatePEM;
    }
}
