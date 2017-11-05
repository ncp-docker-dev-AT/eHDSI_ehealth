package eu.epsos.util.audit;

import eu.epsos.util.audit.AuditLogSerializer.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class FailedLogsHandlerImpl implements FailedLogsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedLogsHandlerImpl.class);
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
                LOGGER.info("Found file to be re-send to OpenATNAServer: '{}'", file.getAbsolutePath());
                Serializable message = serializer.readObjectFromFile(file);
                if (listener.handleMessage(message)) {
                    handleSentMessage(file);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.error("InterruptedException: '{}'", ie.getMessage(), ie);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    private void handleSentMessage(File file) {
        if (!file.delete()) {
            LOGGER.error("Unable to delete successfully re-sent log backup ('{}')!", file.getAbsolutePath());
        }
    }
}
