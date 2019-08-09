package eu.europa.ec.sante.ehdsi.openncp.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class AuditServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private static AuditService instance;

    private AuditServiceFactory() {
    }

    /**
     * Returns a thread safe instance of the AuditService class required for handling event log.
     *
     * @return thread safe AuditService instance initialized.
     */
    public static synchronized AuditService getInstance() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting Instance of Audit Service...");
        }
        if (instance == null) {

            instance = new AuditService();
        }
        return instance;
    }

    /**
     * This method will stop the FailedLogsHandler process instantiated by each OpenNCP component.
     * When the AuditService has been loaded into a webapp, this method will ensure that all the threads will be properly
     * stopped before undeploying the application.
     * Expected result will be a clean status of all the threads started for handling event log serialized.
     */
    public static synchronized void stopAuditService() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stopping Failed Log Handler Service...");
        }
        getInstance().stopFailedHandler();
    }
}
