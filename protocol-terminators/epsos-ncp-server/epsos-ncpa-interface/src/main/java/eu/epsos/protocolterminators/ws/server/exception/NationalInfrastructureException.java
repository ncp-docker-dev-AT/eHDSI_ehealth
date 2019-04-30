package eu.epsos.protocolterminators.ws.server.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalInfrastructureException extends NIException {

    public NationalInfrastructureException(ErrorCode errorCode) {

        super(errorCode.getCode(), errorCode.getMessage());
        Logger logger = LoggerFactory.getLogger(NationalInfrastructureException.class);
        logger.error("NationalInfrastructureException: '{}': '{}'", errorCode.getCode(), errorCode.getMessage());
    }
}
