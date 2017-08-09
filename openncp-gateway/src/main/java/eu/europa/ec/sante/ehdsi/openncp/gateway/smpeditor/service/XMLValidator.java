package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

/**
 * @author Inês Garganta
 */

/**
 * Service responsible for validate the generated xml file against the xsd file.
 */
@Service
public class XMLValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class);

    private String reasonInvalid;

    public static boolean validateXml(String xsdPath, String xmlPath) {

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        File schemaFile = new File(xsdPath);
        Schema schema;
        try {
            schema = schemaFactory.newSchema(schemaFile);
        } catch (SAXException e) {
            LOGGER.warn("The XSD file not found in path : " + xsdPath, e);
            return false;
        }
        Validator validator = schema.newValidator();
        Source source = new StreamSource(xmlPath);
        try {
            validator.validate(source);
        } catch (Exception ex) {
            LOGGER.warn("The XML file is invalid in path : " + xmlPath, ex);
            return false;
        }
        return true;
    }

    public String getReasonInvalid() {
        return reasonInvalid;
    }

    public void setReasonInvalid(String reasonInvalid) {
        this.reasonInvalid = reasonInvalid;
    }

    public boolean validator(String XMLFileSource) {

        LOGGER.debug("\n====== XMLFileSource - '{}'", XMLFileSource);

        boolean valid = true;

        ClassLoader classLoader = getClass().getClassLoader();
        //SPECIFICATION
        File schemaFile = new File(classLoader.getResource("/src/main/resources/smpeditor/bdx-smp-201605.xsd").getFile());
        LOGGER.info("File: '{}'", schemaFile.getAbsolutePath());
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlFile = new StreamSource(new File(XMLFileSource));

        try {

            //axParser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            LOGGER.debug("\n" + xmlFile.getSystemId() + " is valid");
        } catch (SAXException ex) {
            valid = false;
            reasonInvalid = ex.getMessage();
            LOGGER.debug("\n" + xmlFile.getSystemId() + " is NOT valid reason:" + ex);
            LOGGER.error("\n SAXException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            valid = false;
            LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        return valid;
    }
}
