package epsos.ccd.posam.tm.util;

import org.apache.commons.lang.StringUtils;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for TM specific XML manipulation methods
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.10, 2010, 20 October
 */
public class XmlUtil implements TMConstants {

    private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);

    private XmlUtil() {
    }

    /**
     * Simply prints Node as String (useful for logging/testing purpose)
     *
     * @param node
     * @return String
     */
    public static String xmlToString(Node node) {

        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            log.error("xmlToString error: ", e);
        }
        return null;
    }

    public static String nodeListToString(List<Node> nodes) {
        try {
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            for (Node node : nodes) {
                Source source = new DOMSource(node);
                transformer.transform(source, result);
            }

            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            log.error("xmlToString error: ", e);
        }
        return null;
    }

    /**
     * Creates DOM Document from File
     *
     * @param file           XML File
     * @param namespaceAware boolean parameter, determines if Document is namespaceAware or
     *                       not
     * @return Document
     */
    public static Document getDocument(File file, boolean namespaceAware) {

        Document document = null;
        try {
            // parse an XML document into a DOM tree
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            documentFactory.setNamespaceAware(namespaceAware);
            DocumentBuilder parser = documentFactory.newDocumentBuilder();

            document = parser.parse(file);
        } catch (Exception e) {
            log.error("getDocument error: ", e);
        }
        return document;
    }

    /**
     * Creates DOM Document from ByteArray
     *
     * @param xml            input ByteArray
     * @param namespaceAware boolean parameter, determines if Document is namespaceAware or
     *                       not
     * @return Document
     */
    public static Document bytesToXml(byte[] xml, boolean namespaceAware) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("bytesToXml error: ", e);
        }
        return null;
    }

    /**
     * Returns ByteArray from input Node
     *
     * @param node Input Node (Document for example)
     * @return ByteArray
     */
    public static byte[] doc2bytes(Node node) {

        try {
            Source source = new DOMSource(node);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Result result = new StreamResult(out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return out.toByteArray();
        } catch (TransformerException e) {
            log.error("doc2bytes error: ", e);
        }
        return new byte[]{};
    }

    /**
     * Returns namespaceAware Document (namespaceAware Document is required bye
     * validation against schema)
     *
     * @param inputDocument Document
     * @return output NamespaceAware Document
     */
    public static Document getNamespaceAwareDocument(Document inputDocument) {
        return bytesToXml(doc2bytes(inputDocument), true);
    }

    public static Document getNamespaceAwareDocument(byte[] inputDocumentBytes) {
        return bytesToXml(inputDocumentBytes, true);
    }

    /**
     * Returns namespace NOT Aware Document (namespace NOT Aware Document is required bye XPath evaluation)
     *
     * @param inputDocument Document
     * @return output Namespace NOT aware Document
     */
    public static Document getNamespaceNOTAwareDocument(Document inputDocument) {
        return bytesToXml(doc2bytes(inputDocument), false);
    }

    /**
     * Using XPath expression evaluates input node
     *
     * @param node            input Document
     * @param xpathexpression XPath expression to be evaluated
     * @return NodeList
     */
    public static List<Node> getNodeList(Node node, String xpathexpression) {

        List<Node> result;
        try {
            NoNsXpath xpath = new NoNsXpath(xpathexpression);
            result = xpath.selectNodes(node);
        } catch (JaxenException e) {
            log.error("xpath: " + xpathexpression + ", node: " + node, e);
            return new ArrayList<>();
        }
        return result;
    }

    public static Node getNode(Node node, String xpathexpression) {

        try {
            NoNsXpath xpath = new NoNsXpath(xpathexpression);
            return (Node) xpath.selectSingleNode(node);
        } catch (JaxenException e) {
            log.error("xpath: " + xpathexpression + ", node: " + node, e);
            return null;
        }
    }

    public static Document stringToDom(String xmlSource) throws SAXException, ParserConfigurationException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

    /**
     * Workaround how to avoid empty xmlns="" by processing document
     *
     * @param input
     * @return
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static Document removeEmptyXmlns(Document input) throws SAXException, ParserConfigurationException, IOException {

        String s = xmlToString(input);
        s = StringUtils.replace(s, EMPTY_XMLNS, EMPTY_STRING);
        return stringToDom(s);
    }

    /**
     * Returns InputStream from Document
     *
     * @param node
     * @return InputStream
     * @throws TransformerException
     */
    public static InputStream nodeToInputStream(Node node) throws TransformerException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource(node), outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public static String getElementPath(Node e) {

        ArrayList<String> path = new ArrayList<>();
        Node parent = null;
        while (!(parent instanceof Document)) {
            String name = e.getLocalName();
            Node prev = e;
            Node next = e;
            int pos = 1;
            Node tmp = e;
            while (prev != null) {
                prev = tmp.getPreviousSibling();
                if (prev != null) {
                    tmp = prev;
                    if (prev.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if (prev.getLocalName().equals(name)) {
                        pos++;
                    }
                }
            }
            // ak je pos null, skontrolujem aj nasledujce elemnty
            // ci je viac takych z rovnakym menom
            boolean more = false;
            if (pos == 1) {
                tmp = e;
                while (next != null) {
                    next = tmp.getNextSibling();
                    if (next != null) {
                        tmp = next;
                        if (next.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        if (next.getLocalName() != null && next.getLocalName().equals(name)) {
                            more = true;
                            break;
                        }
                    }
                }
            }
            if (e.getPrefix() != null && e.getPrefix().length() > 0) {
                name = e.getPrefix() + ":" + name;
            }
            if (pos > 1 || more) {
                name = name + "[" + pos + "]";
            }
            path.add(name);
            parent = e.getParentNode();
            e = parent;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            sb.append("/");
            sb.append(path.get(i));
        }
        return sb.toString();
    }
}

class CdaNameSpaceContext implements NamespaceContext {

    private static final String NS_CDA = "urn:hl7-org:v3";

    public String getNamespaceURI(String prefix) {
        if ("nsCda".equals(prefix)) {
            return NS_CDA;
        }
        return null;
    }

    public String getPrefix(String namespaceURI) {
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
