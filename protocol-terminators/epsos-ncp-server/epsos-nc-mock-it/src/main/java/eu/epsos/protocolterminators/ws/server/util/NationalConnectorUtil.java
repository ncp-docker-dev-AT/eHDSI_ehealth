package eu.epsos.protocolterminators.ws.server.util;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.common.xml.SAMLSchemaBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.impl.AssertionMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Validator;
import java.io.IOException;

public class NationalConnectorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NationalConnectorUtil.class);

    private NationalConnectorUtil() {
    }

    private static Assertion getAssertionFromSOAPHeader(Element soapHeader, String type) {

        NodeList securityList = soapHeader.getElementsByTagNameNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
        if (securityList != null && securityList.getLength() > 0) {

            Element security = (Element) securityList.item(0);
            NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");

            if (assertionList.getLength() > 0) {
                for (int i = 0; i < assertionList.getLength(); i++) {
                    try {
                        Element assertionElement = (Element) assertionList.item(i);
                        // Validate Assertion according to SAML XSD
                        SAMLSchemaBuilder schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                        Validator validator = schemaBuilder.getSAMLSchema().newValidator();
                        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                        validator.validate(new DOMSource(assertionElement));
                        Assertion assertion = (Assertion) fromElement(assertionElement);
                        if (StringUtils.equals(type, assertion.getIssuer().getNameQualifier())) {
                            return assertion;
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
     * Helper method to read an XML object from a DOM element.
     */
    public static XMLObject fromElement(Element element) throws UnmarshallingException {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
    }

    public static String getDocumentAsXml(Document document, boolean header) {

        var response = "";
        try {
            DOMSource domSource = new DOMSource(document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            String omit;
            if (header) {
                omit = "no";
            } else {
                omit = "yes";
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omit);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            var stringWriter = new java.io.StringWriter();
            StreamResult sr = new StreamResult(stringWriter);
            transformer.transform(domSource, sr);
            response = stringWriter.toString();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return response;
    }

    /**
     * Returns HCP assertion received by NCP-A and passed to National Connector.
     *
     * @param soapHeader - SOAP Header message received by National Connector
     * @return HCP Assertion passed to National Connector.
     */
    public static Assertion getHCPAssertionFromSOAPHeader(Element soapHeader) {
        return getAssertionFromSOAPHeader(soapHeader, "urn:ehdsi:assertions:hcp");
    }

    /**
     * Returns NoK assertion received by NCP-A and passed to National Connector.
     *
     * @param soapHeader - SOAP Header message received by National Connector
     * @return Next of Kin Assertion passed to National Connector.
     */
    public static Assertion getNoKAssertionFromSOAPHeader(Element soapHeader) {
        return getAssertionFromSOAPHeader(soapHeader, "urn:ehdsi:assertions:nok");
    }

    /**
     * Returns TRC assertion received by NCP-A and passed to National Connector.
     *
     * @param soapHeader - SOAP Header message received by National Connector
     * @return TRC Assertion passed to National Connector.
     */
    public static Assertion getTRCAssertionFromSOAPHeader(Element soapHeader) {
        return getAssertionFromSOAPHeader(soapHeader, "urn:ehdsi:assertions:trc");
    }

    public static void logAssertionAsXml(Assertion assertion) {

        try {
            var marshaller = new AssertionMarshaller();
            Element element = marshaller.marshall(assertion);
            Document document = element.getOwnerDocument();
            LOGGER.info("Assertion: '{}'\n'{}'", assertion.getID(), getDocumentAsXml(document, false));
        } catch (MarshallingException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
