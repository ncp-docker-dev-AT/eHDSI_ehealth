package epsos.ccd.gnomon.configmanager;

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
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * This is the class which integrates the SMLSMP discovery client from the EU
 * commission (DG DIGIT) to be used by the Configuration Manager.
 *
 * @author max
 */
public class SMLSMPClient {

    /**
     * The logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(SMLSMPClient.class);

    /**
     * Static constants for SMP identifiers
     */
    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";

    /**
     * The certificate of the remote endpoint.
     */
    private X509Certificate certificate;

    /**
     * The URL address of the remote endpoint.
     */
    private URL address;

    /**
     * The XML contained in the Extension (used by search masks).
     */
    private Document extension;

    //TODO: Analyzing if auditing the SMP query is mandatory
//    private static void sendAuditQuery(String sc_fullname, String sc_email, String sp_fullname, String sp_email,
//                                       String partid, String sourceip, String targetip, String objectID, String EM_PatricipantObjectID,
//                                       byte[] EM_PatricipantObjectDetail) {
//        try {
//            AuditService asd = new AuditService();
//            GregorianCalendar c = new GregorianCalendar();
//            c.setTime(new Date());
//            XMLGregorianCalendar date2 = null;
//            try {
//                date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
//            } catch (DatatypeConfigurationException ex) {
//                LOGGER.error(null, ex);
//            }
//
//            String sc_userid = sc_fullname + "<saml:" + sc_email + ">";
//            String sp_userid = sp_fullname + "<saml:" + sp_email + ">";
//            EventLog eventLog1 = EventLog.createEventLogPatientPrivacy(TransactionName.ehealthSMPQuery,
//                    EventActionCode.EXECUTE, date2, EventOutcomeIndicator.FULL_SUCCESS, null, null, null, sc_userid,
//                    sp_userid, partid, null, EM_PatricipantObjectID, EM_PatricipantObjectDetail, objectID,
//                    "urn:uuid:00000000-0000-0000-0000-000000000000", new byte[1],
//                    "urn:uuid:00000000-0000-0000-0000-000000000000", new byte[1], // Base64
//                    // encoded
//                    // error
//                    // message
//                    sourceip, targetip);
//            eventLog1.setEventType(EventType.ehealthSMPQuery);
//            // facility = 13 --> log audit | severity = 2 --> Critical: critical
//            // conditions
//            // Acording to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
//            asd.write(eventLog1, "13", "2");
//            /*
//             * try { Thread.sleep(10000); } catch (InterruptedException ex) {
//			 * logger.error(null, ex); }
//			 */
//        } catch (Exception e) {
//            LOGGER.error("Error sending audit for eHealth SMP Query: '{}'", e.getMessage(), e);
//        }
//    }

