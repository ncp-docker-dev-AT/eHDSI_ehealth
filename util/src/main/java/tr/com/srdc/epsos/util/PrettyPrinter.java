package tr.com.srdc.epsos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * This class "pretty prints" an XML stream to something more human-readable.
 * It duplicates the character content with some modifications to whitespace,
 * restoring line breaks and a simple pattern of indenting child elements.
 * <p>
 * This version of the class acts as a SAX 2.0 <code>DefaultHandler</code>,
 * so to provide the unformatted XML just pass a new instance to a SAX parser.
 * Its output is via the {@link #toString toString} method.
 * <p>
 * One major limitation:  we gather character data for elements in a single
 * buffer, so mixed-content documents will lose a lot of data!  This works
 * best with data-centric documents where elements either have single values
 * or child elements, but not both.
 *
 * @author Will Provost
 */
/*
Copyright 2002-2003 by Will Provost.
All rights reserved.
*/
public class PrettyPrinter extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrettyPrinter.class);

    private static final String EXCEPTION = "Exception: ";
    private static final String MESSAGE = " Message: \n";
    private static final String STANDARD_INDENT = "  ";
    private static final String END_LINE = System.getProperty("line.separator");
    private static final String NS_SAX_FEATURES_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    /**
     * The primary buffer for accumulating the formatted XML.
     */
    private final StringBuilder output = new StringBuilder();
    /**
     * This whitespace string is expanded and collapsed to manage the output indenting.
     */
    private String indent = "";
    /**
     * A buffer for character data.  It is &quot;enabled&quot; in {@link #startElement startElement} by being
     * initialized to a new <b>StringBuffer</b>, and then read and reset to <code>null</code>
     * in {@link #endElement endElement}.
     */
    private StringBuilder currentValue = null;
    private boolean justHitStartTag;

    /**
     * Convenience method to wrap pretty-printing SAX pass over existing content.
     */
    public static String prettyPrint(byte[] content) {

        try {
            PrettyPrinter pretty = new PrettyPrinter();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(NS_SAX_FEATURES_PREFIXES, true);
            factory.newSAXParser().parse(new ByteArrayInputStream(content), pretty);
            return pretty.toString();
        } catch (Exception ex) {
            LOGGER.error("PrettyPrint byte[] Exception: '{}'", ex.getMessage(), ex);
            return EXCEPTION + ex.getClass().getName() + MESSAGE + ex.getMessage();
        }
    }

    /**
     * Convenience method to wrap pretty-printing SAX pass over existing content.
     */
    public static String prettyPrint(String content) {

        try {
            PrettyPrinter pretty = new PrettyPrinter();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(NS_SAX_FEATURES_PREFIXES, true);
            factory.newSAXParser().parse(content, pretty);
            return pretty.toString();
        } catch (Exception ex) {
            LOGGER.error("PrettyPrint String Exception: '{}'", ex.getMessage(), ex);
            return EXCEPTION + ex.getClass().getName() + MESSAGE + ex.getMessage();
        }
    }

    /**
     * Convenience method to wrap pretty-printing SAX pass over existing content.
     */
    public static String prettyPrint(InputStream content) {

        try {
            PrettyPrinter pretty = new PrettyPrinter();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(NS_SAX_FEATURES_PREFIXES, true);
            factory.newSAXParser().parse(content, pretty);
            return pretty.toString();
        } catch (Exception ex) {
            LOGGER.error("PrettyPrint InputStream Exception: '{}'", ex.getMessage(), ex);
            return EXCEPTION + ex.getClass().getName() + MESSAGE + ex.getMessage();
        }
    }

    /**
     * Convenience method to wrap pretty-printing SAX pass over existing content.
     */
    public static String prettyPrint(Document doc) throws TransformerException {

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(buffer));

            return buffer.toString("UTF-8").replaceAll("(>)([ \t]+<)", "$1\n$2");

        } catch (Exception ex) {
            LOGGER.error("PrettyPrint Document Exception: '{}'", ex.getMessage(), ex);
            return EXCEPTION + ex.getClass().getName() + MESSAGE + ex.getMessage();
        }
    }

    /**
     * Filter to pass strings to output, escaping <b>&lt;</b> and <b>&amp;</b> characters to &amp;lt; and &amp;amp; respectively.
     */
    private static String escape(char[] chars, int start, int length) {

        StringBuilder result = new StringBuilder();
        for (int c = start; c < start + length; ++c)
            if (chars[c] == '<')
                result.append("&lt;");
            else if (chars[c] == '&')
                result.append("&amp;");
            else
                result.append(chars[c]);

        return result.toString();
    }

    /**
     * Call this to get the formatted XML post-parsing.
     */
    public String toString() {
        return output.toString();
    }

    /**
     * Prints the XML declaration.
     */
    @Override
    public void startDocument() throws SAXException {

        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append(END_LINE);
    }

    /**
     * Prints a blank line at the end of the reformatted document.
     */
    @Override
    public void endDocument() throws SAXException {
        output.append(END_LINE);
    }

    /**
     * Writes the start tag for the element.
     * Attributes are written out, one to a text line.  Starts gathering character data for the element.
     */
    @Override
    public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException {

        if (justHitStartTag) {
            output.append('>');
        }

        output.append(END_LINE).append(indent).append('<').append(qName);
        int length = attributes.getLength();
        for (int a = 0; a < length; ++a)
            output.append(END_LINE).append(indent).append(STANDARD_INDENT).append(attributes.getQName(a))
                    .append("=\"").append(attributes.getValue(a)).append('\"');

        if (length > 0) {
            output.append(END_LINE).append(indent);
        }

        indent += STANDARD_INDENT;
        currentValue = new StringBuilder();
        justHitStartTag = true;
    }

    /**
     * Checks the {@link #currentValue} buffer to gather element content.
     * Writes this out if it is available.  Writes the element end tag.
     */
    @Override
    public void endElement(String uri, String name, String qName) throws SAXException {

        indent = indent.substring(0, indent.length() - STANDARD_INDENT.length());

        if (currentValue == null) {
            output.append(indent)
                    .append("</")
                    .append(qName)
                    .append('>');
        } else if (currentValue.length() != 0) {
            output.append('>')
                    .append(currentValue.toString())
                    .append("</")
                    .append(qName)
                    .append('>');
        } else {
            output.append("/>");
        }

        currentValue = null;
        justHitStartTag = false;
    }

    /**
     * When the {@link #currentValue} buffer is enabled, appends character data into it, to be gathered when the element
     * end tag is encountered.
     */
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {

        if (currentValue != null) {
            currentValue.append(escape(chars, start, length));
        }
    }

    public static class StreamAdapter extends OutputStream {

        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer finalDestination;

        public StreamAdapter(Writer finalDestination) {
            this.finalDestination = finalDestination;
        }

        @Override
        public void write(int b) {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            out.write(b, off, len);
        }

        public void flushPretty() throws IOException {

            PrintWriter finalPrinter = new PrintWriter(finalDestination);
            finalPrinter.println(PrettyPrinter.prettyPrint(out.toByteArray()));
            finalPrinter.close();
            out.close();
        }
    }
}
