package eu.epsos.protocolterminators.ws.server.exception;

import eu.epsos.util.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalInfrastructureException extends NIException {

    public NationalInfrastructureException(ErrorCode errorCode) {

        super(errorCode.getCodeToString(), errorCode.getMessage());
        Logger logger = LoggerFactory.getLogger(NationalInfrastructureException.class);
        logger.error("NationalInfrastructureException: '{}': '{}'", errorCode.getCode(), errorCode.getMessage());
    }
}
