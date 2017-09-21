package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Inês Garganta
 */

@Controller
public class SMPEditorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPEditorController.class);

    /**
     * Generates smpeditor page
     *
     * @return
     */
    @RequestMapping("/smpeditor/smpeditor")
    public String SMPEditor() {

        LOGGER.info("SMP Editor Initialization");

        File smp_dir = new File(Constants.SMP_DIR_PATH);
        if (!smp_dir.exists()) {

            boolean directoryCreated = smp_dir.mkdir();
            Set<PosixFilePermission> perms = new HashSet<>();
            //add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            //add group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            //add others permissions
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            try {
                Files.setPosixFilePermissions(Paths.get(Constants.SMP_DIR_PATH), perms);
            } catch (IOException e) {
                LOGGER.error("IOException: '{}'", e.getMessage(), e);
            }
            LOGGER.info("SMP directory created: '{}'", directoryCreated);
        } else {
            LOGGER.info("SMP directory already exist");
        }
        return "smpeditor/smpeditor";
    }
}