    /**
     * Lookup in the SMP, using the SML for a given country code and document
     * type. And example query for, e.g., Portugal and XCPD is the following
     * <p>
     * <pre>
     * lookup("pt", "urn:ehealth:PatientIdentificationAndAuthentication::XCPD::CrossGatewayPatientDiscovery##ITI-55");
     * </pre>
     * <p>
     * then the methods {@link #getCertificate()} and
     * {@link #getEndpointReference()} can be used. <b>Note</b> that this class
     * is using a keystore to validate the signature of the SMP record, and it
     * should use another one to validate the signature. <br/>
     * <br/>
     * TODO: Make the keystores configurable. <br/>
     * <br/>
     *
     * @param countryCode  The lowercase two-letter country code (ISO 3166-1 alpha-2)
     * @param documentType The document type. Its format is given by the Document
     *                     Identifiers defined in the CP-eHealthDSI-002 for SMP/SML
     *                     capabilities.
     * @throws SMLSMPClientException For any error (including not found)
     */
    public void lookup(String countryCode, String documentType) throws SMLSMPClientException {

        LOGGER.info("SML Client: '{}'-'{}'", countryCode, documentType);
        try {
//            KeyStore ks = this.loadTrustStore();
//
//            DynamicDiscovery smpClient = this.createDynamicDiscoveryClient(ks);
//            LOGGER.info("DynamicDiscovery '{}' instantiated.", smpClient.toString());
            String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
            LOGGER.info("participantIdentifierValue '{}'.", participantIdentifierValue);
            //ServiceMetadata serviceMetadata = this.getServiceMetadata(smpClient, participantIdentifierValue, documentType);
            //LOGGER.info("ServiceMetadata '{}'.", serviceMetadata.toString());

            // 2.5.2.RC2 DG Sante

            KeyStore ks = KeyStore.getInstance("JKS");
            //KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

//            ks.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH")),
//                    ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

            //File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_KEYSTORE"));
            File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
            FileInputStream fileInputStream = new FileInputStream(file);
            ks.load(fileInputStream, ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

            DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                    //.locator(new DefaultBDXRLocator("ehealth.testa.eu", new DefaultDNSLookup()))
                    .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager().getProperty("SML_DOMAIN"), new DefaultDNSLookup()))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)))
                    .build();

            DocumentIdentifier documentIdentifier = new DocumentIdentifier(documentType,
                    DOCUMENT_IDENTIFIER_SCHEME);

            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                    PARTICIPANT_IDENTIFIER_SCHEME);

            LOGGER.info("Querying for service metadata");

            //ServiceMetadata serviceMetadata = this.getServiceMetadata(smpClient, participantIdentifierValue, documentType);
            ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            LOGGER.info("ServiceMetadata '{}'.", serviceMetadata.toString());
            ProcessListType processListType = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList();
            for (ProcessType processType : processListType.getProcess()) {

                LOGGER.info("ProcessType: '{}' - '{}'", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier().getScheme());
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
                    throw new Exception("No address found for: " + documentType + ":" + participantIdentifierValue);
                }
                URL urlAddress = new URL(address);


                InputStream inStream = new ByteArrayInputStream(e.getCertificate());
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);

                // X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
                if (certificate == null) {
                    throw new Exception("no certificate found for endpoint: " + e.getEndpointURI());
                }
                LOGGER.info("Certificate: '{}'-'{}", certificate.getIssuerDN().getName(), certificate.getSerialNumber());

                setAddress(urlAddress);
                setCertificate(certificate);
            }
            // 2.5.2.RC2 DG Sante

//            List<Endpoint> endpoints = serviceMetadata.getEndpoints();
//
//			/*
//             * Constraint: here I think I have just one endpoint
//			 */
//            int size = endpoints.size();
//            if (size != 1) {
//                throw new Exception(
//                        "Invalid number of endpoints found (" + size + "). This implementation works just with 1.");
//            }
//
//            Endpoint e = endpoints.get(0);
//            String address = e.getAddress();
//            if (address == null) {
//                throw new Exception("No address found for: " + documentType + ":" + participantIdentifierValue);
//            }
//            URL urlAddress = new URL(address);
//
//            X509Certificate certificate = e.getCertificate();
//            if (certificate == null) {
//                throw new Exception("no certificate found for endpoint: " + e.getAddress());
//            }
//            LOGGER.info("Certificate: '{}'-'{}", certificate.getIssuerDN().getName(), certificate.getSerialNumber());


