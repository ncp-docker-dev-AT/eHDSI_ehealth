package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
    }

    public static boolean initializeFolders(String folder) {

        try {
            Path path = Paths.get(folder);
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to create directory '{}'!!!", folder);
            return false;
        }
    }
}
