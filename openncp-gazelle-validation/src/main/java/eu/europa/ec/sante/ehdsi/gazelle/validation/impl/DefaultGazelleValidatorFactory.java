package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.*;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;


public class DefaultGazelleValidatorFactory implements IGazelleValidatorFactory {

    public static final String GAZELLE_ASSERTION_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_ASSERTION_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/gazelle-xua-jar/ModelBasedValidationWSService/ModelBasedValidationWS";

    public static final String GAZELLE_AUDIT_MESSAGE_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_AUDIT_MESSAGE_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/gazelle-atna-ejb/AuditMessageValidationWSService/AuditMessageValidationWS";

    public static final String GAZELLE_CDA_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_CDA_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/CDAGenerator-ejb/ModelBasedValidationWSService/ModelBasedValidationWS";

    public static final String GAZELLE_CERTIFICATE_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_CERTIFICATE_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/gazelle-atna-ejb/CertificateValidatorService/CertificateValidator";

    public static final String GAZELLE_SCHEMATRON_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_SCHEMATRON_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/SchematronValidator-ejb/GazelleObjectValidatorService/GazelleObjectValidator";

    public static final String GAZELLE_XDS_VALIDATOR_URI = ConfigurationManagerFactory.getConfigurationManager()
            .getProperty("GAZELLE_XDS_VALIDATOR_URI");
    //"https://gazelle.ehdsi.ihe-europe.net/XDStarClient-ejb/ModelBasedValidationWSService/ModelBasedValidationWS";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGazelleValidatorFactory.class);

    private final ConfigurationManager configurationManager;

    public DefaultGazelleValidatorFactory(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public AssertionValidator getAssertionValidator() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.assertion.sante");

        return new AssertionValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_ASSERTION_VALIDATOR_URI));
    }

    @Override
    public AuditMessageValidator getAuditMessageValidator() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.audit.sante");

        return new AuditMessageValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_AUDIT_MESSAGE_VALIDATOR_URI));
    }

    @Override
    public CdaValidator getCdaValidator() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.cda.sante", "net.ihe.gazelle.jaxb.result.sante");
        return new CdaValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_CDA_VALIDATOR_URI));
    }

    @Override
    public CertificateValidator getCertificateValidator() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.certificate.sante");

        return new CertificateValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_CERTIFICATE_VALIDATOR_URI));
    }

    @Override
    public SchematronValidator getSchematronValidator() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.schematron.sante");

        return new SchematronValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_SCHEMATRON_VALIDATOR_URI));
    }

    @Override
    public XdsValidator getXdsValidator() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        //marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.xsd.sante");
        marshaller.setPackagesToScan("net.ihe.gazelle.jaxb.xds.sante");

        return new XdsValidatorImpl(createWebServiceTemplate(marshaller, GAZELLE_XDS_VALIDATOR_URI));
    }

    private WebServiceTemplate createWebServiceTemplate(Marshaller marshaller, String defaultUri) {

        LOGGER.debug("Configuring WebServiceTemplate ...");

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());

        boolean isBehindProxy = configurationManager.getBooleanProperty(StandardProperties.HTTP_PROXY_USED);
        LOGGER.info("NCP Node is behind a proxy: '{}'", isBehindProxy);
        if (isBehindProxy) {

            String hostname = configurationManager.getProperty(StandardProperties.HTTP_PROXY_HOST);
            int port = configurationManager.getIntegerProperty(StandardProperties.HTTP_PROXY_PORT);

            httpClientBuilder.setProxy(new HttpHost(hostname, port));
            LOGGER.info("NCP Proxy is secured by Credentials: '{}'", configurationManager.getBooleanProperty(StandardProperties.HTTP_PROXY_AUTHENTICATED));

            if (configurationManager.getBooleanProperty(StandardProperties.HTTP_PROXY_AUTHENTICATED)) {

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(hostname, port),
                        new UsernamePasswordCredentials(configurationManager.getProperty(StandardProperties.HTTP_PROXY_USERNAME),
                                configurationManager.getProperty(StandardProperties.HTTP_PROXY_PASSWORD)));

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(marshaller);
        webServiceTemplate.setDefaultUri(defaultUri);
        webServiceTemplate.setMessageSender(new HttpComponentsMessageSender(httpClientBuilder.build()));
        return webServiceTemplate;
    }
}
