package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.datamodel.xd.XdModel;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.xds.ModelBasedValidationWS;
import net.ihe.gazelle.jaxb.xds.ModelBasedValidationWSService;
import net.ihe.gazelle.jaxb.xds.SOAPException_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.soap.SOAPFaultException;

/**
 * This class represents the wrapper for the XCA messages validation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class XcaValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcaValidationService.class);
    private static XcaValidationService instance;

    /**
     * Private constructor to avoid instantiation.
     */
    private XcaValidationService() {
    }

    public static XcaValidationService getInstance() {

        if (instance == null) {

            instance = new XcaValidationService();
        }
        return instance;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        String xdXmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        if (XdModel.checkModel(model) == null) {
            LOGGER.error("The specified model is not supported by the WebService.");
            return false;
        }

        if (object == null) {
            LOGGER.error("The specified object to validate is null.");
            return false;
        }

        if (object.isEmpty()) {
            LOGGER.error("The specified object to validate is empty.");
            return false;
        }

        try {
            ModelBasedValidationWSService xdService = new ModelBasedValidationWSService();
            ModelBasedValidationWS xdPort = xdService.getModelBasedValidationWSPort();
            xdXmlDetails = xdPort.validateDocument(object, model);
        } catch (SOAPFaultException e) {
            LOGGER.error("Axis Fault: '{}'", e.getMessage(), e);
        } catch (SOAPException_Exception ex) {
            LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace.", ex);
        }

        if (!xdXmlDetails.isEmpty()) {
            return ReportBuilder.build(model, XdModel.checkModel(model).getObjectType().toString(), object, WsUnmarshaller.unmarshal(xdXmlDetails), xdXmlDetails.toString(), ncpSide); // Report generation.
        } else {
            LOGGER.error("The webservice response is empty, writing report without validation part.");
            return ReportBuilder.build(model, XdModel.checkModel(model).getObjectType().toString(), object, null, null, ncpSide); // Report generation.
        }
    }
}
