package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.cda.CdaModel;
import eu.epsos.validation.datamodel.cda.CdaSchematron;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.cda.ModelBasedValidationWS;
import net.ihe.gazelle.jaxb.cda.ModelBasedValidationWSService;
import net.ihe.gazelle.jaxb.cda.SOAPException_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.soap.SOAPFaultException;

/**
 * This class represents the wrapper for the CDA documents validation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@Deprecated
public class CdaValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcaValidationService.class);
    private static CdaValidationService instance;

    /**
     * Private constructor to avoid instantiation.
     */
    private CdaValidationService() {
    }

    public static CdaValidationService getInstance() {
        if (instance == null) {

            instance = new CdaValidationService();
        }
        return instance;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        String cdaXmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        if (CdaModel.checkModel(model) == null) {
            LOGGER.error("The specified model is not supported by the WebService.");
            return false;
        }
        if (ValidationService.isRemoteValidationOn()) {
            try {
                LOGGER.info("Automated validation for CDA document...");
                ModelBasedValidationWSService cdaService = new ModelBasedValidationWSService();
                ModelBasedValidationWS cdaPort = cdaService.getModelBasedValidationWSPort();
                // Invocation of Web Service client.
                cdaXmlDetails = cdaPort.validateDocument(object, model);
            } catch (SOAPFaultException e) {
                LOGGER.error("Axis Fault: '{}'", e.getMessage(), e);
            } catch (SOAPException_Exception ex) {
                LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stacktrace.", ex);
            }
        }
        if (!cdaXmlDetails.isEmpty()) {
            return ReportBuilder.build(model, CdaModel.checkModel(model).getObjectType().toString(), object, WsUnmarshaller.unmarshal(cdaXmlDetails), cdaXmlDetails.toString(), ncpSide); // Report generation.
        } else {
            LOGGER.error("The webservice response is empty.");
            return ReportBuilder.build(model, CdaModel.checkModel(model).getObjectType().toString(), object, null, null, ncpSide); // Report generation.
        }

    }

    @Override
    public boolean validateSchematron(String object, String schematron, NcpSide ncpSide) {

        if (CdaSchematron.checkSchematron(schematron) == null) {
            LOGGER.error("The specified schematron is not supported by the WebService.");
            return false;
        }

        return super.validateSchematron(object, schematron, ncpSide);
    }
}
