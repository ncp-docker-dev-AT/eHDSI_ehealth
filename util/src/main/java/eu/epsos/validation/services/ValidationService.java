package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.datamodel.hl7v3.Hl7v3Schematron;
import eu.epsos.validation.reporting.ReportBuilder;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import net.ihe.gazelle.schematron.GazelleObjectValidator;
import net.ihe.gazelle.schematron.GazelleObjectValidatorService;
import net.ihe.gazelle.schematron.SOAPException_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public abstract class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);
    private static final String VALIDATION_STATUS_PROPERTY_NAME = "automated.validation";

    /**
     * This method will check is the automated validation is turned on, by the
     * setting of a specific property in the properties database.
     *
     * @return the boolean value, stating if the automated validation is on
     */
    protected static boolean isValidationOn() {

        String validationOnVal = ConfigurationManagerFactory.getConfigurationManager().getProperty(VALIDATION_STATUS_PROPERTY_NAME);

        if (validationOnVal == null) {
            LOGGER.error("The value of Validation Property in properties database is null.");
            return false;
        }
        if (validationOnVal.isEmpty()) {
            LOGGER.error("The value of Validation Property in properties database is empty.");
            return false;
        }

        return Boolean.parseBoolean(validationOnVal);
    }

    /**
     * This abstract method defines the operation that will trigger the model
     * based validation of a specific object (e.g. a document or transaction
     * message), using a specific model.
     *
     * @param object  the object to validate (e.g. a document or transaction
     *                message)
     * @param model   the specific model to be used
     * @param ncpSide
     * @return the result of validation execution: false if errors occur.
     */
    public abstract boolean validateModel(String object, String model, NcpSide ncpSide);

    /**
     * This method executes the operation that will trigger the schematron based
     * validation of a specific object (e.g. a document or transaction message),
     * using a specific schematron. This operation is shared by many object
     * types and they all share the same endpoint.
     *
     * @param object     the object to validate (e.g. a document or transaction
     *                   message)
     * @param schematron the specific schematron to be used
     * @param ncpSide    the specific NCP side, either NCP-A or NCP-B.
     * @return the result of validation execution: false if errors occur.
     */
    protected boolean validateSchematron(String object, String schematron, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Schematron: '{}' on '{}' side]", schematron, ncpSide.getName());
        String xmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        try {
            GazelleObjectValidatorService objectValidatorService = new GazelleObjectValidatorService();
            GazelleObjectValidator gazellePort = objectValidatorService.getGazelleObjectValidatorPort();
            xmlDetails = gazellePort.validateObject(DatatypeConverter.printBase64Binary(object.getBytes()), schematron, schematron);
        } catch (SOAPException_Exception ex) {
            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
            return false;
        }

        if (!xmlDetails.isEmpty()) {
            return ReportBuilder.build(schematron, Hl7v3Schematron.checkSchematron(schematron).getObjectType().toString(), object, WsUnmarshaller.unmarshal(xmlDetails), xmlDetails, ncpSide); // Report generation.
        } else {
            LOGGER.error("The webservice response is empty.");
            return ReportBuilder.build(schematron, Hl7v3Schematron.checkSchematron(schematron).getObjectType().toString(), object, null, null, ncpSide); // Report generation
        }
    }
}
