package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.service.impl.TransformationService;
import epsos.ccd.posam.tm.util.SchematronResult;
import epsos.ccd.posam.tm.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;

/**
 * Test scenarios for schematron validation
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.5, 2010, 20 October
 */
public class SchematronTest extends TBase {

    static final String resources = "src/test/resources/samples/schPassed/";
    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronTest.class);

    /**
     * Tests all reference documents against schematron. Document type is
     * determined based on it's content (document code and body type).
     * Friendly or pivot type is determined based on file name convention,
     * if name contains 'friendly' it's friendly.
     */
    public void testSchematronAll() {
        TransformationService tm = (TransformationService) tmService;
        try {
            File[] docs = new File(resources).listFiles();
            Document xmlDoc;
            boolean friendly;
            boolean allOK = true;
            StringBuilder sb = new StringBuilder();
            for (File doc : docs) {

                friendly = doc.getName().contains("friendly");

                xmlDoc = getDocument(doc);
                SchematronResult result = Validator.validateSchematron(xmlDoc, tm.getCDADocumentType(xmlDoc), friendly);
                if (!result.isValid()) {
                    LOGGER.info("Schematron validation result correct: '{}'", result);
                    allOK = false;

                }
                sb.append(doc.getName()).append(": ").append(result.getErrors().getLength()).append("\n");
            }
            LOGGER.info("Result: '{}'", sb);
            assertTrue(allOK);


        } catch (Exception e) {
            fail();
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
    }
}
