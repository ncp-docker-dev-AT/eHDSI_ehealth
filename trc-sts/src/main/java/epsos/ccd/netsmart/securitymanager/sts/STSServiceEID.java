package epsos.ccd.netsmart.securitymanager.sts;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import epsos.ccd.netsmart.securitymanager.EvidenceEmitter;
import epsos.ccd.netsmart.securitymanager.NoXACMLEvidenceEmitter;
import epsos.ccd.netsmart.securitymanager.SamlTRCIssuer;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.sts.util.STSUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.ws.*;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

/**
 * This class is the implementation of the unsigned TRC STS manager for the eID level 2.
 * This class is changed because we need to return an unsigned TRC STS (to be signed by the patient).
 * The layout is changed as well, to avoid to pass back and forth the IdA (to avoid to have the IdA crossing several
 * security zones).
 *
 * @author Jerry Dimitriou <jerouris at netsmart.gr>, Massimiliano Masi <massimiliano.masi@tiani-spirit.com>
 */
@ServiceMode(value = Mode.MESSAGE)
@WebServiceProvider(targetNamespace = "http://epsos.eu/", serviceName = "SecurityTokenServiceEID", portName = "ISecurityTokenServiceEID_Port")
@BindingType(value = "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class STSServiceEID implements Provider<SOAPMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(STSServiceEID.class);

    private static final QName Messaging_To = new QName("http://www.w3.org/2005/08/addressing", "To");
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion"; // What
    // can
    // be
    // only
    // requested
    // from
    // the
    // STS
    private static final String SUPPORTED_ACTION_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue"; // Only
    // Issuance
    // is
    // supported
    private static final String TRC_NS = "http://epsos.eu/trc"; // TRC
    // Parameters
    // Namespace
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512"; // WS-Trust
    // Namespace
    private static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing"; // WSA
    // Namespace
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_SEC_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    @Resource
    private WebServiceContext context;
    private String messageIdResponse;

    @Override
    public SOAPMessage invoke(SOAPMessage source) {

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
            LOGGER.error(null, ex);
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
            String doctorId = getDoctorID(body);
            DateTime notBefore = getNotBefore(body);
            DateTime notOnOrAfter = getNotOnOrAfter(body);
            DateTime authnInstant = getAuthNInstant(body);
            DateTime sessionNotOnOrAfter = getSessionNotOnOrAfter(body);
            String idaReference = getIdaReference(body);

            String mid = getMessageIdFromHeader(header);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();

            // The response TRC Assertion Issuer.
            SamlTRCIssuer samlTRCIssuer = new SamlTRCIssuer();

            Assertion trc = samlTRCIssuer.issueTrcTokenUnsigned(purposeOfUse, patientID, doctorId, notBefore, notOnOrAfter,
                    authnInstant, sessionNotOnOrAfter, idaReference);

            Document signedDoc = builder.newDocument();
            Configuration.getMarshallerFactory().getMarshaller(trc).marshall(trc, signedDoc);

            SOAPMessage resp = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
            resp.getSOAPBody().addDocument(STSUtils.createRSTRC(signedDoc));
            createResponseHeader(resp.getSOAPHeader(), mid);

            String strRespHeader = STSUtils.domElementToString(resp.getSOAPHeader());
            String strReqHeader = STSUtils.domElementToString(header);


            String tls_cn = STSUtils.getSSLCertPeer(context.getMessageContext());
            LOGGER.info("tls_cn: '{}'", tls_cn);

            if (context.getUserPrincipal() != null) {
                tls_cn = context.getUserPrincipal().getName();
            }

            DefaultKeyStoreManager lamkm = new DefaultKeyStoreManager();

            X509Certificate clientCert = getSSLCert();
            if (clientCert == null) {
                clientCert = (X509Certificate) lamkm.getDefaultCertificate();
            }
            EvidenceEmitter ee = new NoXACMLEvidenceEmitter(lamkm);
            Element el = ee.emitNRR(UUID.randomUUID().toString(),
                    "urn:esens:policyId:trc",
                    c2s(lamkm.getDefaultCertificate()),
                    new DateTime().toString(),
                    "3",
                    c2s(lamkm.getDefaultCertificate()),
                    c2s(clientCert),
                    "TRC-Issuance",
                    messageIdResponse,
                    doDigest(resp)
            );

            // what should I do here? Let me just print it out, waiting for an official
            // notary service
            OutputFormat format = new OutputFormat();

            format.setLineWidth(65);
            format.setIndenting(false);
            format.setIndent(2);
            format.setEncoding("UTF-8");
            format.setOmitComments(true);
            format.setOmitXMLDeclaration(false);
            format.setVersion("1.0");
            format.setStandalone(true);

            XMLSerializer serializer = new XMLSerializer(System.out, format);
            serializer.serialize(el.getOwnerDocument());
            return resp;

        } catch (Exception ex) {
            LOGGER.error(null, ex);
            throw new WebServiceException(ex);
        }
    }

    private String c2s(Certificate certificate) throws CertificateEncodingException {
        byte[] cert = certificate.getEncoded();
        return Base64.getEncoder().encodeToString(cert);
    }

    private String getIdaReference(SOAPBody body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "IdAReference").item(0) == null) {
            return null;
        }

        return trcDetails
                .getElementsByTagNameNS(TRC_NS, "IdAReference").item(0)
                .getTextContent();
    }

    private DateTime getSessionNotOnOrAfter(SOAPBody body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "SessionNotOnOrAfter").item(0) == null) {
            return null;
        }

        String sessionNotOnOrAfter = trcDetails
                .getElementsByTagNameNS(TRC_NS, "SessionNotOnOrAfter").item(0)
                .getTextContent();

        return new DateTime(sessionNotOnOrAfter);
    }

    private DateTime getAuthNInstant(SOAPBody body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "AuthnInstant").item(0) == null) {
            return null;
        }

        String authnInstant = trcDetails
                .getElementsByTagNameNS(TRC_NS, "AuthnInstant").item(0)
                .getTextContent();

        return new DateTime(authnInstant);
    }

    private DateTime getNotOnOrAfter(SOAPBody body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "NotOnOrAfter").item(0) == null) {
            return null;
        }

        String notOnOrAfter = trcDetails
                .getElementsByTagNameNS(TRC_NS, "NotOnOrAfter").item(0)
                .getTextContent();

        return new DateTime(notOnOrAfter);
    }

    private DateTime getNotBefore(SOAPBody body) {
        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "NotBefore").item(0) == null) {
            return null;
        }

        String notBefore = trcDetails
                .getElementsByTagNameNS(TRC_NS, "NotBefore").item(0)
                .getTextContent();

        return new DateTime(notBefore);
    }

    private String getDoctorID(SOAPBody body) {

        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "DoctorId").item(0) == null) {
            return null;
        }

        return trcDetails.getElementsByTagNameNS(TRC_NS, "DoctorId").item(0).getTextContent();
    }


    private String getPatientID(SOAPElement body) {

        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(
                TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "PatientId").item(0) == null) {
            throw new WebServiceException("Patient ID is Missing from the RST"); // Cannot
            // be
            // null!
        }
        return trcDetails
                .getElementsByTagNameNS(TRC_NS, "PatientId").item(0)
                .getTextContent();
    }

    private String getMessageIdFromHeader(SOAPHeader header) {

        if (header.getElementsByTagNameNS(ADDRESSING_NS, "MessageID")
                .getLength() < 1) {
            throw new WebServiceException("Message ID not found in Header");
        }
        String mid = header.getElementsByTagNameNS(ADDRESSING_NS, "MessageID")
                .item(0).getTextContent();
        // PT-236 has to be checked again why is coming like this from soap
        // header
        if (mid.startsWith("uuid"))
            mid = "urn:" + mid;
        return mid;
    }

    private void createResponseHeader(SOAPHeader header, String messageId) {
        try {

            DateTime now = new DateTime();

            SOAPFactory fac = SOAPFactory.newInstance();
            SOAPElement messageIdElem = header.addHeaderElement(new QName(ADDRESSING_NS, "MessageID", "wsa"));
            messageIdResponse = "uuid:" + UUID.randomUUID().toString();
            messageIdElem.setTextContent(messageIdResponse);

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
            LOGGER.error(null, ex);
            throw new WebServiceException("Could not create Response Header");
        }
    }

    private String getRSTAction(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").getLength() < 1) {
            throw new WSTrustException("No Request Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "RequestType").item(0)
                .getTextContent();
    }

    private String getRequestedToken(SOAPBody body) throws WSTrustException {

        if (body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").getLength() < 1) {
            throw new WSTrustException("No Token Type is Specified.");
        }

        return body.getElementsByTagNameNS(WS_TRUST_NS, "TokenType").item(0)
                .getTextContent();

    }

    private String getClientIP() {

        MessageContext mc = context.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        return req.getRemoteAddr();
    }

    private X509Certificate getSSLCert() {

        MessageContext msgCtx = context.getMessageContext();

        javax.servlet.ServletRequest sreq = (javax.servlet.ServletRequest) msgCtx.get(MessageContext.SERVLET_REQUEST);

        if (sreq instanceof HttpServletRequest && sreq.isSecure()) {
            LOGGER.info("Secure and http");
            HttpServletRequest hreq = (HttpServletRequest) sreq;
            X509Certificate[] peerCert = (X509Certificate[]) hreq.getAttribute("javax.servlet.request.X509Certificate");
            if (peerCert != null) {
                return peerCert[0];
            }
        }
        return null;
    }

    /**
     * Performs a digest over the soapBody.getOwnerDocument().
     *
     * @return The md5 string
     * @throws IOException              in case of I/O error serializing
     * @throws SOAPException            if the document created is wrong
     * @throws NoSuchAlgorithmException If the message digest is not able to find SHA-1
     */
    public final String doDigest(SOAPMessage envelope) throws IOException, SOAPException, NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputFormat format = new OutputFormat();

        format.setIndenting(false);
        format.setIndent(2);
        format.setEncoding("UTF-8");
        format.setOmitComments(true);
        format.setOmitXMLDeclaration(false);
        format.setVersion("1.0");
        format.setStandalone(true);


        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.serialize(envelope.getSOAPBody().getOwnerDocument());
        md.update(outputStream.toByteArray());
        return Base64.getEncoder().encodeToString(md.digest());
    }
}
