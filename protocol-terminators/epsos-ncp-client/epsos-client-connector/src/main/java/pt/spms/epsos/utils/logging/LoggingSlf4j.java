package pt.spms.epsos.utils.logging;

import org.slf4j.Logger;

/**
 * Provides auxiliary logging methods.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class LoggingSlf4j {

    private static final String START = "Start";
    private static final String END = "End";
    private static final String ERROR = "Error";

    private LoggingSlf4j() {
    }

    public static void start(Logger logger, String methodName) {
        logger.debug("{} | {}", methodName, START);
    }

    public static void start(Logger logger, String methodName, String msg) {
        logger.debug("{} | {} : {}", methodName, START, msg);
    }

    public static void end(Logger logger, String methodName) {
        logger.debug("{} | {}", methodName, END);
    }

    public static void end(Logger logger, String methodName, String msg) {
        logger.debug("{} | {} : {}", methodName, END, msg);
    }

    public static void error(Logger logger, String methodName) {
        logger.debug("{} | {}", methodName, ERROR);
    }

    public static void errorMsg(Logger logger, String methodName, String msg) {
        logger.debug("{} | {} : {}", methodName, ERROR, msg);
    }
}
