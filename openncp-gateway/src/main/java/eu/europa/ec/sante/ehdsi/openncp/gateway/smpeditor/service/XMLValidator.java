package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service responsible for validate the generated xml file against the xsd file.
 *
 * @author Inês Garganta
 */
public class XMLValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class);

    private XMLValidator() {
    }

    /**
     * @param xmlStream
     * @param xsdResource
     * @return
     */
    public static boolean validate(String xmlStream, String xsdResource) {

        LOGGER.debug("XML/XSD Validation");

        boolean valid = true;

        InputStream inputStream = XMLValidator.class.getResourceAsStream(xsdResource);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlFile = new StreamSource(new ByteArrayInputStream(xmlStream.getBytes(StandardCharsets.UTF_8)));

        try {

            Schema schema = schemaFactory.newSchema(new StreamSource(inputStream));
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            LOGGER.debug("'{}' is valid", xmlFile.getSystemId());
        } catch (SAXException ex) {
            valid = false;
            LOGGER.debug("'{}' is NOT valid reason: '{}'", xmlFile.getSystemId(), ex.getMessage(), ex);
            LOGGER.error("\n SAXException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            valid = false;
            LOGGER.error("IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex), ex);
        }

        return valid;
    }
}
