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
import org.oasis_open.docs.bdxr.ns.smp._2016._05.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
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

    private final Environment environment;

    @Autowired
    public ServiceMetadataLocatorManager(Environment environment) {

        Assert.notNull(environment, "environment must not be null");
        this.environment = environment;
    }

    public void validateOpenNCP() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableEntryException {

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

    private java.security.cert.Certificate loadSignatureCertificate(String path, String alias, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {


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

    public void lookup() throws KeyStoreException, IOException, TechnicalException, CertificateException, NoSuchAlgorithmException {

        String KEYSTORE_PATH = environment.getRequiredProperty("openncp.truststore.path");
        String KEYSTORE_PASSWORD = environment.getRequiredProperty("openncp.truststore.password");
        String SML_DOMAIN = environment.getRequiredProperty("openncp.sml.domain");
        String PARTICIPANT_IDENTIFIER = "urn:ehealth:" + environment.getRequiredProperty("openncp.sml.country")
                + ":ncp-idp";


        LOGGER.info("Lookup: '{}'-'{}'", KEYSTORE_PATH, KEYSTORE_PASSWORD);
        KeyStore truststore = KeyStore.getInstance("JKS");

        LOGGER.info("Lookup Participant: '{}'", PARTICIPANT_IDENTIFIER);

        File file = new File(KEYSTORE_PATH);
        FileInputStream fileInputStream = new FileInputStream(file);

        truststore.load(fileInputStream, KEYSTORE_PASSWORD.toCharArray());

        DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                .locator(new DefaultBDXRLocator(SML_DOMAIN, new DefaultDNSLookup()))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                .build();

        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(PARTICIPANT_IDENTIFIER, "ehealth-participantid-qns");
        DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55", "ehealth-resid-qns");
        //DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:ISM::internationalsearchmask##ehealth-107", "ehealth-resid-qns");
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
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                transformer.transform(new DOMSource(document), new StreamResult(sw));
                //LOGGER.info(sw.toString());
            }
        } catch (TechnicalException | TransformerException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        ProcessListType processListType = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList();
        //smpClient.getServiceMetadata(participantIdentifier, documentIdentifier).getOriginalServiceMetadata().getServiceMetadata().get;
        for (ProcessType processType : processListType.getProcess()) {

            LOGGER.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier().getScheme());
            ServiceEndpointList serviceEndpointList = processType.getServiceEndpointList();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            for (EndpointType endpointType : serviceEndpointList.getEndpoint()) {
                LOGGER.info("Endpoint: '{}'", endpointType.getEndpointURI());

                LOGGER.info("ActivationDate: '{}'", format.format(endpointType.getServiceActivationDate().getTime()));
                LOGGER.info("ExpirationDate: '{}'", format.format(endpointType.getServiceExpirationDate().getTime()));
                LOGGER.info("Certificate:\n '{}'", endpointType.getCertificate());

            }

//            List<EndpointType> endpoints = serviceEndpointList.getEndpoint();
//            for (EndpointType endpointType : endpoints) {
//                LOGGER.info("Endpoint: '{}' - {}", endpointType.getExtension().size(), endpointType.getExtension().get(0).getClass());
//                ExtensionType extensionType = endpointType.getExtension().get(0);
//                LOGGER.info("ExtensionType: '{}' - '{}' \n '{}'", extensionType.getAny().getClass(),
//                        ((ElementNSImpl) extensionType.getAny()).getLocalName(), extensionType.getExtensionID());
//                Document document = ((ElementNSImpl) extensionType.getAny()).getOwnerDocument();
//
//                DOMSource source = new DOMSource(document.getElementsByTagName("patientSearch").item(0));
//                TransformerFactory transformerFactory = TransformerFactory.newInstance();
//                Transformer transformer = transformerFactory.newTransformer();
//                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//                StreamResult result = new StreamResult("/home/dg-sante/InternationalSearch_CH.xml");
//                transformer.transform(source, result);
//
//                StringWriter sw = new StringWriter();
//
//                try {
//                    TransformerFactory tf = TransformerFactory.newInstance();
//                    Transformer transformer1 = tf.newTransformer();
//                    transformer1.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//                    transformer1.setOutputProperty(OutputKeys.METHOD, "xml");
//                    transformer1.setOutputProperty(OutputKeys.INDENT, "yes");
//                    transformer1.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//                    transformer1.transform(new DOMSource(document), new StreamResult(sw));
//                } catch (TransformerException e) {
//                    LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
//                }
            //LOGGER.info("Extension: \n'{}'", sw.toString());
            //       }
        }

//        for(DocumentIdentifier identifier : smpClient.getDocumentIdentifiers(participantIdentifier)) {
//            LOGGER.info("Identifier: '{}'", identifier.getIdentifier());
//        }
//
//        for(ExtensionType extensionType : smpClient.getServiceGroup(participantIdentifier).getOriginalServiceGroup().getExtension()) {
//
//            LOGGER.info("ServiceGroup: '{}'", extensionType.getExtensionName());
//        }
//
//        ProcessListType processListType = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier).getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList();
//        for (ProcessType processType : processListType.getProcess()) {
//
//            LOGGER.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier().getScheme());
//            ServiceEndpointList serviceEndpointList = processType.getServiceEndpointList();
//            for(EndpointType endpointType : serviceEndpointList.getEndpoint()) {
//                LOGGER.info("Endpoint: '{}'", endpointType.getEndpointURI());
//
//            }
//        }
//
//
//        List<DocumentIdentifier> documentIdentifiers = smpClient.getDocumentIdentifiers(participantIdentifier);
//        LOGGER.info("Document Identifiers List: \n {}", documentIdentifiers);
//
//        URI uri = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
//        LOGGER.info("URI: {}", uri.getHost());
//
//        //DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:ism::internationalsearchmask##ehealth-107", "ehealth-resid-qns");
//        URI serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(uri, participantIdentifier);
//        URI serviceMetadataUri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(uri, participantIdentifier, documentIdentifier);
//
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info("URI ASCII: '{}'", uri.toASCIIString());
//            LOGGER.info("Service Group: '{}'", serviceGroup.toASCIIString());
//            LOGGER.info("DocumentIdentifier: '{}'", serviceMetadataUri.toASCIIString());
//        }
//
//        ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
//        LOGGER.info("ServiceMetadata '{}'.", sm.toString());
//        List<Endpoint> endpoints = sm.getEndpoints();
//        LOGGER.info("Endpoints: '{}'", endpoints.size());
//        LOGGER.info("Endpoints Class: '{}'", endpoints.get(0).getClass());
//        for (Endpoint endpoint : endpoints) {
//            LOGGER.info("Endpoint: '{}'", endpoint);
//            LOGGER.info("Endpoint: '{}'", endpoint.getAddress());
//        }
//
//
//        ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
//        for (ExtensionType extensionType : serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getExtension()) {
//            LOGGER.info("EndpointType: '{}'", extensionType.getAny());
//        }
//        List<ExtensionType> extensionTypeList;
//        try {
//            extensionTypeList = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier).getOriginalServiceMetadata().getServiceMetadata()
//                    .getServiceInformation().getProcessList().getProcess().get(0).getServiceEndpointList()
//                    .getEndpoint().get(0).getExtension();
//            if (!extensionTypeList.isEmpty()) {
//                LOGGER.info("List<ExtensionType> '{}'", extensionTypeList.size());
//            }
//        } catch (TechnicalException e) {
//            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
//        }


//            for (EndpointType endpointType : processType.getServiceEndpointList().getEndpoint().g) {
//                LOGGER.info("EndpointType: '{}'", endpointType.getEndpointURI());
//            }
        //      LOGGER.info("Extension: '{}'", extensionType.getAny().toString());
        //  }

    }

    //  public void lookupOpenNCP(String countryCode, String documentType, String key) {
    public void lookupOpenNCP() {

        String KEYSTORE_PATH = environment.getRequiredProperty("openncp.truststore.path");
        String KEYSTORE_PASSWORD = environment.getRequiredProperty("openncp.truststore.password");
        String SML_DOMAIN = environment.getRequiredProperty("openncp.sml.domain");
        String PARTICIPANT_IDENTIFIER = "urn:ehealth:" + environment.getRequiredProperty("openncp.sml.country") + ":ncp-idp";

        try {

            //String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
            LOGGER.info("participantIdentifierValue '{}'.", PARTICIPANT_IDENTIFIER);
            KeyStore ks = KeyStore.getInstance("JKS");

            File file = new File(KEYSTORE_PATH);
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                ks.load(fileInputStream, KEYSTORE_PASSWORD.toCharArray());

                DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                        .locator(new DefaultBDXRLocator(SML_DOMAIN, new DefaultDNSLookup()))
                        .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)))
                        .build();

                DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55", "ehealth-resid-qns");
                //DocumentIdentifier documentIdentifier = new DocumentIdentifier(documentType, DOCUMENT_IDENTIFIER_SCHEME);
                ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(PARTICIPANT_IDENTIFIER, PARTICIPANT_IDENTIFIER_SCHEME);

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
                        throw new Exception(
                                "Invalid number of endpoints found (" + size + "). This implementation works just with 1.");
                    }

                    EndpointType e = endpoints.get(0);
                    String address = e.getEndpointURI();
                    if (address == null) {
                        // throw new Exception("No address found for: " + documentType + ":" + participantIdentifierValue);
                        throw new Exception("No address found for");
                    }
                    URL urlAddress = new URL(address);

                    InputStream inStream = new ByteArrayInputStream(e.getCertificate());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);

                    if (certificate == null) {
                        throw new Exception("no certificate found for endpoint: " + e.getEndpointURI());
                    }
                    LOGGER.info("Certificate: '{}'-'{}", certificate.getIssuerDN().getName(), certificate.getSerialNumber());
//                setAddress(urlAddress);
//                setCertificate(certificate);
                }

//            URL endpointUrl = getAddress();
//            if (endpointUrl == null) {
//                throw new PropertyNotFoundException("Property '" + key + "' not found!");
//            }
//
//            String value = endpointUrl.toExternalForm();
//            LOGGER.info("Storing endpoint to database: '{}' - '{}'", key, value);
//            ConfigurationManagerFactory.getConfigurationManager().setProperty(key, value);
//
//            X509Certificate certificate = getCertificate();
//            if (certificate != null) {
//                String endpointId = countryCode.toLowerCase() + "_" + StringUtils.substringAfter(documentType, "##");
//                storeEndpointCertificate(endpointId, certificate);
//            }
            }
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            // throw new ConfigurationManagerException(e);
        }
    }
}
