package eu.epsos.util.audit;

import eu.epsos.util.audit.AuditLogSerializer.Type;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FailedLogsHandlerServiceImpl implements FailedLogsHandlerService {

    public static final String KEY_SCHEDULED_TIME_BETWEEN_FAILED_LOGS_HANDLING = "scheduled.time.between.failed.logs.handling.minutes";
    public static final int SCHEDULED_THREAD_POOL_SIZE = 1;
    public static final int WAIT_FOR_TERMINATION = 5000;
    // Default time scheduler is fix to 1 hour
    private static final long DEFAULT_SCHEDULER_TIME_MINUTES = 60;
    private static final Logger LOGGER = LoggerFactory.getLogger(FailedLogsHandlerServiceImpl.class);
    private ScheduledExecutorService service = null;
    private MessageHandlerListener listener;
    private Type type;

    public FailedLogsHandlerServiceImpl(MessageHandlerListener listener, Type type) {
        this.listener = listener;
        this.type = type;
    }

    public synchronized void start() {

        if (service == null) {
            FailedLogsHandler failedLogsHandlerCommand = new FailedLogsHandlerImpl(listener, type);
            service = new ScheduledThreadPoolExecutor(SCHEDULED_THREAD_POOL_SIZE);
            service.scheduleWithFixedDelay(failedLogsHandlerCommand, getTimeBetween(), getTimeBetween(), TimeUnit.MINUTES);
            LOGGER.info("Started FailedLogsHandlerService. Logs will be scanned every '{}' minutes.", getTimeBetween());
        } else {
            LOGGER.warn("Attempted to start FailedLogsHandlerService even already running.");
        }
    }

    public synchronized void stop() {

        LOGGER.info("Shutting down FailedLogsHandlerService");
        if (service != null) {
            service.shutdown();

            boolean shutdownOk = false;
            try {
                shutdownOk = service.awaitTermination(WAIT_FOR_TERMINATION, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("InterruptedException: '{}'", e.getMessage(), e);
            }
            if (!shutdownOk) {
                service.shutdownNow();
            }

            service = null;
        } else {
            LOGGER.warn("Unable to stop FailedLogsHandlerService. Service is not running.");
        }
    }

    /**
     * Method that return the interval between Failed log retry in minute.
     *
     * @return Interval in minute
     */
    private long getTimeBetween() {

        String sValue = ConfigurationManagerFactory.getConfigurationManager().getProperty(KEY_SCHEDULED_TIME_BETWEEN_FAILED_LOGS_HANDLING);
        if (StringUtils.isBlank(sValue)) {
            return DEFAULT_SCHEDULER_TIME_MINUTES;
        }

        long l;
        try {
            l = Long.parseLong(sValue);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            return DEFAULT_SCHEDULER_TIME_MINUTES;
        }
        return l;
    }
}
