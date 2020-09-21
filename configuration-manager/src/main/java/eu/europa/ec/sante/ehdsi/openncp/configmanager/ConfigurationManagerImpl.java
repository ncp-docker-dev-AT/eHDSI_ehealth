package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OpenNCP Configuration Manager class responsible for the properties management.
 */
public class ConfigurationManagerImpl implements ConfigurationManager {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
    private final SessionFactory sessionFactory;
    private final Map<String, String> properties = new HashMap<>();

    /**
     * Constructor initializing the Hibernate SessionFactory of the component.
     *
     * @param sessionFactory - Hibernate Session Factory
     */
    public ConfigurationManagerImpl(SessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "Hibernate SessionFactory must not be null!");
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getProperty(String key) {
        Assert.notNull(key, "key must not be null!");
        return getProperty(key, true);
    }

    /**
     * @param key      - OpenNCP property key.
     * @param checkMap - boolean value if the cache should be checked or not.
     * @return Value of the property requested.
     */
    public String getProperty(String key, boolean checkMap) {
        Assert.notNull(key, "key must not be null!");
        return findProperty(key, checkMap).orElseThrow(() -> new PropertyNotFoundException("Property '" + key + "' not found!"));
    }

    /**
     * Returns a Map of all system properties.
     *
     * @return Map of all the system properties as a key and value pairs.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    @Override
    public int getIntegerProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    @Override
    public void setProperty(String key, String value) {

        Property property = new Property(key, value);
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(property);
        transaction.commit();

        properties.put(key, value);
    }

    /**
     * Initializes the SML/SMP Dynamic Discovery Client.
     *
     * @return Dynamic Discovery Client initialized.
     */
    public DynamicDiscoveryBuilder initializeDynamicDiscoveryFetcher() {

        //TODO: [Specification] Is it necessary to use the nonProxyHosts feature from DynamicDiscovery module.
        try {
            DynamicDiscoveryBuilder discoveryBuilder = DynamicDiscoveryBuilder.newInstance();
            boolean proxyEnabled = getBooleanProperty(StandardProperties.HTTP_PROXY_USED);

            if (proxyEnabled) {
                boolean proxyAuthenticated = getBooleanProperty(StandardProperties.HTTP_PROXY_AUTHENTICATED);
                String proxyHost = getProperty(StandardProperties.HTTP_PROXY_HOST);
                int proxyPort = getIntegerProperty(StandardProperties.HTTP_PROXY_PORT);

                if (proxyAuthenticated) {
                    String proxyUsername = getProperty(StandardProperties.HTTP_PROXY_USERNAME);
                    String proxyPassword = getProperty(StandardProperties.HTTP_PROXY_PASSWORD);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Configuring access through Authenticated Proxy '{}:{}' with Credentials: '{}/{}'",
                                proxyHost, proxyPort, proxyUsername, StringUtils.isNoneBlank(proxyPassword) ? "XXXXXX" : "No Password provided");
                    }
                    discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort, proxyUsername, proxyPassword)));

                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Configuring access through Proxy '{}:{}'", proxyHost, proxyPort);
                    }
                    discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort)));
                }
            }
            return discoveryBuilder;
        } catch (ConnectionException e) {
            throw new ConfigurationManagerException("An internal error occurred while trying to connect the Proxy", e);
        }
    }

    /**
     * Sets information related to Endpoint into OpenNCP properties database.
     *
     * @param countryCode - ISO Country code of the Service Provider.
     * @param serviceName - Interoperable service name used.
     * @param url         - URL of the endpoint.
     */
    public void setServiceWSE(String countryCode, String serviceName, String url) {
        setProperty(countryCode + "." + serviceName + ".WSE", url);
    }

    /**
     * Returns application properties including a check into the cache mechanism.
     *
     * @param key      - OpenNCP property key.
     * @param checkMap - boolean value if the cache should be checked or not.
     * @return OpenNCP property.
     */
    private Optional<String> findProperty(String key, boolean checkMap) {

        String value = null;
        if (checkMap) {
            value = properties.get(key);
        }
        if (value == null) {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Property property = session.get(Property.class, key);
            transaction.commit();

            if (property == null) {
                return Optional.empty();
            }
            value = property.getValue();
            properties.put(key, value);
        }
        return Optional.of(value);
    }
}
