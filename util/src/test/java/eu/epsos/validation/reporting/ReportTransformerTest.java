package eu.epsos.validation.reporting;

import eu.epsos.validation.ValidationTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportTransformerTest extends ValidationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ReportTransformerTest.class);
    private String validationResult;
    private String validatedObject;

    //  @Before
    public void setUp() throws Exception {
        validationResult = getResource("validationResult.xml");
        validatedObject = getResource("validatedObject.xml");
    }

    //  @Test
    public void testGetHtmlReport() {
        ReportTransformer rt = new ReportTransformer(validationResult, validatedObject);
        String html = rt.getHtmlReport();
        logger.info(html);
    }
}
