package at.gv.bmg.openncp.tomcat;

import org.apache.tomcat.util.digester.EnvironmentPropertySource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigurationPropertySource extends EnvironmentPropertySource {
    private static final String JDBC_URL = Objects.toString(System.getenv("OPENNCP_PROPERTIES_JDBC_URL"),"jdbc:mariadb://openncp-db:3306/ehealth_properties?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&useSSL=false");
    private static final String JDBC_USER = Objects.toString(System.getenv("OPENNCP_PROPERTIES_JDBC_USER"),"openncp");
    private static final String JDBC_PASSWORD = Objects.toString(System.getenv("OPENNCP_PROPERTIES_JDBC_PASSWORD"),"openncp1234");
    private static final String SELECT_PROPERTIES = "SELECT NAME,VALUE FROM EHNCP_PROPERTY";
    private Map<String, String> properties;

    @Override
    public String getProperty(String key, ClassLoader classLoader) {
        return getProperty(key);
    }

    @Override
    public String getProperty(String key) {
        if (properties == null) {
            properties = new HashMap<>();
            try(final Connection connection = getConnection();
                final Statement statement = connection.createStatement();
                final ResultSet resultSet = statement.executeQuery(SELECT_PROPERTIES);) {
                while (resultSet.next()) {
                    properties.put(resultSet.getString("NAME"), resultSet.getString("VALUE"));
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        return properties.containsKey(key) ? properties.get(key) : super.getProperty(key, getClass().getClassLoader());
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}
