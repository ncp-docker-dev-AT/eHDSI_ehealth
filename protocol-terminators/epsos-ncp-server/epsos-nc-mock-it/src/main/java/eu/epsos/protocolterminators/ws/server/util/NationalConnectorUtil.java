package eu.epsos.protocolterminators.ws.server.util;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.common.xml.SAMLSchemaBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;

public class NationalConnectorUtil {

    private NationalConnectorUtil() {
    }

    private static Assertion getAssertionFromSOAPHeader(Element soapHeader, boolean advice) {

        NodeList securityList = soapHeader.getElementsByTagNameNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
        if (securityList.getLength() > 0) {

            Element security = (Element) securityList.item(0);
            NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");

            if (assertionList.getLength() > 0) {
                for (int i = 0; i < assertionList.getLength(); i++) {

                    try {
                        Element assertionElement = (Element) assertionList.item(i);
                        // Validate Assertion according to SAML XSD
                        SAMLSchemaBuilder schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                        schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(assertionElement));
                        Assertion anAssertion = (Assertion) fromElement(assertionElement);
                        if (advice && anAssertion.getAdvice() != null) {
                            return anAssertion;
                        } else if (!advice && anAssertion.getAdvice() == null) {
                            return anAssertion;
                        }
                    } catch (SAXException | IOException | UnmarshallingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns HCP assertion received by NCP-A and passed to National Connector.
     *
     * @param soapHeader - SOAP Header message received by National Connector
     * @return HCP Assertion passed to National Connector.
     */
    public static Assertion getHCPAssertionFromSOAPHeader(Element soapHeader) {
        return getAssertionFromSOAPHeader(soapHeader, Boolean.FALSE);
    }

    /**
     * Returns TRC assertion received by NCP-A and passed to National Connector.
     *
     * @param soapHeader - SOAP Header message received by National Connector
     * @return TRC Assertion passed to National Connector.
     */
    public static Assertion getTRCAssertionFromSOAPHeader(Element soapHeader) {
        return getAssertionFromSOAPHeader(soapHeader, Boolean.TRUE);
    }

    /**
     * Helper method to read an XML object from a DOM element.
     */
    public static XMLObject fromElement(Element element) throws UnmarshallingException {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
    }
}
