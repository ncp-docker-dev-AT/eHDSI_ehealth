package eu.epsos.validation.services;

import eu.epsos.util.net.ProxyUtil;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.datamodel.xd.XdModel;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.assertion.ModelBasedValidationWS;
import net.ihe.gazelle.assertion.ModelBasedValidationWSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assertion Validation Service
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class AssertionValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionValidationService.class);

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
//        LOG.info("epSOS Assertion validation result, using '{}' schematron", schematron);

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

        throw new UnsupportedOperationException("Not supported yet.");
//        String xmlDetails = "";
//
//        if (!ValidationService.isValidationOn()) {
//            LOG.info("Automated validation turned off, not validating.");
//            return false;
//        }
//
//        try {
//            ModelBasedValidationWSService assertionService = new ModelBasedValidationWSService();
//            ModelBasedValidationWS assertionPort = assertionService.getModelBasedValidationWSPort();
//            xmlDetails = assertionPort.(object, );
//        } catch (Exception ex) {
//            LOG.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
//        }
//        LOG.info("epSOS Assertion validation result, using '{}' schematron", schematron);
//
//        if (!xmlDetails.isEmpty()) {
//            LOG.info(xmlDetails);
//            return ReportBuilder.build(schematron, XdModel.checkModel(schematron).getObjectType().toString(), object, WsUnmarshaller.unmarshal(xmlDetails), xmlDetails.toString(), ncpSide); // Report generation.
//        } else {
//            LOG.error("The webservice response is empty, writing report without validation part.");
//            return ReportBuilder.build(schematron, XdModel.checkModel(schematron).getObjectType().toString(), object, null, null, ncpSide); // Report generation.
//        }
    }
}
