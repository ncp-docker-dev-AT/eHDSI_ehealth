package eu.epsos.validation.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

@Deprecated
public class ReportTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportTransformer.class);

    private static final String XSL_FILE = "xsl//resultStylesheet.xsl";
    private String validationResult;
    private String validatedObject;

    public ReportTransformer(String validationResult, String validatedObject) {
        this.validationResult = validationResult;
        this.validatedObject = validatedObject;
    }

    public String getHtmlReport() {

        ClassLoader loader = getClass().getClassLoader();
        InputStream inputStream = loader.getResourceAsStream(XSL_FILE);
        Source xsl = new StreamSource(inputStream);

        StringReader reader = new StringReader(validationResult);
        Source in = new StreamSource(reader);

        StringWriter writer = new StringWriter();
        StreamResult out = new StreamResult(writer);

        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer(xsl);
            transformer.transform(in, out);
        } catch (TransformerException e) {
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
        }

        return writer.toString();
    }
}
