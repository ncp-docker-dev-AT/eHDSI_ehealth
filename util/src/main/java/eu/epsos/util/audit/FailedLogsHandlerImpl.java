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

        try {
            List<File> files = serializer.listFiles();
            for (File file : files) {
                logger.info("Found file to be re-send to OpenATNAServer: '{}'", file.getAbsolutePath());
                Serializable message = serializer.readObjectFromFile(file);
                if (listener.handleMessage(message)) {
                    handleSentMessage(file);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("InterruptedException: '{}'", ie.getMessage(), ie);
        } catch (Exception e) {
            logger.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param file
     */
    private void handleSentMessage(File file) {

        logger.info("Resent Message file should be deleted from filesystem:\n'{}'", file.getAbsolutePath());
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            logger.error("Unable to delete successfully re-sent log backup ('{}')!", file.getAbsolutePath());
        }
    }
}
