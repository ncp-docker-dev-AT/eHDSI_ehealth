package eu.epsos.util.audit;

import eu.epsos.util.audit.AuditLogSerializer.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class FailedLogsHandlerImpl implements FailedLogsHandler {

    private static Logger log = LoggerFactory.getLogger("org.openhealthtools.openatna.audit.service.FailedLogsHandlerImpl");
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
                log.info("Found file to be re-send to OpenATNAServer: '{}'", file.getAbsolutePath());
                Serializable message = serializer.readObjectFromFile(file);
                if (listener.handleMessage(message)) {
                    handleSentMessage(file);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // Ignored
                    log.error("InterruptedException: '{}'", ie.getMessage(), ie);
                }
            }
        } catch (Exception e) {
            log.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    private void handleSentMessage(File file) {
        if (!file.delete()) {
            log.error("Unable to delete successfully re-sent log backup ('{}')!", file.getAbsolutePath());
        }
    }
}
