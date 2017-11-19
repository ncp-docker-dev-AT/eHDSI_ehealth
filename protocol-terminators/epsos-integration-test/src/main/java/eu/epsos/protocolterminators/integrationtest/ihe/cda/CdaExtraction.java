package eu.epsos.protocolterminators.integrationtest.ihe.cda;

import eu.epsos.protocolterminators.integrationtest.common.AbstractIT;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.dto.DetailedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.xpath.XPathEvaluator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class CdaExtraction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdaExtraction.class);

    private CdaExtraction() {
    }

    public static String extract(MessageType msgType, String filePath) {
        Document msgDoc;
        msgDoc = AbstractIT.readDoc(filePath);

        return extract(msgType, msgDoc);
    }

    public static String extract(MessageType msgType, Document message) {

        HashMap<String, String> ns = new HashMap<>();
        XPathEvaluator evaluator;
        String xpathExpr = null;

        if (msgType == MessageType.PORTAL) {
            ns.put("cc", "http://clientconnector.protocolterminator.openncp.epsos/");
            xpathExpr = "//base64Binary";
        } else if (msgType == MessageType.IHE) {
            ns.put("", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
            ns.put("ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0");
            ns.put("ns3", "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0");
            ns.put("ns4", "urn:ihe:iti:xds-b:2007");
            ns.put("ns5", "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0");
            xpathExpr = "//ns4:Document";
        }

        evaluator = new XPathEvaluator(ns);

        return evaluator.evaluate(message, xpathExpr).item(0).getFirstChild().getNodeValue();
    }

    public static DetailedResult unmarshalDetails(String xmlDetails) {

        InputStream is = new ByteArrayInputStream(xmlDetails.getBytes());

        try {
            JAXBContext jc = JAXBContext.newInstance(DetailedResult.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (DetailedResult) unmarshaller.unmarshal(is);
        } catch (JAXBException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }

    public enum MessageType {

        HL7, IHE, PORTAL
    }
}
