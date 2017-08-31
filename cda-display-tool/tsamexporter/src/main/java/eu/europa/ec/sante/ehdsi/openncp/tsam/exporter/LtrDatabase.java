package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import epsos.ccd.gnomon.tsam.configuration.Settings;

import javax.sql.DataSource;


public class LtrDatabase {

    private static LtrDatabase ltrDatabase;

    private LtrDatabase() {
    }

    public static LtrDatabase getInstance() {
        if (ltrDatabase == null) {
            ltrDatabase = new LtrDatabase();
        }
        return ltrDatabase;
    }

    public DataSource getDataSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        Class.forName(Settings.getInstance().getSettingValue("database.class.name")).newInstance();
        String databaseUrl = Settings.getInstance().getSettingValue("database.url");
        String userName = Settings.getInstance().getSettingValue("database.username");
        String userPassword = Settings.getInstance().getSettingValue("database.password");


        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(userName);
        config.setPassword(userPassword);

        return new HikariDataSource(config);
    }
}

/**
 * String query = "SELECT * FROM code_system";
 * Connection conn = DriverManager.getConnection(databaseUrl, userName, userPassword);
 * Statement stat = conn.createStatement();
 * <p>
 * <p>
 * LOG.info("Connecting to database ...");
 * ResultSet result = stat.executeQuery(query)
 * <p>
 * try(
 * <p>
 * {
 * <p>
 * String cs_id;
 * String cs_oid;
 * String cs_name;
 * }
 **/