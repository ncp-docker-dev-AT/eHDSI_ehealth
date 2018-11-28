package epsos.ccd.netsmart.securitymanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class XMLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

    private XMLUtils() {
    }

    /**
     * @param doc
     * @param out
     */
    public static void sendXMLtoStream(Document doc, OutputStream out) {

        try {

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(out));
        } catch (TransformerException ex) {
            LOGGER.error("TransformerException: ", ex);
        }
    }
}
