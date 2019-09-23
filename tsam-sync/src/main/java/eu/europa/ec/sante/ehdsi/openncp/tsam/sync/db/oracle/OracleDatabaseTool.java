package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.oracle;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.DatabaseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("oracle")
public class OracleDatabaseTool implements DatabaseTool {

    private final Logger logger = LoggerFactory.getLogger(OracleDatabaseTool.class);

    @Override
    public void backup(String filename) {
        logger.warn("Backup is not supported using Oracle database");
    }
}
