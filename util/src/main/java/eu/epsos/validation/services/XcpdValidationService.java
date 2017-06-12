package eu.epsos.validation.services;

import eu.epsos.util.net.ProxyUtil;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.datamodel.hl7v3.Hl7v3Model;
import eu.epsos.validation.datamodel.hl7v3.Hl7v3Schematron;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.xds.ModelBasedValidationWS;
import net.ihe.gazelle.xds.ModelBasedValidationWSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the wrapper for the XCPD messages validation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class XcpdValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcpdValidationService.class);
    private static XcpdValidationService instance;

    /**
     * Private constructor to avoid instantiation.
     */
    private XcpdValidationService() {
    }

    public static XcpdValidationService getInstance() {
        if (instance == null) {
            instance = new XcpdValidationService();
        }
        return instance;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        String hl7v3XmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        if (Hl7v3Model.checkModel(model) == null) {
            LOGGER.error("The specified model is not supported by the WebService.");
            return false;
        }

        //TODO: Fix Gazelle timeout and validation error.
        //        try {
        //        ModelBasedValidationWSService hl7Service = new net.ihe.gazelle.validator.mb.ws.ModelBasedValidationWSService();
        //        ModelBasedValidationWS hl7v3Port = hl7Service.getModelBasedValidationWSPort();
        //            hl7v3XmlDetails = hl7v3Port.validateDocument(object, model); // Invocation of Web Service client.
        //        } catch (Exception ex) {
        //            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
        //            //return false;
        //        }
        try {
            LOGGER.info("Automated validation requested to Gazelle eHDSI platform...");
            ModelBasedValidationWSService hl7Service = new ModelBasedValidationWSService();
            ModelBasedValidationWS hl7v3Port = hl7Service.getModelBasedValidationWSPort();
            // Invocation of Web Service client.
            hl7v3XmlDetails = hl7v3Port.validateDocument(object, model);
        } catch (Exception ex) {
            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
            //return false;
        }

        if (!hl7v3XmlDetails.isEmpty()) {
            return ReportBuilder.build(model, Hl7v3Model.checkModel(model).getObjectType().toString(), object, WsUnmarshaller.unmarshal(hl7v3XmlDetails), hl7v3XmlDetails.toString(), ncpSide); // Report generation.
        } else {
            LOGGER.error("The webservice response is empty.");
            return ReportBuilder.build(model, Hl7v3Model.checkModel(model).getObjectType().toString(), object, null, null, ncpSide); // Report generation.
        }
    }

    @Override
    public boolean validateSchematron(String object, String schematron, NcpSide ncpSide) {
        if (Hl7v3Schematron.checkSchematron(schematron) == null) {
            LOGGER.error("The specified schematron is not supported by the WebService.");
            return false;
        }

        return super.validateSchematron(object, schematron, ncpSide);
    }
}
