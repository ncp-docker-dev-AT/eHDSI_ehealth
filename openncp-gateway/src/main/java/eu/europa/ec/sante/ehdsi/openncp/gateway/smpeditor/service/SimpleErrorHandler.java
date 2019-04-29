package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {

    private Logger logger = LoggerFactory.getLogger(SimpleErrorHandler.class);

    /**
     * Pretty-prints an Exception into its stack trace
     *
     * @param e The Exception instance that we want to print the stack trace
     * @return The string representation of the Exception's stack trace
     */
    public static String printExceptionStackTrace(Exception e) {

        StringBuilder sb = new StringBuilder(e.toString());
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append("\n\tat ");
            sb.append(ste);
        }
        return sb.toString();
    }

    public void warning(SAXParseException e) {
        logger.error(printExceptionStackTrace(e));
    }

    public void error(SAXParseException e) {
        logger.error(printExceptionStackTrace(e));
    }

    public void fatalError(SAXParseException e) {
        logger.error(printExceptionStackTrace(e));
    }
}
