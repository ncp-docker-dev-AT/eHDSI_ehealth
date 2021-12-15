package eu.europa.ec.sante.openncp.sts;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceException;

public class SecurityTokenServiceUtil {

    public static final String NO_CLIENT_CERTIFICATE = "Unknown (No Client Certificate)";
    // Issuance is supported
    private static final String TRC_NS = "https://ehdsi.eu/assertion/trc";
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityTokenServiceUtil.class);
    // TRC Parameters Namespace
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";

    private SecurityTokenServiceUtil() {
    }

    public static String getPatientID(Document document) {

        if (document.getElementsByTagNameNS(TRC_NS, "TRCParameters").getLength() < 1) {
            throw new WebServiceException("No TRC Parameters in RST");
        }
        Element trcDetails = (Element) document.getElementsByTagNameNS(TRC_NS, "TRCParameters").item(0);
        if (trcDetails.getElementsByTagNameNS(TRC_NS, "PatientId").item(0) == null) {
            // Cannot be null!
            throw new WebServiceException("Patient ID is Missing from the RST");
        }
        return trcDetails.getElementsByTagNameNS(TRC_NS, "PatientId").item(0).getTextContent();
    }

    public static Document createRequestSecurityTokenResponse(Document assertion) {

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

            var now = new DateTime();
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
}
