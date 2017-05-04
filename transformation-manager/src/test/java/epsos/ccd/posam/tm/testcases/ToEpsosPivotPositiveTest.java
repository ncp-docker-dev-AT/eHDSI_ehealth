package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.response.TMResponseStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
public class ToEpsosPivotPositiveTest extends TBase {

    private static final Logger logger = LoggerFactory.getLogger(ToEpsosPivotPositiveTest.class);

    public void testToEpSOSPivotPatientSummaryL3() {
        Document validDocument = getDocument();
        //Document validDocument = getDocument(new File(samplesDir + "HuberMelanie_PS.xml"));
        assertNotNull(validDocument);

        TMResponseStructure response = tmService.toEpSOSPivot(validDocument);
        try {
            logger.info("XML Response: '{}'", response.getDocument());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    public void testToEpSOSPivotPatientSummaryL1() {
        Document doc = getDocument(new File(samplesDir + "unstructuredCDA.xml"));
        assertNotNull(doc);

        TMResponseStructure response = tmService.toEpSOSPivot(doc);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(response.getResponseCDA()), new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
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

        TMResponseStructure response = tmService.toEpSOSPivot(doc);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(response.getDocument()), new StreamResult(new FileOutputStream("tmresult.xml")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(response);
        assertTrue(response.isStatusSuccess());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }
}
