package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.GatewayProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class DynamicDiscoveryClient {

    private final Logger logger = LoggerFactory.getLogger(DynamicDiscoveryClient.class);
    private final PropertyService propertyService;
    private DynamicDiscovery instance = null;

    private DynamicDiscoveryClient(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public synchronized DynamicDiscovery getInstance() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, TechnicalException {

        logger.info("[Gateway] DynamicDiscovery getInstance()");
        if (instance == null) {
            logger.debug("Instantiating new instance of DynamicDiscovery");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty(GatewayProperties.GTW_TRUSTSTORE_PATH)),
                    ConfigurationManagerFactory.getConfigurationManager().getProperty(GatewayProperties.GTW_TRUSTSTORE_PWD).toCharArray());

            trustStore.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty(GatewayProperties.GTW_TRUSTSTORE_PATH)),
                    ConfigurationManagerFactory.getConfigurationManager().getProperty(GatewayProperties.GTW_TRUSTSTORE_PWD).toCharArray());

            DynamicDiscoveryBuilder dynamicDiscoveryBuilder = ConfigurationManagerFactory.getConfigurationManager().initializeDynamicDiscoveryFetcher()
                    .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager()
                            .getProperty(StandardProperties.SMP_SML_DNS_DOMAIN), new DefaultDNSLookup()))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(trustStore)));
            instance = dynamicDiscoveryBuilder.build();
        }
        return instance;
    }
}
