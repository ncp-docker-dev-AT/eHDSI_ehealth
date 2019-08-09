package eu.epsos.util.audit;

import eu.epsos.util.audit.AuditLogSerializer.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;

public class FailedLogsHandlerImpl implements FailedLogsHandler {

    private final Logger logger = LoggerFactory.getLogger(FailedLogsHandlerImpl.class);
    private MessageHandlerListener listener;
    private AuditLogSerializer serializer;

    public FailedLogsHandlerImpl(MessageHandlerListener listener, Type type) {

        this.listener = listener;
        serializer = new AuditLogSerializerImpl(type);
    }

    public void run() {

        logger.info("[Audit Util] FailedLogsHandler thread '{}' is running", Thread.currentThread().getId());
        List<File> files = serializer.listFiles();
        for (File file : files) {

            if (logger.isDebugEnabled()) {
                logger.debug("Found file to be re-send to ATNA Server: '{}'", file.getAbsolutePath());
            }
            try {
                Serializable message = serializer.readObjectFromFile(file);
                if (listener.handleMessage(message)) {

                    handleSentMessage(file);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Exception: '{}'", e.getMessage(), e);
            }
        }
    }

    /**
     * @param file
     */
    private void handleSentMessage(File file) {

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Resent Message file has been processed, it should be deleted from filesystem:\n'{}'", file.getAbsolutePath());
            }
            Files.delete(file.toPath());
        } catch (IOException e) {
            logger.error("Unable to delete successfully re-sent log backup ('{}')!", file.getAbsolutePath());
        }
    }
}
