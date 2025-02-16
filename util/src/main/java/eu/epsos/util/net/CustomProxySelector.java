package eu.epsos.util.net;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author European Commission - DG SANTE-A4-002
 */
public class CustomProxySelector extends ProxySelector {

    private final ProxySelector defaultSelector;
    private final Logger logger = LoggerFactory.getLogger(CustomProxySelector.class);
    private final HashMap<SocketAddress, InnerProxy> proxies = new HashMap<>();

    public CustomProxySelector(ProxySelector def, final ProxyCredentials credentials) {

        this.defaultSelector = def;
        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(credentials.getProxyUser(),
                        credentials.getProxyPassword().toCharArray());
            }
        });

        InnerProxy proxy = new InnerProxy(new InetSocketAddress(credentials.getProxyHost(),
                Integer.parseInt(credentials.getProxyPort())));
        proxies.put(proxy.address(), proxy);
    }

    @Override
    public List<Proxy> select(URI uri) {

        if (logger.isDebugEnabled()) {
            logger.debug("select for URL : '{}'", uri);
        }

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null.");
        }

        // Get protocol and management configuration for HTTP or HTTPS.
        String protocol = uri.getScheme();
        if (StringUtils.equalsIgnoreCase("http", protocol)
                || StringUtils.equalsIgnoreCase("https", protocol)) {

            List<Proxy> proxyList = new ArrayList<>();
            for (InnerProxy p : proxies.values()) {

                proxyList.add(p.toProxy());
            }
            // Return configured Proxy
            return proxyList;
        }

        /*
         * For others protocols (could be SOCKS or FTP etc.) return the default selector.
         */
        if (defaultSelector != null) {
            return defaultSelector.select(uri);
        } else {
            List<Proxy> proxyList = new ArrayList<>();
            proxyList.add(Proxy.NO_PROXY);
            return proxyList;
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        InnerProxy p = proxies.get(sa);
        if (p != null) {

            if (p.failed() >= 3) {
                proxies.remove(sa);
            }
        } else {

            if (defaultSelector != null) {
                defaultSelector.connectFailed(uri, sa, ioe);
            }
        }
    }

    /*
     * Inner class representing a Proxy.
     */
    static class InnerProxy {

        Proxy proxy;
        SocketAddress socketAddress;
        int failedCount = 0;

        InnerProxy(InetSocketAddress inetSocketAddress) {
            socketAddress = inetSocketAddress;
            proxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
        }

        SocketAddress address() {
            return socketAddress;
        }

        Proxy toProxy() {
            return proxy;
        }

        int failed() {
            return ++failedCount;
        }
    }
}
