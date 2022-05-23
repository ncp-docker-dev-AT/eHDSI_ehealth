package eu.europa.ec.sante.ehdsi.openncp.gateway.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@ControllerAdvice
public class DefaultExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public void handleApiException(HttpServletResponse response, ApiException ex) throws IOException {
        logger.error(ex.getMessage(), ex);
        response.sendError(ex.getRawStatusCode());
    }
}

