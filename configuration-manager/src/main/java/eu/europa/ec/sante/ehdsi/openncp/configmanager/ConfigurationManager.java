package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import java.util.Map;

public interface ConfigurationManager {

    /**
     * @param key
     * @return
     */
    String getProperty(String key);

    String getProperty(String key, boolean checkMap);

    Map<String, String> getProperties();

    boolean getBooleanProperty(String key);

    int getIntegerProperty(String key);

    /**
     * @param key
     * @param value
     */
    void setProperty(String key, String value);

    void fetchInternationalSearchMask(String countryCode);
}
