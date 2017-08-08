package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public interface ConfigurationManager {

    /**
     * @param key
     * @return
     */
    String getProperty(String key);

    /**
     * @param key
     * @param value
     */
    void setProperty(String key, String value);

    /**
     * @param countryCode
     * @param service
     * @return
     */
    String getEndpointUrl(String countryCode, RegisteredService service);

    /**
     * @param ISOCountryCode
     * @param ServiceName
     * @param URL
     */
    void setServiceWSE(String ISOCountryCode, String ServiceName, String URL);
}
