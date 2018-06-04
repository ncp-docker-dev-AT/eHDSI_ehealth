package org.openhealthtools.openatna.audit.log;

import org.openhealthtools.openatna.audit.persistence.AtnaPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistence Exception logger
 *
 * @author Andrew Harrison
 */
public class PersistenceErrorLogger {

    private static List<ErrorHandler<AtnaPersistenceException>> handlers = new ArrayList<>();

    private PersistenceErrorLogger() {
    }

    @SuppressWarnings("unused")
    public static void addErrorHandler(ErrorHandler<AtnaPersistenceException> handler) {
        handlers.add(handler);
    }

    private static void invokeHandlers(AtnaPersistenceException e) {
        for (ErrorHandler<AtnaPersistenceException> handler : handlers) {
            handler.handle(e);
        }
    }

    public static void log(AtnaPersistenceException e) {

        Logger logger = LoggerFactory.getLogger("ATNA.PERSISTENCE_ERROR_LOG");
        invokeHandlers(e);

        logger.error("===> ATNA PERSISTENCE EXCEPTION THROWN\n** PERSISTENCE ERROR: {} **", e.getError(), e);
    }
}
