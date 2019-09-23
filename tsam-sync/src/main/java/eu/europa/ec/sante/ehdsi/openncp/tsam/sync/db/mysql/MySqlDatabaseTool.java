package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.mysql;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.config.DataSourceProperties;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.DatabaseTool;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.DatabaseToolException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile({"mysql", "mariadb"})
public class MySqlDatabaseTool implements DatabaseTool {

    private final DataSourceProperties dataSourceProperties;

    public MySqlDatabaseTool(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public void backup(String filename) {
        String command = "mysqldump" +
                " --host=" + dataSourceProperties.getHost() +
                " --port=" + dataSourceProperties.getPort() +
                " --user=" + dataSourceProperties.getUsername() +
                " --password=" + dataSourceProperties.getPassword() +
                " --result-file=" + filename +
                " --databases " + dataSourceProperties.getDatabase();

        try {
            Process process = Runtime.getRuntime().exec(command);
            if (process.waitFor() != 0) {
                throw new DatabaseToolException("Database backup operation failed");
            }
        } catch (IOException e) {
            throw new DatabaseToolException("An I/O exception occurred while starting database backup process", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseToolException("An error occurred while running database backup process", e);
        }
    }
}
