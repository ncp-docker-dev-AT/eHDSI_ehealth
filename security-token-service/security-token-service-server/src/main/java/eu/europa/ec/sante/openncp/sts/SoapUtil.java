package eu.europa.ec.sante.openncp.sts;

import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.SOAPElement;
import javax.xml.ws.WebServiceException;

public class SoapUtil {

    // TRC Parameters Namespace
    private static final String TRC_NS = "https://ehdsi.eu/trc";
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";

    private SoapUtil() {
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
        if (purposeOfUse != null && (!StringUtils.equals("TREATMENT", purposeOfUse) && !StringUtils.equals("EMERGENCY", purposeOfUse))) {
            throw new WebServiceException("Purpose of Use MUST be either TREATMENT of EMERGENCY");
        }
        return purposeOfUse;
    }
}
