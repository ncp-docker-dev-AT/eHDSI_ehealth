package eu.epsos.validation.services;

import eu.epsos.validation.datamodel.common.NcpSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the wrapper for the XDR messages validation.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class XdrValidationService extends ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XdrValidationService.class);
    private static XdrValidationService instance;

    /**
     * Private constructor to avoid instantiation.
     */
    private XdrValidationService() {
    }

    public static XdrValidationService getInstance() {

        if (instance == null) {

            instance = new XdrValidationService();
        }
        return instance;
    }

    @Override
    public boolean validateModel(String object, String model, NcpSide ncpSide) {

        LOGGER.info("[Validation Service Model: '{}' on '{}' side]", model, ncpSide.getName());
        return XcaValidationService.getInstance().validateModel(object, model, ncpSide);
    }
}
