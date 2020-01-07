package eu.epsos.util.net;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ProxySelector;

/**
 * An utility class that allows the ability to configure proxy host settings and credentials required when a component
 * need to open an Url connection.
 * <p>
 * The following parameters must be stored into the OpenNCP database properties and available throughout the service
 * <code>ConfigurationManagerService</code>.
 * <p>
 * <i>APP_BEHIND_PROXY</i> Boolean if application is behind a Proxy or not.<br/>
 * <i>APP_PROXY_HOST</i> Proxy hostname.<br/>
 * <i>APP_PROXY_PORT</i> Proxy port number.<br/>
 * <i>APP_PROXY_USERNAME</i> Authenticated Proxy username.<br/>
 * <i>APP_PROXY_PASSWORD</i> Authenticated Proxy password.<br/>
 * <p>
 * The <code>ProxySelector</code> is automatically set when it's required by
 * using the static method initProxyConfiguration().
 *
 * @author European Commission - DG SANTE-A4-002.
 */
public class ProxyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUtil.class);

    private ProxyUtil() {
    }

    /**
     * Main method responsible of the detection and configuration of the proxy credentials.
     */
    public static void initProxyConfiguration() {

        LOGGER.info("initProxyConfiguration()");
        if (isProxyAuthenticationMandatory()) {

            ProxyCredentials proxyCredentials = getProxyCredentials();
            CustomProxySelector ps = new CustomProxySelector(ProxySelector.getDefault(), proxyCredentials);
            ProxySelector.setDefault(ps);

            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
            System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
        }
    }

    /**
     * <p>
     * Checks if a proxy server has been defined into the application system properties by querying the database.
     * </p>
     *
     * @return <code>true</code> if the property defined is true or null
     */
    public static boolean isProxyAuthenticationMandatory() {

        return Boolean.parseBoolean(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_USED));
    }

    /**
     * Method that returns the information required in order to be proxy authenticated.
     *
     * @return <code>ProxyCredentials</code> Object that encapsulates the necessary connection properties.
     */
    public static ProxyCredentials getProxyCredentials() {

        ProxyCredentials credentials = new ProxyCredentials();
        credentials.setProxyAuthenticated(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_USED));
        credentials.setProxyHost(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_HOST));
        credentials.setProxyPort(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_PORT));
        credentials.setProxyUser(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_USERNAME));
        credentials.setProxyPassword(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.HTTP_PROXY_PASSWORD));

        return credentials;
    }
}
