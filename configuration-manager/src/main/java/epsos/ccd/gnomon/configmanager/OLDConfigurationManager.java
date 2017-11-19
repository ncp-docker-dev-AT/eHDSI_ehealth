package epsos.ccd.gnomon.configmanager;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service provides access to the system defined properties
 * <p>
 * Warning: This class is deprecated and will be removed in future versions. Please use ConfigurationManagerService instead.
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
 */
@Deprecated
public class OLDConfigurationManager implements ConfigurationManagerInt {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationManagerService.class);

    private volatile static OLDConfigurationManager instance;

    private OLDConfigurationManager() {
    }

    public static synchronized OLDConfigurationManager getInstance() {

        if (instance == null) {
            synchronized (OLDConfigurationManager.class) {
                if (instance == null) {
                    instance = new OLDConfigurationManager();
                }
            }
        }
        return instance;
    }

    private String getPropertiesPath() {

        String path = getEnvKey("EPSOS_PROPS_PATH") + "epsos.properties";
        logger.debug("EPSOS PROPERTIES PATH: {}", path);
        return path;
    }

    /**
     * Gets the value of a specific key stored in the properties file. It
     * consists of key/value pairs. First the method searches in the properties
     * file. Allowed syntax: key1=%key2%_value2. In this example the method
     * first searches for key2 in properties file and then to operating system
     * variables
     *
     * @param key the key for which we want to get its value
     * @return the string value of the key
     */
    @Override
    @Deprecated
    public String getProperty(String key) {

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(params.fileBased().setFile(new File(getPropertiesPath())));

        PropertiesConfiguration config;
        Pattern regex = Pattern.compile("%([^]]*)%");
        String specialValue;
        String returnStr = "";

        try {
            config = builder.getConfiguration();
            if (!config.isEmpty()) {
                // reads property from epsos.properties
                returnStr = config.getString(key);
                if (returnStr == null) {
                    returnStr = "";
                }
                logger.debug("GETTING PROPERTY = {} = {}", key, returnStr);
                // try to find % properties
                try {
                    Matcher m = regex.matcher(returnStr);
                    if (m.find()) {
                        specialValue = m.group();
                        String value2;
                        value2 = config.getString(specialValue.replaceAll("%", ""));
                        if (value2 != null) {
                            returnStr = returnStr.replaceAll("%([^]]*)%", value2);
                            if (returnStr == null) {
                                returnStr = "";
                            }
                            logger.debug("GETTING PROPERTY WITH WILDCARD EXPRESSION = {} = {}", key, returnStr);
                        } else {
                            logger.error("ERROR FINDING PROPERTY WITH % WILDCARD = {} = {}", key, returnStr);
                        }
                    }
                } catch (Exception e1) {
                    logger.error("ERROR FINDING PROPERTY WITH WILDCARD EXPRESSION %", e1);
                }
            } else {
                logger.error("EPSOS PROPERTIES FILE DOESN'T EXIST OR IS EMPTY");
            }

            if ("".equals(returnStr)) {
                logger.debug("TRYING TO READ PROPERTY FROM ENVIRONMENT");
                try {
                    returnStr = getEnvKey(key);
                    if (returnStr == null) {
                        returnStr = "";
                    }
                    logger.debug("GETTING PROPERTY FROM ENVIRONMENT = {} = {}", key, returnStr);
                } catch (Exception e2) {
                    logger.error("ENVIRONMENT PROPERTY {} NOT FOUND. RETURNS EMPTY STRING", key, e2);
                }
            }
        } catch (ConfigurationException e) {
            logger.error("An unexpected configuration error occurred", e);
        }
        return returnStr;
    }

    /**
     * Updates a property of the properties file
     *
     * @param key   the key we want to update its value
     * @param value the new value of the property
     * @return
     */
    @Override
    @Deprecated
    public String updateProperty(String key, String value) {

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(params.fileBased().setFile(new File(getPropertiesPath())));

        PropertiesConfiguration config;
        try {
            config = builder.getConfiguration();
            config.setProperty(key, value);
            builder.save();
            logger.debug("UPDATING PROPERTY: '{}'='{}'", key, value);
        } catch (ConfigurationException e) {
            logger.error("An unexpected configuration error occurred", e);
        } catch (Exception e) {
            logger.error("ERROR UPDATING PROPERTY " + key + " " + e.getMessage(), e);
            return "";
        }
        return value;
    }

    /**
     * Get the endpoint URL for a specified country and a service name
     *
     * @param ISOCountryCode the iso country code
     * @param ServiceName    the service name
     * @return
     */
    @Override
    @Deprecated
    public String getServiceWSE(String ISOCountryCode, String ServiceName) {

        return getProperty(ISOCountryCode + "." + ServiceName + ".WSE");
    }

    /**
     * This method updates in the properties file for a given country code and
     * service name the service endpoint
     *
     * @param ISOCountryCode the country code
     * @param ServiceName    the service name
     * @param URL            the endpoint url to be set
     */
    @Override
    @Deprecated
    public void setServiceWSE(String ISOCountryCode, String ServiceName, String URL) {

        updateProperty(ISOCountryCode + "." + ServiceName + ".WSE", URL);
    }

    /**
     * This method returns the value of an operating system variable
     *
     * @param key1
     * @return the string value of the variable
     */
    @Deprecated
    private String getEnvKey(String key1) {

        String value = "";
        Map map = System.getenv();
        Set keys = map.keySet();
        for (Object key2 : keys) {
            String key = (String) key2;
            if (key.equals(key1)) {
                value = (String) map.get(key);
                break;
            }
        }
        return value;
    }
}
