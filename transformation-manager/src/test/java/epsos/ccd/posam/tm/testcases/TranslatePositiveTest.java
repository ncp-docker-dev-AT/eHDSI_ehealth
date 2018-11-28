package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.response.TMResponseStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Positive test scenarios for method translate
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.7, 2010, 20 October
 */
public class TranslatePositiveTest extends TBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslatePositiveTest.class);

    public void testTranslatePatientSummaryL3() {
        // valid document - PatientSummary
        Document document = getDocument(new File(samplesDir + "PS_Katzlmacher.xml"));

        assertNotNull(document);

        TMResponseStructure response = tmService.translate(document, "sk-SK");

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(response.getDocument()), new StreamResult(new FileOutputStream("tmresult.xml")));
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }
}
