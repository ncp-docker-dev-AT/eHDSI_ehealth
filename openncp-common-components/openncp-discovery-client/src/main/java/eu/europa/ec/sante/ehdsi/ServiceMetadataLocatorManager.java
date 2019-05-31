package eu.europa.ec.sante.ehdsi;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class ServiceMetadataLocatorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMetadataLocatorManager.class);

    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";
    private static final String DOCUMENT_IDENTIFIER_ITI_55 = "urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55";

    private final Environment environment;

    private final ResourceLoader resourceLoader;

    @Autowired
    public ServiceMetadataLocatorManager(Environment environment, ResourceLoader resourceLoader) {

        Assert.notNull(environment, "environment must not be null");
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    /**
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     * @throws UnrecoverableEntryException
     */
    public void validateOpenNCP() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableEntryException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Validate  Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.signature.path"),
                    environment.getRequiredProperty("openncp.signature.alias"),
                    environment.getRequiredProperty("openncp.signature.password")).toString());

            LOGGER.info("Consumer Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.consumer.path"),
                    environment.getRequiredProperty("openncp.consumer.alias"),
                    environment.getRequiredProperty("openncp.consumer.password")).toString());

            LOGGER.info("Provider  Signature Certificate: '{}'", loadSignatureCertificate(
                    environment.getRequiredProperty("openncp.provider.path"),
                    environment.getRequiredProperty("openncp.provider.alias"),
                    environment.getRequiredProperty("openncp.provider.password")).toString());
        }
    }

    /**
     * @param path
     * @param alias
     * @param password
     * @return
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     */
    private java.security.cert.Certificate loadSignatureCertificate(String path, String alias, String password)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {


        KeyStore truststore = KeyStore.getInstance("JKS");

        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        truststore.load(fileInputStream, password.toCharArray());

        X509Certificate x509Certificate = (X509Certificate) truststore.getCertificate(alias);

        LOGGER.info("Principal Issuer: '{}' SubjectDN: '{}' Serial Number: '{}'", x509Certificate.getIssuerX500Principal().getName(),
                x509Certificate.getSubjectDN().getName(), x509Certificate.getSerialNumber());

        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(password.toCharArray());
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) truststore.getEntry(alias, pp);

        for (Certificate certificate : entry.getCertificateChain()) {

            X509Certificate cert = (X509Certificate) certificate;
            LOGGER.info("Server certificate '{}':", (cert.getSerialNumber()));
            LOGGER.info("Subject DN: '{}'", cert.getSubjectDN());
            LOGGER.info("Signature Algorithm: '{}'", cert.getSigAlgName());
            LOGGER.info("Valid from: '{}'", cert.getNotBefore());
            LOGGER.info("Valid until: '{}'", cert.getNotAfter());
            LOGGER.info("Issuer: '{}'", cert.getIssuerDN());
            LOGGER.info("Certificate chain from Private Key: '{}'", cert.getIssuerDN().getName());
        }

        return x509Certificate;
    }

    /**
     * @throws KeyStoreException
     * @throws IOException
     * @throws TechnicalException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public void lookup() throws KeyStoreException, IOException, TechnicalException, CertificateException, NoSuchAlgorithmException {

        String trustStorePath = environment.getRequiredProperty("openncp.truststore.path");
        String trustStorePassword = environment.getRequiredProperty("openncp.truststore.password");
        String serviceMetadataLocatorDomain = environment.getRequiredProperty("openncp.sml.domain");
        String participantIdentifierUrn = "urn:ehealth:" + environment.getRequiredProperty("openncp.sml.country") + ":ncp-idp";

        LOGGER.info("Lookup: '{}'-'{}'", trustStorePath, trustStorePassword);
        KeyStore truststore = KeyStore.getInstance("JKS");

        LOGGER.info("Lookup Participant: '{}'", participantIdentifierUrn);

        File file = new File(trustStorePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        truststore.load(fileInputStream, trustStorePassword.toCharArray());

        DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                .locator(new DefaultBDXRLocator(serviceMetadataLocatorDomain, new DefaultDNSLookup()))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                .build();

        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierUrn, PARTICIPANT_IDENTIFIER_SCHEME);
        DocumentIdentifier documentIdentifier = new DocumentIdentifier(DOCUMENT_IDENTIFIER_ITI_55, DOCUMENT_IDENTIFIER_SCHEME);
        List<DocumentIdentifier> documentIdentifiers = smpClient.getServiceGroup(participantIdentifier).getDocumentIdentifiers();
        for (DocumentIdentifier identifier : documentIdentifiers) {
            LOGGER.info("Identifiers: '{}'-'{}'", identifier.getFullIdentifier(), identifier.getIdentifier());
        }

        ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        for (ExtensionType extensionType : serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getExtension()) {
            LOGGER.info("EndpointType: '{}'", extensionType.getAny());
        }
        List<ExtensionType> extensionTypeList;
        try {
            extensionTypeList = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier).getOriginalServiceMetadata().getServiceMetadata()
                    .getServiceInformation().getProcessList().getProcess().get(0).getServiceEndpointList()
                    .getEndpoint().get(0).getExtension();

            if (!extensionTypeList.isEmpty()) {
                LOGGER.info("List<ExtensionType> '{}' - '{}'", extensionTypeList.size(), extensionTypeList.get(0).getClass());
                LOGGER.info("Extension Any Class: '{}'", extensionTypeList.get(0).getAny().getClass());

                Document document = ((ElementNSImpl) extensionTypeList.get(0).getAny()).getOwnerDocument();

                StringWriter sw = new StringWriter();
                TransformerFactory tf = TransformerFactory.newInstance();
                tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                transformer.transform(new DOMSource(document), new StreamResult(sw));
            }
        } catch (TechnicalException | TransformerException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        ProcessListType processListType = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList();

        for (ProcessType processType : processListType.getProcess()) {

            LOGGER.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier().getScheme());
            ServiceEndpointList serviceEndpointList = processType.getServiceEndpointList();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            for (EndpointType endpointType : serviceEndpointList.getEndpoint()) {

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Endpoint: '{}'", endpointType.getEndpointURI());
                    LOGGER.info("ActivationDate: '{}'", format.format(endpointType.getServiceActivationDate().getTime()));
                    LOGGER.info("ExpirationDate: '{}'", format.format(endpointType.getServiceExpirationDate().getTime()));
                    LOGGER.info("Certificate:\n '{}'", endpointType.getCertificate());
                }
            }
        }
    }

    /**
     *
     */
    public void lookupOpenNCP() {

        String trustStorePath = environment.getRequiredProperty("openncp.truststore.path");
        String trustStorePassword = environment.getRequiredProperty("openncp.truststore.password");
        String serviceMetadataLocatorDomain = environment.getRequiredProperty("openncp.sml.domain");
        String participantIdentifierUrn = String.format(PARTICIPANT_IDENTIFIER_VALUE, environment.getRequiredProperty("openncp.sml.country"));

        try {

            LOGGER.info("participantIdentifierValue '{}'.", participantIdentifierUrn);
            LOGGER.info("NAPTR Hash: '{}'", HashUtil.getSHA256HashBase32(participantIdentifierUrn));
            LOGGER.info("CNAME Hash: '{}'", StringUtils.lowerCase("b-" + HashUtil.getMD5Hash(participantIdentifierUrn)));
            KeyStore ks = KeyStore.getInstance("JKS");

            Resource resource = resourceLoader.getResource("classpath:" + trustStorePath);

            try (InputStream inputStream = resource.getInputStream()) {
                ks.load(inputStream, trustStorePassword.toCharArray());

                DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                        .locator(new DefaultBDXRLocator(serviceMetadataLocatorDomain, new DefaultDNSLookup()))
                        .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)))
                        .build();

                DocumentIdentifier documentIdentifier = new DocumentIdentifier(DOCUMENT_IDENTIFIER_ITI_55, DOCUMENT_IDENTIFIER_SCHEME);
                ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierUrn, PARTICIPANT_IDENTIFIER_SCHEME);
                URI smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
                LOGGER.info("DNS: '{}'", smpURI.toASCIIString());
                ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
                LOGGER.info("ServiceMetadata '{}'.", serviceMetadata.toString());
                ProcessListType processListType = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation()
                        .getProcessList();
                for (ProcessType processType : processListType.getProcess()) {

                    LOGGER.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier()
                            .getScheme());
                    ServiceEndpointList serviceEndpointList = processType.getServiceEndpointList();
                    for (EndpointType endpointType : serviceEndpointList.getEndpoint()) {
                        LOGGER.info("Endpoint: '{}'", endpointType.getEndpointURI());
                    }
                    List<EndpointType> endpoints = serviceEndpointList.getEndpoint();

                    /*
                     * Constraint: here I think I have just one endpoint
                     */
                    int size = endpoints.size();
                    if (size != 1) {
                        throw new DynamicDiscoveryClientException(
                                "Invalid number of endpoints found (" + size + "). This implementation works just with 1.");
                    }

                    EndpointType e = endpoints.get(0);
                    String address = e.getEndpointURI();
                    if (address == null) {
                        throw new DynamicDiscoveryClientException("No address found for");
                    }
                    URL urlAddress = new URL(address);

                    InputStream inStream = new ByteArrayInputStream(e.getCertificate());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);

                    if (certificate == null) {
                        throw new DynamicDiscoveryClientException("No certificate found for endpoint: " + e.getEndpointURI());
                    }
                    LOGGER.info("Certificate: '{}'-'{}", certificate.getIssuerDN().getName(), certificate.getSerialNumber());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }
}
