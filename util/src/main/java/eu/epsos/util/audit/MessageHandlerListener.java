package eu.epsos.util.audit;

import java.io.Serializable;

/**
 * Message Handler contract handling the FailedLogHandler mechanism.
 */
public interface MessageHandlerListener {

    /**
     * Sending backup Audit Message as Syslog Serializable object to ATNA Server.
     *
     * @param message - Backup message serialized on the file system.
     * @return true if message has been submitted with success | false otherwise.
     */
    boolean handleMessage(Serializable message);
}
