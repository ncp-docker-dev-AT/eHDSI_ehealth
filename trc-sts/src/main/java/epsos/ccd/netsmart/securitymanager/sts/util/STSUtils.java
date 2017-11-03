package epsos.ccd.netsmart.securitymanager.sts.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.StringWriter;
import java.net.InetAddress;
import java.security.cert.X509Certificate;


public class STSUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(STSUtils.class);

    // TRC Parameters Namespace
    private static final String TRC_NS = "http://epsos.eu/trc";
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";

    private STSUtils() {
    }

    public static String getPurposeOfUse(SOAPElement body) {

        if (body.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }

        SOAPElement trcDetails = (SOAPElement) body.getElementsByTagNameNS(TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "PurposeOfUse").item(0) == null) {
            return null;
        }

        String purposeOfUse = trcDetails.getElementsByTagNameNS(TRC_NS, "PurposeOfUse").item(0).getTextContent();
        if (purposeOfUse != null && (!"TREATMENT".equals(purposeOfUse) && !"EMERGENCY".equals(purposeOfUse))) {
            throw new WebServiceException("Purpose of Use MUST be either TREATMENT of EMERGENCY");
        }
        return purposeOfUse;
    }

    public static Document createRSTRC(Document assertion) {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document respBody = builder.newDocument();

            Element rstrcElem = respBody.createElementNS(WS_TRUST_NS, "wst:RequestSecurityTokenResponseCollection");
            respBody.appendChild(rstrcElem);

            Element rstrElem = respBody.createElementNS(WS_TRUST_NS, "wst:RequestSecurityTokenResponse");
            rstrcElem.appendChild(rstrElem);

            Element rstElem = respBody.createElementNS(WS_TRUST_NS, "wst:RequestedSecurityToken");
            rstrElem.appendChild(rstElem);

            // add the Assertion
            rstElem.appendChild(respBody.importNode(assertion.getDocumentElement(), true));

            Element tokenTypeElem = respBody.createElementNS(WS_TRUST_NS, "wst:TokenType");
            tokenTypeElem.setTextContent(SAML20_TOKEN_URN);

            rstrElem.appendChild(tokenTypeElem);

            Element lifeTimeElem = respBody.createElementNS(WS_TRUST_NS, "wst:LifeTime");
            rstrElem.appendChild(lifeTimeElem);

            DateTime now = new DateTime();

            Element ltCreated = respBody.createElementNS(WS_SEC_UTIL_NS, "wsu:Created");
            ltCreated.setTextContent(now.toDateTime(DateTimeZone.UTC).toString());

            lifeTimeElem.appendChild(ltCreated);

            Element ltExpires = respBody.createElementNS(WS_SEC_UTIL_NS, "wsu:Expires");
            ltExpires.setTextContent(now.plusHours(2).toDateTime(DateTimeZone.UTC).toString());

            lifeTimeElem.appendChild(ltExpires);

            return respBody;

        } catch (Exception ex) {
            LOGGER.error(null, ex);
            throw new WebServiceException("Cannot create RSTSC Message");
        }
    }

    public static String domElementToString(Element elem) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(elem), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException ex) {
            LOGGER.error(null, ex);
            throw new WebServiceException("Error Creating audit message");
        }
    }

    public static String getServerIP() {

        try {
            InetAddress thisIp = InetAddress.getLocalHost();
            return thisIp.getHostAddress();
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            return "Unknown IP";
        }
    }

    public static String getSSLCertPeer(MessageContext messageContext) {

        String user = "Unknown(No Client Certificate)";

        javax.servlet.ServletRequest sreq = (javax.servlet.ServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);

        if (sreq instanceof HttpServletRequest && sreq.isSecure()) {
            LOGGER.info("Secure and http");
            HttpServletRequest hreq = (HttpServletRequest) sreq;
            X509Certificate[] peerCert = (X509Certificate[]) hreq.getAttribute("javax.servlet.request.X509Certificate");
            if (peerCert != null) {
                return peerCert[0].getSubjectDN().getName();
            }
        }
        return user;
    }
}
