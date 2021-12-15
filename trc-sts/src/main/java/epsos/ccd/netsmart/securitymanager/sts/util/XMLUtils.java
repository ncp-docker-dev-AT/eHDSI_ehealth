package epsos.ccd.netsmart.securitymanager.sts.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.StringWriter;

public class XMLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

    private XMLUtils() {
    }

    public static void sendXMLtoStream(Document doc, OutputStream out) {

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
        } catch (TransformerException ex) {
            LOGGER.error(null, ex);
        }
    }

    public static String asString(Node node) {

        var writer = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            if (!(node instanceof Document)) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (final TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
        return writer.toString();
    }
}
