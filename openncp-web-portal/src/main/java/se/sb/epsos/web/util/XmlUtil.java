package se.sb.epsos.web.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

public class XmlUtil {

    /**
     * @param node
     * @return
     * @throws TransformerException
     */
    public static byte[] doc2bytes(Node node) throws TransformerException {

        Source source = new DOMSource(node);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Result result = new StreamResult(out);
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.transform(source, result);
        return out.toByteArray();
    }

    /**
     * @param obj
     * @return
     * @throws JAXBException
     */
    public static String marshallJaxbObject(XmlTypeWrapper<?> obj) throws JAXBException {
        StringWriter writer = new StringWriter();
        if (obj != null && obj.getTypeClass() != null) {
            JAXBContext context = JAXBContext.newInstance(obj.getClass(), obj.getTypeClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(obj, writer);
        }
        return writer.toString();
    }

    /**
     * @param doc
     * @return
     * @throws TransformerException
     */
    public String prettyPrint(Document doc) throws TransformerException {

        StringWriter stringWriter = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}
