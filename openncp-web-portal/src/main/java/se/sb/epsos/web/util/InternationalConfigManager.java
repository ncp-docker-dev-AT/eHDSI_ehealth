package se.sb.epsos.web.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.model.CountryVO;

import java.io.File;
import java.util.*;

public class InternationalConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalConfigManager.class);
    private static Map<String, XMLConfiguration> config = new HashMap<>();

    public InternationalConfigManager(List<CountryVO> countryCodes) {

        super();
        String path = System.getProperty("epsos-internationalsearch-config-path");
        LOGGER.debug("epsos-internationalsearch-config-path: '{}'", path);
        if (path == null) {
            final String err = "epsos-internationalsearch-config-path is not set";
            LOGGER.error(err);
            throw new IllegalArgumentException(err);
        }
        for (CountryVO countryCode : countryCodes) {
            File iSearchFile = new File(path + "/InternationalSearch_" + countryCode.getId() + ".xml");
            LOGGER.info("Path: '{}'", iSearchFile.getPath());
            if (iSearchFile.exists() && iSearchFile.isFile()) {
                try {
                    config.put(countryCode.getId(), new XMLConfiguration(iSearchFile));
                } catch (ConfigurationException e) {
                    LOGGER.error("ConfigurationException", e);
                }
            }
        }
    }

    public static List<Properties> getProperties(String country, String prefix) {

        XMLConfiguration xmlConfig = config.get(country);
        List<Properties> propList = new ArrayList<>();
        if (xmlConfig != null) {
            Iterator<?> keys = xmlConfig.getKeys(prefix);
            while (keys.hasNext()) {
                String key = (String) keys.next();
                List<String> list = getList(country, key);
                int i = 0;
                for (String str : list) {
                    Properties props = new Properties();
                    props.put(key + i, str);
                    propList.add(props);
                    i++;
                }
            }
        }
        return propList;
    }

    public static String get(String country, String key) {

        XMLConfiguration xmlConfig = config.get(country);
        return xmlConfig.getString(key);
    }

    public static List<String> getList(String country, String key) {

        XMLConfiguration xmlConfig = config.get(country);
        if (xmlConfig == null) {
            return Collections.emptyList();
        }
        return (List<String>) (List) xmlConfig.getList(key);
    }
}
