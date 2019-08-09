package eu.europa.ec.sante.ehdsi;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessListType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceEndpointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

@Component
public class ServiceMetadataLocatorManager {

    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";
    private static final String DOCUMENT_IDENTIFIER_ITI_55 = "urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55";
    private final Logger logger = LoggerFactory.getLogger(ServiceMetadataLocatorManager.class);
    private final Environment environment;

    private final ResourceLoader resourceLoader;

    @Autowired
    public ServiceMetadataLocatorManager(Environment environment, ResourceLoader resourceLoader) {

        Assert.notNull(environment, "Environment must not be null");
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    public void validateOpenNCP() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableEntryException {

        if (logger.isInfoEnabled()) {
            logger.info("Validate  Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.signature.path"),
                    environment.getRequiredProperty("openncp.signature.alias"),
                    environment.getRequiredProperty("openncp.signature.password")).toString());

            logger.info("Consumer Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.consumer.path"),
                    environment.getRequiredProperty("openncp.consumer.alias"),
                    environment.getRequiredProperty("openncp.consumer.password")).toString());

            logger.info("Provider  Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.provider.path"),
                    environment.getRequiredProperty("openncp.provider.alias"),
                    environment.getRequiredProperty("openncp.provider.password")).toString());
        }
    }

    private Certificate loadSignatureCertificate(String path, String alias, String password)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {

        KeyStore truststore = KeyStore.getInstance("JKS");

        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        truststore.load(fileInputStream, password.toCharArray());

        X509Certificate x509Certificate = (X509Certificate) truststore.getCertificate(alias);

        logger.info("Principal Issuer: '{}' SubjectDN: '{}' Serial Number: '{}'", x509Certificate.getIssuerX500Principal().getName(),
                x509Certificate.getSubjectDN().getName(), x509Certificate.getSerialNumber());

        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(password.toCharArray());
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) truststore.getEntry(alias, pp);

        for (Certificate certificate : entry.getCertificateChain()) {

            X509Certificate cert = (X509Certificate) certificate;
            logger.info("Server certificate '{}':", (cert.getSerialNumber()));
            logger.info("Subject DN: '{}'", cert.getSubjectDN());
            logger.info("Signature Algorithm: '{}'", cert.getSigAlgName());
            logger.info("Valid from: '{}'", cert.getNotBefore());
            logger.info("Valid until: '{}'", cert.getNotAfter());
            logger.info("Issuer: '{}'", cert.getIssuerDN());
            logger.info("Certificate chain from Private Key: '{}'", cert.getIssuerDN().getName());
        }

        return x509Certificate;
    }

    /**
     * Processing DNS query retrieving SMP information fot the Participant provided into the application.yml file.
     */
    public void lookup() {

        String trustStorePath = environment.getRequiredProperty("openncp.truststore.path");
        String trustStorePassword = environment.getRequiredProperty("openncp.truststore.password");

        String serviceMetadataLocatorDomain = environment.getRequiredProperty("openncp.sml.domain");
        String participantIdentifierUrn = String.format(PARTICIPANT_IDENTIFIER_VALUE, environment.getRequiredProperty("openncp.sml.country"));

        try {

            if (logger.isInfoEnabled()) {
                logger.info("participantIdentifierValue '{}'.", participantIdentifierUrn);
                logger.info("NAPTR Hash: '{}'", HashUtil.getSHA256HashBase32(participantIdentifierUrn));
                logger.info("CNAME Hash: '{}'", StringUtils.lowerCase("b-" + HashUtil.getMD5Hash(participantIdentifierUrn)));
            }
            KeyStore trustStore = getTruststore(trustStorePath, trustStorePassword);
            DynamicDiscovery smpClient = buildDynamicDiscoveryClient(serviceMetadataLocatorDomain, trustStore);

            DocumentIdentifier documentIdentifier = new DocumentIdentifier(DOCUMENT_IDENTIFIER_ITI_55, DOCUMENT_IDENTIFIER_SCHEME);
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierUrn, PARTICIPANT_IDENTIFIER_SCHEME);

            URI smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
            if (logger.isInfoEnabled()) {
                logger.info("DNS: '{}'", smpURI.toASCIIString());
            }

            ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            if (logger.isInfoEnabled()) {
                logger.info("ServiceMetadata '{}'.", serviceMetadata.toString());
            }

            List<DocumentIdentifier> documentIdentifiers = smpClient.getServiceGroup(participantIdentifier).getDocumentIdentifiers();
            for (DocumentIdentifier identifier : documentIdentifiers) {
                logger.info("Identifiers: '{}'-'{}'", identifier.getFullIdentifier(), identifier.getIdentifier());
            }

            ProcessListType processListType = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation()
                    .getProcessList();
            for (ProcessType processType : processListType.getProcess()) {

                logger.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier()
                        .getScheme());
                ServiceEndpointList serviceEndpointList = processType.getServiceEndpointList();
                for (EndpointType endpointType : serviceEndpointList.getEndpoint()) {
                    logger.info("Endpoint: '{}'", endpointType.getEndpointURI());
                    InputStream inStream = new ByteArrayInputStream(endpointType.getCertificate());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);

                    if (certificate == null) {
                        throw new DynamicDiscoveryClientException("No certificate found for endpoint: " + endpointType.getEndpointURI());
                    }
                    logger.info("Certificate: '{}'-'{} - Date: [{}/{}]", certificate.getIssuerDN().getName(), certificate.getSerialNumber(),
                            certificate.getNotBefore(), certificate.getNotAfter());
                }
            }
        } catch (Exception e) {
            logger.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    private DynamicDiscovery buildDynamicDiscoveryClient(String smlDomain, KeyStore trustStore) throws TechnicalException {

        boolean proxyEnabled = Boolean.parseBoolean(environment.getRequiredProperty("openncp.proxy.enabled"));
        boolean proxyAuthenticated = Boolean.parseBoolean(environment.getRequiredProperty("openncp.proxy.authenticated"));
        String proxyHost = environment.getRequiredProperty("openncp.proxy.host");
        int proxyPort = Integer.parseInt(environment.getRequiredProperty("openncp.proxy.port"));
        String proxyUsername = environment.getRequiredProperty("openncp.proxy.username");
        String proxyPassword = environment.getRequiredProperty("openncp.proxy.password");

        DynamicDiscoveryBuilder discoveryBuilder = DynamicDiscoveryBuilder.newInstance();
        if (proxyEnabled) {
            if (proxyAuthenticated) {
                logger.info("Configuring access through Authenticated Proxy '{}:{}' with Credentials: '{}/{}'",
                        proxyHost, proxyPort, proxyUsername, StringUtils.isNoneBlank(proxyPassword) ? "XXXXXX" : "No Password provided");
                discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort, proxyUsername, proxyPassword)));
            } else {
                logger.info("Configuring access through Proxy '{}:{}'", proxyHost, proxyPort);
                discoveryBuilder.fetcher(new DefaultURLFetcher(new DefaultProxy(proxyHost, proxyPort)));
            }
        }
        return discoveryBuilder.locator(new DefaultBDXRLocator(smlDomain, new DefaultDNSLookup()))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(trustStore)))
                .build();
    }

    private KeyStore getTruststore(String trustStorePath, String trustStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream;
        logger.info("DynamicDiscovery Client Truststore: '{}'", trustStorePath);
        FileSystemResource fileSystemResource = new FileSystemResource(new File(trustStorePath));
        if (fileSystemResource.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("Local Truststore Resource Loaded: '{}'", fileSystemResource.getURI().toASCIIString());
            }
            inputStream = fileSystemResource.getInputStream();
        } else {
            logger.info("Loading default Truststore");
            Resource resource = resourceLoader.getResource("classpath:keystore/openncp-truststore.jks");
            inputStream = resource.getInputStream();
        }
        keyStore.load(inputStream, trustStorePassword.toCharArray());
        return keyStore;
    }
}
