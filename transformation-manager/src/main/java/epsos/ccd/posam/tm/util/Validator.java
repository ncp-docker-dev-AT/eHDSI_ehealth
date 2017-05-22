package epsos.ccd.posam.tm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * Provides access to validation methods for CDA Document
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.8, 2010, 20 October
 */
public class Validator implements TMConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    private Validator() {
    }

    /**
     * Validation against schema
     *
     * @param document - Validated document
     * @throws Exception
     */
    public static boolean validateToSchema(Document document) {

        LOGGER.debug("method validateToSchema('{}')", document);
        if (TMConfiguration.getInstance().isSchemaValidationEnabled()) {
            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            try {
                LOGGER.info("XSD Path File: '{}'", TMConfiguration.getInstance().getSchemaFilePath());
                Source schemaFile = new StreamSource(new File(TMConfiguration.getInstance().getSchemaFilePath()));
                Schema schema = factory.newSchema(schemaFile);

                // create a Validator instance, which can be used to validate an instance document
                javax.xml.validation.Validator validator = schema.newValidator();

                // validate the DOM tree
                LOGGER.info("... Schema Validation ");
                LOGGER.debug("DOCUMENT: '{}", XMLUtil.prettyPrint(document));
                validator.validate(new DOMSource(document));
                LOGGER.info("OK , instance document is valid ");
                return true;
            } catch (SAXException e) {
                LOGGER.error("Schema validation error, input document is invalid!", e);
                return false;

            } catch (Exception e) {
                LOGGER.error("Schema validation error!", e);
                return false;
            }
        } else {
            LOGGER.info("Schema validation DISABLED");
            return false;
        }
    }

    private static boolean isScanneddoc(String docType) {

        if (docType.equals(PATIENT_SUMMARY1) ||
                docType.equals(EDISPENSATION1) ||
                docType.equals(EPRESCRIPTION1) ||
                docType.equals(HCER1) ||
                docType.equals(MRO1)) {

            return true;
        }
        return false;
    }

    /**
     * Validation using Schematron
     *
     * @param document        - Validated document
     * @param cdaDocumentType - type of CDA document (PatientSummary, ePrescription,
     *                        eDispensation)
     * @param friendly        - if true validate against friendly scheme, else against pivot
     */
    public static SchematronResult validateSchematron(Document document, String cdaDocumentType, boolean friendly) throws Exception {

        LOGGER.info("--> method SchematronResult validateSchematron('{}', '{}', '{})", document, cdaDocumentType, friendly);
        SchematronResult result;
        String schemaPath;
        SchematronValidator schValidator = SchematronValidator.getInstance();

        // fix docType for schematron validation. Schematron has special validators
        // for L1 documents, not taking actual doc type into account
        if (isScanneddoc(cdaDocumentType)) {
            if (friendly) {
                cdaDocumentType = SCANNED1;
            } else {
                cdaDocumentType = SCANNED3;
            }
        }

        if (friendly) {
            schemaPath = schValidator.getFriendlyType().get(cdaDocumentType);
        } else {
            schemaPath = schValidator.getPivotType().get(cdaDocumentType);
        }

        if (schemaPath == null) {
            // if no schematron is found return empty false result
            result = new SchematronResult();

            result.setValid(false);
            NodeList emptyList = document.createDocumentFragment().getChildNodes();
            result.setErrors(emptyList);

            return result;
        }
        result = SchematronValidator.validate(new File(schemaPath), document);
        return result;
    }

    public static ModelValidatorResult validateMDA(String document, String docType, boolean friendly) {
        return ModelBasedValidator.getInstance().validate(document, docType, friendly);
    }
}
