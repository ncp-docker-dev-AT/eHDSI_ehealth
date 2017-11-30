package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.datamodel.saml.AssertionSchematron;
import eu.epsos.validation.datamodel.xd.XdModel;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.assertion.ModelBasedValidationWS;
import net.ihe.gazelle.jaxb.assertion.ModelBasedValidationWSService;
import net.ihe.gazelle.jaxb.schematron.GazelleObjectValidator;
import net.ihe.gazelle.jaxb.schematron.GazelleObjectValidatorService;
import net.ihe.gazelle.jaxb.schematron.SOAPException_Exception;
import net.ihe.gazelle.jaxb.schematron.TransformerException_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Assertion Validation Service
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class AssertionValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionValidationService.class);
    private static AssertionValidationService INSTANCE = null;

    private AssertionValidationService() {
    }

    public synchronized static AssertionValidationService getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new AssertionValidationService();
        }
        return INSTANCE;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        String xmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        try {
            ModelBasedValidationWSService assertionService = new ModelBasedValidationWSService();
            ModelBasedValidationWS assertionPort = assertionService.getModelBasedValidationWSPort();
            xmlDetails = assertionPort.validateDocument(object, model);
        } catch (Exception ex) {
            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
        }

        if (!xmlDetails.isEmpty()) {
            LOGGER.info(xmlDetails);
            return ReportBuilder.build(model, XdModel.checkModel(model).getObjectType().toString(), object, WsUnmarshaller.unmarshal(xmlDetails), xmlDetails.toString(), ncpSide); // Report generation.
        } else {
            LOGGER.error("The webservice response is empty, writing report without validation part.");
            return ReportBuilder.build(model, XdModel.checkModel(model).getObjectType().toString(), object, null, null, ncpSide); // Report generation.
        }
    }

    @Override
    public boolean validateSchematron(String object, String schematron, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Schematron: '{}' on '{}' side]", schematron, ncpSide.getName());
        String xmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        try {
            GazelleObjectValidatorService objectValidatorService = new GazelleObjectValidatorService();
            GazelleObjectValidator gazellePort = objectValidatorService.getGazelleObjectValidatorPort();
            // Invocation of Web Service.
            xmlDetails = gazellePort.validateObject(DatatypeConverter.printBase64Binary(object.getBytes()), schematron, schematron);
        } catch (SOAPException_Exception | TransformerException_Exception ex) {
            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
        }
        LOGGER.info("epSOS Assertion validation result, using '{}' schematron", schematron);

        // Report generation.
        if (!xmlDetails.isEmpty()) {
            LOGGER.info(xmlDetails);
            return ReportBuilder.build(schematron, AssertionSchematron.checkSchematron(schematron).getObjectType().toString(),
                    object, WsUnmarshaller.unmarshal(xmlDetails), xmlDetails, ncpSide);
        } else {
            LOGGER.error("The webservice response is empty, writing report without validation part.");
            return ReportBuilder.build(schematron, AssertionSchematron.checkSchematron(schematron).getObjectType().toString(),
                    object, null, null, ncpSide);
        }
    }
}
