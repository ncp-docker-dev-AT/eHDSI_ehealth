package epsos.ccd.carecom.tsam.synchronizer;

import epsos.ccd.carecom.tsam.synchronizer.configuration.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This is the entry class that contains the main method for the TSAM Synchronizer application.
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    /**
     * Applications main method.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // Ensuring that logging properties file location is specified.
        if (System.getProperty(ApplicationController.DEFAULT_SETTING_LOG_SETTINGS) == null) {
            // Have to write to stderr because there is no log file to write to.
            // Could create a file instead.
            LOGGER.error("The location of the logging properties file is required.");
            System.exit(1);
        }

        if (Settings.getInstance().getIsDebug()) {
            ApplicationController.unlockApplication();
        }

        if (ApplicationController.isApplicationLocked()) {
            ApplicationController.LOG.warn("Application is locked, please wait until another instance is done or delete the pid file.");
            System.exit(0); // Normal exit, because this is normal behavior.
        }

        ApplicationController.lockApplication();

        Date lastSyncDate = ApplicationController.getLastSyncDate();

        SynchronizeProcedure procedure;
        if (lastSyncDate == null) {
            procedure = new InitialSynchronization();
        } else {
            procedure = new SubsequentSynchronization(lastSyncDate, new Date());
        }
        procedure.execute();

        ApplicationController.writeSyncDate();

        ApplicationController.unlockApplication();
    }
}
