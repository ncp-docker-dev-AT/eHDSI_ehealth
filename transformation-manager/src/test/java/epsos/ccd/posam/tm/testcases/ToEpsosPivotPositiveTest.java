package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.response.TMResponseStructure;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Positive test scenarios for method toEpsosPivot
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.7, 2010, 20 October
 */
@Ignore("Test to revise - Exclude unit test from test execution")
public class ToEpsosPivotPositiveTest extends TBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToEpsosPivotPositiveTest.class);

    public void testToEpSOSPivotPatientSummaryL3() {

        Document validDocument = getDocument();
        assertNotNull(validDocument);

        TMResponseStructure response = null;
//      TMResponseStructure response = tmService.toEpSOSPivot(doc);
        try {
            LOGGER.info("XML Response: '{}'", response.getDocument());
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }

        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    public void testToEpSOSPivotPatientSummaryL1() {

        Document doc = getDocument(new File(samplesDir + "unstructuredCDA.xml"));
        assertNotNull(doc);

        TMResponseStructure response = null;
//      TMResponseStructure response = tmService.toEpSOSPivot(doc);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(response.getResponseCDA()), new StreamResult(outputStream));
            LOGGER.info("Response:\n{}", outputStream.toString());
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    /**
     * otestuje ci coded element ma rovnaky NS ako jeho parrent
     */
    public void testToEpSOSPivotPatientSummaryL1NS() {

        Document doc = getDocument(new File(samplesDir + "PS_Katzlmacher.xml"));
        assertNotNull(doc);

        TMResponseStructure response = null;
//      TMResponseStructure response = tmService.toEpSOSPivot(doc);
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(response.getDocument()), new StreamResult(new FileOutputStream("tmresult.xml")));
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }
}