//            //Audit vars
//            String ncp = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country");
//            String ncpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.email");
//            String country = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
//            String localip = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");//Source Gateway
//            String remoteip = ConfigurationManagerFactory.getConfigurationManager().getProperty("SERVER_IP");//Target Gateway
//            String smp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_SUPPORT");
//            String smpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_SUPPORT_EMAIL");
//            //ET_ObjectID --> Base64 of url
//            String objectID = urlAddress.toString(); //ParticipantObjectID
//            byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());
//
//            LOGGER.debug("Sending audit trail");
//            //TODO: Request Audit SMP Query
//            //sendAuditQuery(smp, smpemail, ncp, ncpemail, country, localip, remoteip, new String(encodedObjectID), null, null);


        } catch (Exception e) {
            throw new SMLSMPClientException(e);
        }

    }

    /**
     * Lookup for a search mask in the SMP, using the SML for a given country
     * code and document type. This will be called directly by the Portal to
     * fetch a search mask when it's not found. And example query for, e.g.,
     * Portugal and ISM is the following
     * <p>
     * <pre>
     * lookup("pt", "urn:ehealth:ISM::InternationalSearchMask##ehealth-107");
     * </pre>
     * <p>
     * then the method {@link #getExtension()} can be used. <b>Note</b> that
     * this class is using a keystore to validate the signature of the SMP
     * record, and it should use another one to validate the signature. <br/>
     * <br/>
     * TODO: Make the keystores configurable. <br/>
     * <br/>
     *
     * @param countryCode  The lowercase two-letter country code (ISO 3166-1 alpha-2)
     * @param documentType The document type. Its format is given by the Document
     *                     Identifiers defined in the CP-eHealthDSI-002 for SMP/SML
     *                     capabilities.
     * @throws SMLSMPClientException For any error (including not found)
     */
    public void fetchSearchMask(String countryCode, String documentType) throws SMLSMPClientException {

        String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH") + "forms";
        try {
            //KeyStore ks = this.loadTrustStore();
            //DynamicDiscovery smpClient = this.createDynamicDiscoveryClient(ks);
            KeyStore truststore = KeyStore.getInstance("JKS");

            File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
            FileInputStream fileInputStream = new FileInputStream(file);
            truststore.load(fileInputStream, ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

            DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                    .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager().getProperty("SML_DOMAIN"), new DefaultDNSLookup()))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                    .build();

            // ParticipantIdentifier participantIdentifier = new ParticipantIdentifier("urn:ehealth:ch:ncp-idp", "ehealth-participantid-qns");
            // DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55", "ehealth-resid-qns");
            // DocumentIdentifier documentIdentifier = new DocumentIdentifier("urn:ehealth:ism::internationalsearchmask##ehealth-107", "ehealth-resid-qns");

            String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
            ServiceMetadata sm = this.getServiceMetadata(smpClient, participantIdentifierValue, documentType);

            LOGGER.info("DocumentIdentifier: '{}' - '{}'",
                    sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getScheme(),
                    sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getValue());

            LOGGER.info("ParticipantIdentifier: '{}' - '{}'",
                    sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getScheme(),
                    sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getValue());

            List<ProcessType> processTypes = sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess();
            if (!processTypes.isEmpty()) {
                List<EndpointType> endpointTypes = processTypes.get(0).getServiceEndpointList().getEndpoint();
                if (!endpointTypes.isEmpty()) {
                    List<ExtensionType> extensionTypes = endpointTypes.get(0).getExtension();
                    if (!extensionTypes.isEmpty()) {
                        Document document = ((ElementNSImpl) extensionTypes.get(0).getAny()).getOwnerDocument();

                        DOMSource source = new DOMSource(document.getElementsByTagName("patientSearch").item(0));
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        StreamResult result = new StreamResult(epsosPropsPath + "InternationalSearch_" + countryCode + ".xml");
                        transformer.transform(source, result);
                    }

                }
            }
        } catch (Exception e) {
            throw new SMLSMPClientException(e);
        }
    }

    /**
     * Set the address of the newly discovered endpoint.
     *
     * @param address the address as URL
     */
    private synchronized void setAddress(URL address) {
        this.address = address;
    }

    /**
     * After calling the {@link #lookup(String, String)}, this method returns
     * the certificate
     *
     * @return the certificate of the newly discovered endpoint
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    /**
     * Set the certificate of the newly discovered endpoint.
     *
     * @param certificate The certificate
     */
    private synchronized void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;

    }

    /**
     * After calling the {@link #lookup(String, String)}, this method returns
     * the url
     *
     * @return the url of the newly discovered endpoint
     */
    public URL getEndpointReference() {
        return this.address;
    }

    /**
     * After calling the {@link #lookup(String, String)}, this method returns
     * the XML of the extension
     *
     * @return the XML of the extension
     */
    public Document getExtension() {
        return this.extension;
    }

    /**
     * Set the extension of the newly discovered resource (e.g., search masks).
     *
     * @param extension the XML extension as a Document
     */
    private synchronized void setExtension(Document extension) {
        this.extension = extension;
    }

    /**
     * Loads the truststore containing the certificate used by the SMP server to
     * sign SMP files, to be used when building the Dynamic Discovery Client.
     *
     * @return The truststore containing the certificate used by the SMP server
     * to sign SMP files
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    private KeyStore loadTrustStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH")),
                ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());
        return ks;
    }

    /**
     * Creates an instance of the Dynamic Discovery Client with a custom
     * validator for validating both the SMP server signature and the eHealth
     * specific national authority signature of the SMP file. OpenNCP proxy
     * settings are also taken into account.
     *
     * @param keyStore The truststore containing the certificate used by SMP server
     *                 to sign SMP files.
     * @return The Dynamic Discovery Client ready to use.
     * @throws TechnicalException
     */
    private DynamicDiscovery createDynamicDiscoveryClient(KeyStore keyStore) throws TechnicalException, KeyStoreException,
            IOException, CertificateException, NoSuchAlgorithmException {

        LOGGER.info("Instantiating the smpClient.");

        // Instantiate the DIGIT client using our customized signature
        // validator. This is due to
        // the fact that we need to evaluate our signature as well. We also
        // instantiate proxy credentials
        // in case they're needed.
        // TODO: Jerome cycling dependency confmanager --> util and util -->
        // confmanager
        // DynamicDiscovery smpClient = null;
        // ProxyCredentials proxyCredentials = null;
        // if (ProxyUtil.isProxyAnthenticationMandatory()) {
        // proxyCredentials = ProxyUtil.getProxyCredentials();
        // }
        // if (proxyCredentials != null) {
        // smpClient = DynamicDiscoveryBuilder.newInstance()
        // .locator(new
        // DefaultBDXRLocator(ConfigurationManagerService.getInstance().getProperty("SML_DOMAIN")))
        // .fetcher(new DefaultURLFetcher(new
        // CustomProxy(proxyCredentials.getProxyHost(),
        // Integer.parseInt(proxyCredentials.getProxyPort()),
        // proxyCredentials.getProxyUser(),
        // proxyCredentials.getProxyPassword())))
        // .reader(new CustomizedBDXRReader(new
        // CustomizedSignatureValidator(ks))).build();
        // } else {
//        smpClient = DynamicDiscoveryBuilder.newInstance()
//                .locator(new DefaultBDXRLocator(ConfigurationManagerService.getInstance().getProperty("SML_DOMAIN")))
//                .reader(new CustomizedBDXRReader(new CustomizedSignatureValidator(ks))).build();
        // }
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(ConfigurationManagerFactory.getConfigurationManager()
                .getProperty("SMP_KEYSTORE")), null);

        return DynamicDiscoveryBuilder.newInstance().locator(new DefaultBDXRLocator(
                ConfigurationManagerFactory.getConfigurationManager().getProperty("SML_DOMAIN"), new DefaultDNSLookup()))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(trustStore)))
                .build();
    }

    /**
     * Fetches the ServiceMetadata from the SMP server based on the combination
     * of the country code and document identifier.
     *
     * @param smpClient                  The Dynamic Discovery Client ready to be used
     * @param participantIdentifierValue The SMP Participant Identifier
     * @param documentType               The SMP Document Identifier
     * @return The ServiceMetadata for the requested ParticipantIdentifier and
     * Document Identifier
     * @throws TechnicalException
     */
    private ServiceMetadata getServiceMetadata(DynamicDiscovery smpClient, String participantIdentifierValue,
                                               String documentType) throws TechnicalException {

        LOGGER.info("Querying for participant identifier {}", participantIdentifierValue);
        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                PARTICIPANT_IDENTIFIER_SCHEME);

        LOGGER.info("Querying for service metadata");
        return smpClient.getServiceMetadata(participantIdentifier, new DocumentIdentifier(documentType,
                DOCUMENT_IDENTIFIER_SCHEME));
    }
}
