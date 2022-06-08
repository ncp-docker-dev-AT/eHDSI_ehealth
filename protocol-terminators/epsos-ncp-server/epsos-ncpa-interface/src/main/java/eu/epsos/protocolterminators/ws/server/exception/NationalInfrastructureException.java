package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalInfrastructureException extends NIException {

    public NationalInfrastructureException(EhdsiErrorCode ehdsiErrorCode) {

        super(ehdsiErrorCode, ehdsiErrorCode.getMessage());
        Logger logger = LoggerFactory.getLogger(NationalInfrastructureException.class);
        logger.error("NationalInfrastructureException: '{}': '{}'", ehdsiErrorCode.getCode(), ehdsiErrorCode.getMessage());
    }
}
