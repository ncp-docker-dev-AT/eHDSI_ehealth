package epsos.ccd.posam.tm.testcases;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class ValidatorTest {

    @Test
    public void testSchemaValidationPSL3() {
        final boolean result = validateXml("src/test/resources/schema/CDA_Pharma.xsd",
                "src/test/resources/samples/PS_L3_W7.xml");
        Assert.assertTrue(result);
    }

    @Test
    public void testSchemaValidationePL3_1() {
        final boolean result = validateXml("src/test/resources/schema/CDA_Pharma.xsd",
                "src/test/resources/samples/eP_L3_W6_1.xml");
        Assert.assertTrue(result);
    }

    @Test
    public void testSchemaValidationePL3_2() {
        final boolean result = validateXml("src/test/resources/schema/CDA_Pharma.xsd",
                "src/test/resources/samples/eP_L3_W6_2.xml");
        Assert.assertTrue(result);
    }

    @Test
    public void testSchemaValidationePL3_3() {
        final boolean result = validateXml("src/test/resources/schema/CDA_Pharma.xsd",
                "src/test/resources/samples/eP_L3_W6_3.xml");
        Assert.assertTrue(result);
    }

    @Test
    public void testSchemaValidationeDL3() {
        final boolean result = validateXml("src/test/resources/schema/CDA_Pharma.xsd",
                "src/test/resources/samples/eD_L3_W6.xml");
        Assert.assertTrue(result);
    }

    private boolean validateXml(String xsdPath, String xmlPath) {

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(new File(xsdPath));
            Schema schema = factory.newSchema(schemaFile);
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
            return true;
        } catch (SAXParseException e) {
            System.out.println(e.getLineNumber());
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
