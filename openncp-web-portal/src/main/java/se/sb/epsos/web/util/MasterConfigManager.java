package se.sb.epsos.web.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MasterConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterConfigManager.class);
    private static final String CONFIG_PROPERTY = "epsos-web.master.cfg";
    private static final String DEFAULT_CONFIG_FILE = "config/master-config.xml";
    private static MasterConfigManager instance;

    private Configuration config = null;

    private MasterConfigManager() {

        super();
        String cfgFilePath = System.getProperty(CONFIG_PROPERTY);
        if (StringUtils.isBlank(cfgFilePath)) {
            LOGGER.warn("No explicit master config file found in system property ''{}', using default bundled config", CONFIG_PROPERTY);
            cfgFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE)).getPath();
        }
        if (!System.getProperties().containsKey("override-config.xml")) {
            System.setProperty("override-config.xml", "override-config.xml");
        }
        try {
            LOGGER.info("Loading Web Portal configuration file: '{}'", cfgFilePath);
            this.config = new DefaultConfigurationBuilder(cfgFilePath).getConfiguration();
        } catch (ConfigurationException e) {
            LOGGER.error("Failed to initialize master config: '{}'", e.getMessage());
        }
        if (config != null) {
            LOGGER.debug("Master configuration read successfully!");
        }
    }

    private static MasterConfigManager getInstance() {
        if (instance == null) {
            instance = new MasterConfigManager();
        }
        return instance;
    }

    /**
     * Flushes all configuration
     */
    public static void clear() {
        if (instance != null && instance.config != null) {
            instance.config.clear();
        }
        instance = null;
    }

    public static String get(String key) {
        if (getInstance().config == null)
            return null;
        return getInstance().config.getString(key);
    }

    public static int getInt(String key) {
        if (getInstance().config == null)
            return -1;
        return getInstance().config.getInt(key);
    }

    public static Boolean getBoolean(String key) {
        if (getInstance().config == null)
            return null;
        return getInstance().config.getBoolean(key);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        if (getInstance().config == null)
            return null;
        return getInstance().config.getBoolean(key, defaultValue);
    }

    public static boolean contains(String key) {
        if (getInstance().config == null)
            return false;
        return getInstance().config.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getList(String key) {
        if (getInstance().config == null)
            return Collections.emptyList();
        return (List<String>) (List) getInstance().config.getList(key);
    }

    public static Properties getProperties(String prefix) {
        if (getInstance().config == null)
            return null;
        Properties props = new Properties();
        Iterator<?> keys = getInstance().config.getKeys(prefix);
        while (keys.hasNext()) {
            String key = (String) keys.next();
            props.put(key, get(key));
        }
        return props;
    }

    public static Configuration getConfig() {
        return getInstance().config;
    }
}
