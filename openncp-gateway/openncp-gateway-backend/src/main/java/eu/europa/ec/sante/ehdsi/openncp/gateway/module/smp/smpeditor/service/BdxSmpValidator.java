package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.io.IOException;

/**
 * Utility validation class allowing to verify the conformance of the SMP File according the XSD definition.
 */
public class BdxSmpValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BdxSmpValidator.class);
    private static final String BDX_SMP_XSD = "/bdx-smp-201605.xsd";
    private static final String EHDSI_ISM_XSD = "/ehdsi-ism-2020.xsd";
    private static final String NS_BDXR_SMP = "http://docs.oasis-open.org/bdxr/ns/SMP/2016/05";
    private static final String NS_PREFIX_BDXR_SMP = "bdxr";
    private static final String ISM_DOCUMENT_ID = "urn:ehealth:ISM::InternationalSearchMask##ehealth-107";

    private BdxSmpValidator() {
    }

    /**
     * Evaluates XPath Expression over the SMP Document.
     *
     * @param document   SMP File as XML Document.
     * @param expression XPath Expression.
     * @return NodeList elements according the XPath Expression if exists.
     * @throws XPathExpressionException If <code>expression</code> cannot be evaluated
     */
    private static NodeList evaluateExpression(Document document, String expression) throws XPathExpressionException {

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bindNamespaceUri(NS_PREFIX_BDXR_SMP, NS_BDXR_SMP);
        xpath.setNamespaceContext(namespaceContext);

        XPathExpression expr = xpath.compile(expression);
        Object result = expr.evaluate(document, XPathConstants.NODESET);

        return (NodeList) result;
    }

    /**
     * Gets the SMP Document Identifier from the Document passed in parameter.
     *
     * @param document SMP File as XML Document.
     * @return The Document Identifier.
     */
    private static String getDocumentId(Document document) {

        String expressionDocId = "//bdxr:ServiceMetadata/bdxr:ServiceInformation/bdxr:DocumentIdentifier";
        try {

            NodeList nodeList = evaluateExpression(document, expressionDocId);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
            return "";

        } catch (XPathExpressionException e) {
            return "";
        }
    }

    /**
     * Validates BDX SMP File according the Oasis BDX XSD.
     *
     * @param xmlStream SMP File as String.
     * @return true | false according the XSD validation result.
     */
    public static boolean validateFile(String xmlStream) {

        try {
            Document document = XmlUtil.parse(xmlStream);
            if (StringUtils.equals(getDocumentId(document), ISM_DOCUMENT_ID) && !validateFileExtension(xmlStream)) {

                return false;
            }
            return XMLValidator.validate(xmlStream, BDX_SMP_XSD);

        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.error("Exception: '{}'", e.getMessage());
            return false;
        }
    }

    /**
     * Validates BDX SMP File Extension according the eHDSI International Search Mask XSD.
     *
     * @param xmlStream SMP File as String.
     * @return true | false according the XSD validation result.
     */
    public static boolean validateFileExtension(String xmlStream) {

        String expression = "//bdxr:ServiceMetadata/bdxr:ServiceInformation/bdxr:ProcessList/bdxr:Process/bdxr:ServiceEndpointList/bdxr:Endpoint/bdxr:Extension";

        try {
            Document document = XmlUtil.parse(xmlStream);
            NodeList nodeList = evaluateExpression(document, expression);

            if (nodeList.getLength() > 0) {
                Node extension = nodeList.item(0).getFirstChild();
                return XMLValidator.validate(XmlUtil.nodeToString(extension), EHDSI_ISM_XSD);
            }

        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException | TransformerException e) {
            LOGGER.error("Exception: '{}'", e.getMessage());
            return false;
        }
        return false;
    }
}
