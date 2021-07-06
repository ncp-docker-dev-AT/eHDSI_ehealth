package eu.europa.ec.sante.openncp.protocolterminator.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionUtil.class);
    private static final String OASIS_WSSE_SCHEMA_LOC = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private AssertionUtil() {
    }

//    public static Map<AssertionEnum, Assertion> retrieveAssertionFromSoapHeader(Element soapHeader) {
//
//        LOGGER.info("Retrieving SAML tokens from SOAP Header");
//        NodeList securityList = soapHeader.getElementsByTagNameNS(OASIS_WSSE_SCHEMA_LOC, "Security");
//
//        Element security = (Element) securityList.item(0);
//        NodeList assertionList = security.getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
//        List<Assertion> result = new ArrayList<>();
//
//        for (var i = 0; i < assertionList.getLength(); i++) {
//            Element ass = (Element) assertionList.item(i);
//
//            if (ass.getAttribute("ID").startsWith("urn:uuid:")) {
//                ass.setAttribute("ID", "_" + ass.getAttribute("ID").substring("urn:uuid:".length()));
//            }
//
//            try {
//                // Validate Assertion according to SAML XSD
//                var schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
//                schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(ass));
//                result.add((Assertion) SAML.fromElement(ass));
//
//            } catch (UnmarshallingException | IOException | SAXException ex) {
//                LOGGER.error(null, ex);
//            }
//        }
//        return result;
//    }
}
