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

public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
    /**
     * Static constants for SMP identifiers
     */
    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";

    private final SessionFactory sessionFactory;

    private final Map<String, String> properties = new HashMap<>();

    public ConfigurationManagerImpl(SessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "sessionFactory must not be null!");
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getProperty(String key) {
        Assert.notNull(key, "key must not be null!");
        return getProperty(key, true);
    }

    public String getProperty(String key, boolean checkMap) {
        Assert.notNull(key, "key must not be null!");
        return findProperty(key, checkMap).orElseThrow(() -> new PropertyNotFoundException("Property '" + key + "' not found!"));
    }

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
     * @return
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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuring access through Authenticated Proxy '{}:{}' with Credentials: '{}/{}'",
                                proxyHost, proxyPort, proxyUsername, StringUtils.isNoneBlank(proxyPassword) ? "XXXXXX" : "No Password provided");
                    }
                    discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort, proxyUsername, proxyPassword)));

                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuring access through Proxy '{}:{}'", proxyHost, proxyPort);
                    }
                    discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort)));
                }
            }
            return discoveryBuilder;
        } catch (ConnectionException e) {
            throw new ConfigurationManagerException("An internal error occurred while trying to connect the Proxy", e);
        }
    }

    public void setServiceWSE(String coutryCode, String serviceName, String url) {
        setProperty(coutryCode + "." + serviceName + ".WSE", url);
    }

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
