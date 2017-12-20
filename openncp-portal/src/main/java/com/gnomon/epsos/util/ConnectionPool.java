package com.gnomon.epsos.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;

public class ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    private static ConnectionPool _instance = new ConnectionPool();
    private ComboPooledDataSource _cpds;
    private Properties _props;

    private ConnectionPool() {

        try {
            LOGGER.info("ConnectionPool Initialization...");

            // Properties
            ClassLoader classLoader = getClass().getClassLoader();
            _props = new Properties();
            _props.load(classLoader.getResourceAsStream("connection-pool.properties"));

            Set<String> names = _props.stringPropertyNames();
            for (String name : names) {

                LOGGER.info("{}: '{}'", name, _props.getProperty(name));
            }


            // Pooled data source
            String driverClass = _props.getProperty("driver.class");
            String jdbcUrl = _props.getProperty("jdbc.url");
            String user = _props.getProperty("user");
            String password = _props.getProperty("password");

            int minPoolSize = 5;

            try {
                minPoolSize = Integer.parseInt(_props.getProperty("min.pool.size"));
            } catch (Exception e) {
                LOGGER.error("ConnectionPool Exception: '{}'", e.getMessage(), e);
            }

            int maxPoolSize = 5;

            try {
                maxPoolSize = Integer.parseInt(_props.getProperty("max.pool.size"));
            } catch (Exception e) {
                LOGGER.error("ConnectionPool Exception: '{}'", e.getMessage(), e);
            }

            int acquireIncrement = 5;

            try {
                acquireIncrement = Integer.parseInt(_props.getProperty("acquire.increment"));
            } catch (Exception e) {
                LOGGER.error("ConnectionPool Exception: '{}'", e.getMessage(), e);
            }

            _cpds = new ComboPooledDataSource();
            _cpds.setDriverClass(driverClass);
            _cpds.setJdbcUrl(jdbcUrl);
            _cpds.setUser(user);
            _cpds.setPassword(password);
            _cpds.setMinPoolSize(minPoolSize);
            _cpds.setMaxPoolSize(maxPoolSize);
            _cpds.setAcquireIncrement(acquireIncrement);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            LOGGER.error("Exception: " + e);
        }
    }

    public static void cleanUp(Connection con) {
        _instance._cleanUp(con);
    }

    public static void cleanUp(Connection con, Statement s) {
        _instance._cleanUp(con, s);
    }

    public static void cleanUp(Connection con, Statement s, ResultSet rs) {
        _instance._cleanUp(con, s, rs);
    }

    public static void destroy() throws SQLException {
        _instance._destroy();
    }

    public static Connection getConnection() throws SQLException {
        return _instance._getConnection();
    }

    public static Properties getProperties() {
        return _instance._props;
    }

    private void _cleanUp(Connection con) {
        _cleanUp(con, null, null);
    }

    private void _cleanUp(Connection con, Statement s) {
        _cleanUp(con, s, null);
    }

    private void _cleanUp(Connection con, Statement s, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException sqle) {
            LOGGER.error("SQLException: '{}'-'{}'", sqle.getErrorCode(), sqle.getSQLState(), sqle);
        }

        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException sqle) {
            LOGGER.error("SQLException: '{}'-'{}'", sqle.getErrorCode(), sqle.getSQLState(), sqle);
        }

        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException sqle) {
            LOGGER.error("SQLException: '{}'-'{}'", sqle.getErrorCode(), sqle.getSQLState(), sqle);
        }
    }

    private void _destroy() throws SQLException {
        DataSources.destroy(_cpds);
    }

    private Connection _getConnection() throws SQLException {
        return _cpds.getConnection();
    }
}
