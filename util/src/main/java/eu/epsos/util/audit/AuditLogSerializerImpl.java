package eu.epsos.util.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSerializerImpl implements AuditLogSerializer {

    private final Logger logger = LoggerFactory.getLogger(AuditLogSerializerImpl.class);
    private Type type;

    public AuditLogSerializerImpl(Type type) {
        this.type = type;
    }

    public List<File> listFiles() {

        List<File> files = new ArrayList<>();
        File path = getPath();
        if (isPathValid(path)) {
            File[] srcFiles = path.listFiles();
            if (srcFiles == null) {
                return new ArrayList<>();
            }
            for (File file : srcFiles) {
                if (isAuditLogBackupWriterFile(file)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public Serializable readObjectFromFile(File inFile) throws IOException, ClassNotFoundException {

        try (InputStream file = new FileInputStream(inFile)) {

            ObjectInput input = new ObjectInputStream(file);
            Serializable serializable = (Serializable) input.readObject();
            input.close();
            return serializable;
        }
    }

    /**
     * @param message
     */
    public void writeObjectToFile(Serializable message) {

        String path = System.getenv("EPSOS_PROPS_PATH") + type.getNewFileName();
        try (OutputStream file = new FileOutputStream(path)) {

            if (message != null) {

                ObjectOutput outputStream = new ObjectOutputStream(file);
                outputStream.writeObject(message);
                outputStream.flush();
                outputStream.close();
                logger.error("Error occurred while writing AuditLog to OpenATNA! AuditLog saved to: '{}'", path);
            }
        } catch (Exception e) {
            logger.error("Unable to send AuditLog to OpenATNA nor able write auditLog backup! Dumping to log: '{}'", message.toString(), e);
        }
    }

    private File getPath() {

        return new File(System.getenv("EPSOS_PROPS_PATH") + type.getDir());
    }

    private boolean isAuditLogBackupWriterFile(File file) {

        String fileName = file.getName();
        return fileName.startsWith(type.getFilePrefix()) && fileName.endsWith(type.getFileSuffix());
    }

    private boolean isPathValid(File path) {

        if (!path.exists()) {
            logger.error("Source path ('{}') does not exist!", path);
            return false;
        } else if (!path.isDirectory()) {
            logger.error("Source path ('{}') is not a diredtory!", path);
            return false;
        }

        return true;
    }
}
