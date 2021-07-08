package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.util.TMConstants;
import epsos.ccd.posam.tm.util.XmlUtil;
import junit.framework.TestCase;
import org.jaxen.dom.DOMXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

public class XmlTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlTest.class);
    private final String samplesDir = "./src/test/resources/samples/";

    public void testNameSpace() {

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {

                }
            });
            reader.parse(new InputSource(new FileReader(samplesDir + "unstructuredCDANS.xml")));

            Document document = XmlUtil.getDocument(new File(samplesDir + "unstructuredCDANS.xml"), false);
            printNode(document);
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
    }

    public void printNode(Node n) {

        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            child.getNodeType();
            if (child.hasChildNodes()) {
                printNode(child);
            }
        }
    }

    public void testXpath() {

        Document doc = XmlUtil.getDocument(new File(samplesDir + "unstructuredCDANS.xml"), true);
        try {
            DOMXPath xpath = new DOMXPath("/my:ClinicalDocument/my:code");
            xpath.setNamespaceContext(arg0 -> "urn:hl7-org:v3");
            List<Node> result = xpath.selectNodes(doc);
            assertTrue(result.size() > 0);
            for (Node node : result) {
                LOGGER.info(node.getNamespaceURI() + ": " + node.getNodeName());
            }

            XPath jxpath = XPathFactory.newInstance().newXPath();
            jxpath.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {

                public Iterator getPrefixes(String namespaceURI) {
                    return null;
                }

                public String getPrefix(String namespaceURI) {

                    return "my";
                }

                public String getNamespaceURI(String prefix) {

                    return "urn:hl7-org:v3";
                }
            });
            XPathExpression expr = jxpath.compile("/my:ClinicalDocument/my:code");

            NodeList nodeList = (NodeList) expr.evaluate(doc,
                    XPathConstants.NODESET);
            assertTrue(nodeList.getLength() > 0);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                LOGGER.info(n.getNamespaceURI() + ": " + n.getNodeName());
            }
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
    }

    public void testNodePath() {

        Document doc = XmlUtil.getDocument(new File(samplesDir + "drda_L3.xml"), true);
        List<Node> list = XmlUtil.getNodeList(doc, TMConstants.XPATH_ALL_ELEMENTS_WITH_CODE_ATTR);
        for (Node node : list) {
            String elementPath = XmlUtil.getElementPath(node);
            LOGGER.info(elementPath);
            elementPath = elementPath.replaceAll("\\w+:", "");
            Node node2 = XmlUtil.getNode(doc, elementPath);
            assertNotNull(elementPath + " je null", node2);
            assertEquals(node.getLocalName(), node2.getLocalName());
            assertEquals(1, XmlUtil.getNodeList(doc, elementPath).size());
        }
    }
}
