package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import eu.epsos.util.net.ProxyCredentials;
import eu.epsos.util.net.ProxyUtil;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public final class DynamicDiscoveryService {

    private DynamicDiscoveryService() {
    }

    public static DynamicDiscovery initDynamicDiscovery() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, TechnicalException {

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH")),
                ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

        DynamicDiscoveryBuilder builder = DynamicDiscoveryBuilder.newInstance()
                .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager()
                        .getProperty(StandardProperties.SMP_SML_DNS_DOMAIN), new DefaultDNSLookup()))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)));

        if (ProxyUtil.isProxyAnthenticationMandatory()) {
            ProxyCredentials proxyCredentials = ProxyUtil.getProxyCredentials();
            if (proxyCredentials != null) {
                builder.fetcher(new DefaultURLFetcher(new CustomProxy(proxyCredentials.getProxyHost(),
                        Integer.parseInt(proxyCredentials.getProxyPort()), proxyCredentials.getProxyUser(),
                        proxyCredentials.getProxyPassword())));
            }
        }
        return builder.build();
    }

    public static CloseableHttpClient buildHttpClient(SSLContext sslContext) {

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                //  new String[]{"TLSv1"}, // Allow TLSv1 protocol only
                //   null,
                //SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                new NoopHostnameVerifier());

        ProxyCredentials proxyCredentials = null;
        if (ProxyUtil.isProxyAnthenticationMandatory()) {
            proxyCredentials = ProxyUtil.getProxyCredentials();
        }
        CloseableHttpClient httpclient;
        if (proxyCredentials != null) {

            if (proxyCredentials.getProxyUser() != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxyCredentials.getProxyHost(), Integer.parseInt(proxyCredentials.getProxyPort())),
                        new UsernamePasswordCredentials(proxyCredentials.getProxyUser(), proxyCredentials.getProxyPassword()));

                httpclient = HttpClients.custom()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLSocketFactory(sslsf)
                        .setProxy(new HttpHost(proxyCredentials.getProxyHost(), Integer.parseInt(proxyCredentials.getProxyPort())))
                        .build();
            } else {
                httpclient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .setProxy(new HttpHost(proxyCredentials.getProxyHost(), Integer.parseInt(proxyCredentials.getProxyPort())))
                        .build();
            }

        } else {
            httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
        }
        return httpclient;
    }
}
