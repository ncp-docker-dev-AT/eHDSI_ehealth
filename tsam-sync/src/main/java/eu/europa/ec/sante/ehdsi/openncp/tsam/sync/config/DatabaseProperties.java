package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tsam-sync.database")
public class DatabaseProperties {

    private boolean backup;

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }
}
