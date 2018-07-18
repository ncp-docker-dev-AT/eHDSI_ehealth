package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.audit.AuditModel;
import eu.epsos.validation.datamodel.audit.AuditSchematron;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.dts.WsUnmarshaller;
import eu.epsos.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.audit.AuditMessageValidationWS;
import net.ihe.gazelle.jaxb.audit.AuditMessageValidationWSService;
import net.ihe.gazelle.jaxb.audit.SOAPException_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.soap.SOAPFaultException;

/**
 * This class represents the wrapper for the Audit messages validation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @deprecated
 */
@Deprecated
public class AuditValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditValidationService.class);
    private static AuditValidationService instance = null;

    /**
     * Private constructor to avoid instantiation.
     */
    private AuditValidationService() {
    }

    public static synchronized AuditValidationService getInstance() {

        if (instance == null) {

            instance = new AuditValidationService();
        }
        return instance;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        boolean result;
        String amXmlDetails = "";

        if (!ValidationService.isValidationOn()) {
            LOGGER.info("Automated validation turned off, not validating.");
            return false;
        }

        if (AuditModel.checkModel(model) == null) {
            LOGGER.error("The specified model is not supported by the WebService.");
            return false;
        }

        if (ValidationService.isRemoteValidationOn()) {
            try {
                AuditMessageValidationWSService amService = new AuditMessageValidationWSService();
                AuditMessageValidationWS amPort = amService.getAuditMessageValidationWSPort();
                LOGGER.info("Requesting online validation to '{}'", amService.getWSDLDocumentLocation());
                amXmlDetails = amPort.validateDocument(object, model);
            } catch (SOAPFaultException e) {
                LOGGER.error("Axis Fault: '{}'", e.getMessage(), e);
            } catch (SOAPException_Exception ex) {
                LOGGER.error("An error has occurred during the invocation of remote validation service, please check the stack trace: '{}'", ex.getMessage(), ex);
            }
        }
        // Report generation.
        if (!amXmlDetails.isEmpty()) {
            LOGGER.info("Audit message has been successfully validated through Gazelle endpoint as result");
            result = ReportBuilder.build(model, AuditModel.checkModel(model).getObjectType().toString(), object, WsUnmarshaller.unmarshal(amXmlDetails), amXmlDetails, ncpSide);
        } else {
            LOGGER.error("The webservice response is empty.");
            result = ReportBuilder.build(model, AuditModel.checkModel(model).getObjectType().toString(), object, null, null, ncpSide);
        }
        return result;
    }

    @Override
    public boolean validateSchematron(String object, String schematron, NcpSide ncpSide) {
        if (AuditSchematron.checkSchematron(schematron) == null) {
            LOGGER.error("The specified schematron is not supported by the WebService.");
            return false;
        }

        return super.validateSchematron(object, schematron, ncpSide);
    }
}
