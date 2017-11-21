/**
 * Copyright (C) 2011, 2012 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik
 * Tic. Ltd. Sti. <epsos@srdc.com.tr>
 * <p>
 * This file is part of SRDC epSOS NCP.
 * <p>
 * SRDC epSOS NCP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SRDC epSOS NCP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SRDC epSOS NCP. If not, see <http://www.gnu.org/licenses/>.
 */
package tr.com.srdc.epsos.util;

import org.apache.xml.security.c14n.Canonicalizer;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class XMLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtil.class);

    /**
     * Creates a new instance of XMLUtil
     */
    private XMLUtil() {
    }

    /**
     * returns null if Node is null
     */
    public static Node extractFromDOMTree(Node node) throws ParserConfigurationException {

        if (node == null) {
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document theDocument = db.newDocument();
        theDocument.appendChild(theDocument.importNode(node, true));
        return theDocument.getDocumentElement();
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

        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        byte[] back = canon.canonicalizeSubtree(doc);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(back));
    }

    public static org.w3c.dom.Document parseContent(byte[] byteContent) throws ParserConfigurationException, SAXException, IOException {

        org.w3c.dom.Document doc;
        String content = new String(byteContent);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        StringReader lReader = new StringReader(content);
        InputSource inputSource = new InputSource(lReader);
        doc = docBuilder.parse(inputSource);
        return doc;
    }

    /**
     * @param content
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static org.w3c.dom.Document parseContent(String content) throws ParserConfigurationException, SAXException, IOException {

        LOGGER.debug("parseContent(): \n'{}'", content);
        org.w3c.dom.Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        StringReader lReader = new StringReader(content);
        InputSource inputSource = new InputSource(lReader);
        doc = docBuilder.parse(inputSource);
        return doc;
    }

    public static String DocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        //transformer.setOutputProperty(OutputKeys.INDENT, indent);
        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    /**
     * @param node
     * @return
     * @throws TransformerException
     */
    public static String prettyPrint(Node node) throws TransformerException {

        StringWriter stringWriter = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    /**
     * @param doc
     * @param out
     */
    public static void prettyPrint(Document doc, OutputStream out) throws TransformerException, UnsupportedEncodingException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    /**
     * @param namespaceBindings
     * @return
     */
    public static Map<String, String> parseNamespaceBindings(String namespaceBindings) {
        if (namespaceBindings == null) {
            return null;
        }
        //remove { and }
        namespaceBindings = namespaceBindings.substring(1, namespaceBindings.length() - 1);
        String[] bindings = namespaceBindings.split(",");
        Map<String, String> namespaces = new HashMap<>();
        for (String binding : bindings) {
            String[] pair = binding.trim().split("=");
            String prefix = pair[0].trim();
            String namespace = pair[1].trim();
            //Remove ' and '
            //namespace = namespace.substring(1,namespace.length()-1);
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
            JAXBContext jc = JAXBContext.newInstance(
                    context);
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
            JAXBContext jc = JAXBContext.newInstance(
                    context);
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
}
