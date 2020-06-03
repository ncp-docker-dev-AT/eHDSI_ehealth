package tr.com.srdc.epsos.util;

import eu.europa.ec.sante.ehdsi.openncp.util.security.CryptographicConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XMLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtil.class);

    private XMLUtil() {
    }

    /**
     * returns null if Node is null
     */
    public static Node extractFromDOMTree(Node node) throws ParserConfigurationException {

        if (node == null) {
            return null;
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        document.appendChild(document.importNode(node, true));
        return document.getDocumentElement();
    }

    /**
     * Gets a DOM document and canonicalize it using OMIT_COMMENTS.
     * <p>
     * Add by massi - 29/12/2016
     *
     * @param doc The document to be canonicalized
     * @return the canonicalized document
     * @throws Exception either the document is null, there is no available DOM factory, or a generic c14n error
     */
    public static Document canonicalize(Document doc) throws Exception {

        Canonicalizer canon = Canonicalizer.getInstance(CryptographicConstant.ALGO_ID_C14N_INCL_OMIT_COMMENTS);
        byte[] back = canon.canonicalizeSubtree(doc);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        return documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(back));
    }

    public static Document parseContent(byte[] byteContent) throws ParserConfigurationException, SAXException, IOException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("XMLUtil: parse byte[] content: \n'{}'", byteContent.length);
        }
        String content = new String(byteContent, StandardCharsets.UTF_8);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        StringReader lReader = new StringReader(content);
        InputSource inputSource = new InputSource(lReader);
        return documentBuilder.parse(inputSource);
    }

    public static Document parseContent(String content) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        StringReader lReader = new StringReader(content);
        InputSource inputSource = new InputSource(lReader);
        return documentBuilder.parse(inputSource);
    }

    public static String documentToString(Document doc) throws TransformerException {

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    public static String prettyPrintForValidation(Node node) throws TransformerException, XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", node, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node item = nodeList.item(i);
            item.getParentNode().removeChild(item);
        }

        StringWriter stringWriter = new StringWriter();
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    private static String unPrettyPrint(final String xml) {

        if (StringUtils.isBlank(xml)) {
            throw new RuntimeException("xml was null or blank in unPrettyPrint()");
        }

        final StringWriter sw;

        try {
            final OutputFormat format = OutputFormat.createCompactFormat();
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            sw = new StringWriter();
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
        } catch (Exception e) {
            throw new RuntimeException("Error un-pretty printing xml:\n" + xml, e);
        }
        return sw.toString();
    }

    /**
     * @param node
     * @return
     * @throws TransformerException
     */
    public static String prettyPrint(Node node) throws TransformerException {

        StringWriter stringWriter = new StringWriter();
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    /**
     * @param source
     * @param result
     */
    public static void transformDocument(DOMSource source, Result result) throws TransformerException {

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.transform(source, result);
    }

    /**
     * @param namespaceBindings
     * @return
     */
    public static Map<String, String> parseNamespaceBindings(String namespaceBindings) {

        if (namespaceBindings == null) {
            return null;
        }
        namespaceBindings = namespaceBindings.substring(1, namespaceBindings.length() - 1);
        String[] bindings = namespaceBindings.split(",");
        Map<String, String> namespaces = new HashMap<>();
        for (String binding : bindings) {
            String[] pair = binding.trim().split("=");
            String prefix = pair[0].trim();
            String namespace = pair[1].trim();
            namespaces.put(prefix, namespace);
        }
        return namespaces;
    }

    /**
     * @param object
     * @param context
     * @param schemaLocation
     * @return
     */
    public static Document marshall(Object object, String context, String schemaLocation) {

        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        try {
            JAXBContext jc = JAXBContext.newInstance(context);
            Marshaller marshaller = jc.createMarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaLocation));
            marshaller.setSchema(schema);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            marshaller.marshal(object, doc);
            Locale.setDefault(oldLocale);
            return doc;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Locale.setDefault(oldLocale);
        return null;
    }

    /**
     * @param context
     * @param schemaLocation
     * @param content
     * @return
     */
    public static Object unmarshall(String context, String schemaLocation, String content) {

        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        try {
            JAXBContext jc = JAXBContext.newInstance(context);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaLocation));
            unmarshaller.setSchema(schema);

            Object obj = unmarshaller.unmarshal(new StringReader(content));
            Locale.setDefault(oldLocale);
            return obj;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Locale.setDefault(oldLocale);
        return null;
    }

    /**
     * @param context
     * @param schemaLocation
     * @param content
     * @return
     */
    public static Object unmarshallWithoutValidation(String context, String schemaLocation, String content) {

        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        try {
            JAXBContext jc = JAXBContext.newInstance(context);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaLocation));
            Object obj = unmarshaller.unmarshal(new StringReader(content));
            Locale.setDefault(oldLocale);
            return obj;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Locale.setDefault(oldLocale);
        return null;
    }

    /**
     * @param in
     * @return
     */
    public static Document newDocumentFromInputStream(InputStream in) {

        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document ret = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            ret = builder.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            LOGGER.error("ParserConfigurationException: '{}'", e.getMessage(), e);
        } catch (SAXException | IOException e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return ret;
    }

    public static Node stringToNode(String xml) throws IOException {

        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
        } catch (SAXException | ParserConfigurationException e) {
            return null;
        }
    }

    public static List<Node> getNodeList(Node node, String xpathexpression) {

        List<Node> result;
        try {
            NoNsXpath xpath = new NoNsXpath(xpathexpression);
            result = xpath.selectNodes(node);
        } catch (JaxenException e) {
            LOGGER.error("xpath: " + xpathexpression + ", node: " + node, e);
            return new ArrayList<>();
        }
        return result;
    }
}
