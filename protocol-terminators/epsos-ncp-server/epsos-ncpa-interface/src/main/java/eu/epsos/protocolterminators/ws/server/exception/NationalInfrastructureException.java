package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalInfrastructureException extends NIException {

    public NationalInfrastructureException(OpenncpErrorCode openncpErrorCode) {

        super(openncpErrorCode, openncpErrorCode.getDescription());
        Logger logger = LoggerFactory.getLogger(NationalInfrastructureException.class);
        logger.error("NationalInfrastructureException: '{}': '{}'", openncpErrorCode.getCode(), openncpErrorCode.getDescription());
    }
}
