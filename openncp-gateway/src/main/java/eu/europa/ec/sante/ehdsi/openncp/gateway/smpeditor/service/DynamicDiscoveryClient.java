package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;


import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class DynamicDiscoveryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscoveryClient.class);

    private static DynamicDiscovery INSTANCE = null;

    private DynamicDiscoveryClient() {
    }

    public static synchronized DynamicDiscovery getInstance() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, TechnicalException {

        LOGGER.info("DynamicDiscovery getInstance()");

        if (INSTANCE == null) {
            LOGGER.debug("Initializing Dynamic Discovery...");

            LOGGER.debug("Loading Truststore...");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.NCP_TRUSTSTORE)),
                    ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.NCP_TRUSTSTORE_PASSWORD).toCharArray());

            LOGGER.debug("Loading Dynamic Discovery Builder...");
            DynamicDiscoveryBuilder dynamicDiscoveryBuilder = ConfigurationManagerFactory.getConfigurationManager().initializeDynamicDiscoveryFetcher()
                    .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager()
                            .getProperty(StandardProperties.SMP_SML_DNS_DOMAIN), new DefaultDNSLookup()))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)));
            LOGGER.debug("Building Dynamic Discovery...");
            INSTANCE = dynamicDiscoveryBuilder.build();

        }
        LOGGER.debug("Return Dynamic Discovery...");
        return INSTANCE;
    }
}
