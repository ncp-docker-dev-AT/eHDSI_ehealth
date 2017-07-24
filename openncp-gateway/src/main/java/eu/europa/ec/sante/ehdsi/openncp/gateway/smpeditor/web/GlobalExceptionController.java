package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionController.class);

    /**
     * Manage Custom Exceptions
     */
    @ExceptionHandler(GenericException.class)
    public String handleCustomException(GenericException ex, Model model) {
        LOGGER.error("handleCustomException: Code:'{}' - Messages:'{}'", ex.getCode(), ex.getMessage(), ex);
        model.addAttribute("code", ex.getCode());
        model.addAttribute("message", ex.getMessage());
        return "smpeditor/error/error";
    }

    /**
     * Manage all exceptions
     */
    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception ex, Model model) {
        LOGGER.error("handleAllException: Messages:'{}'", ex.getMessage(), ex);
        model.addAttribute("message", "An error occurred. For more information check the log file.");
        return "smpeditor/error/error";
    }
}
