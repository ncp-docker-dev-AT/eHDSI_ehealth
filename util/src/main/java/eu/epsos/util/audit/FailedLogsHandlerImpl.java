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
            boolean isFileHandledOk = true;
            try {
                Serializable message = serializer.readObjectFromFile(file);
                isFileHandledOk = listener.handleMessage(message);
                if (isFileHandledOk) {
                	isFileHandledOk = handleSentMessage(file);
                }
            } catch (IOException | ClassNotFoundException e) {
            	logger.error("Exception: '{}', {}", e.getMessage(), e);
            	 isFileHandledOk = false;
            } catch(Exception extra) {
            	logger.error("Exception not IO or ClassNotFound: '{}', {}", extra.getMessage(), extra);
            	isFileHandledOk = false; // prepare for the big bad wolf
            }
            if(!isFileHandledOk) {
            	serializer.moveFile(file);
            }
        }
    }

    /**
     * @param file
     */
    private boolean handleSentMessage(File file) {
    	boolean isFileHandledOk = false;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Resent Message file has been processed, it should be deleted from filesystem:\n'{}'", file.getAbsolutePath());
            }
            Files.delete(file.toPath());
            isFileHandledOk = true;
        } catch (IOException e) {
            logger.error("Unable to delete successfully re-sent log backup ('{}')!", file.getAbsolutePath());
        }
        return isFileHandledOk;
    }
}
