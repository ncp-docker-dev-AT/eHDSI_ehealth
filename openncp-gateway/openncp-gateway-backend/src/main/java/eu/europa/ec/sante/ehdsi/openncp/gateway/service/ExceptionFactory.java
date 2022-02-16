package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import eu.europa.ec.sante.ehdsi.openncp.gateway.GatewayBackendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class ExceptionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionFactory.class);

    private ExceptionFactory() {
    }

    public static GatewayBackendException create(final Throwable cause, final ExceptionType exceptionType,
                                                 final Object... messageArguments) {
        LOGGER.error(MessageFormat.format(exceptionType.getMessage(), messageArguments), cause);
        return new GatewayBackendException(exceptionType, cause, messageArguments);
    }

    public static GatewayBackendException create(final ExceptionType exceptionType, final Object... messageArguments) {
        LOGGER.error(MessageFormat.format(exceptionType.getMessage(), messageArguments));
        return new GatewayBackendException(exceptionType, messageArguments);
    }
}
