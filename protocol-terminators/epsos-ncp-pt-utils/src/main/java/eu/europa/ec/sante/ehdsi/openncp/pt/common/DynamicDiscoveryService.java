package eu.europa.ec.sante.ehdsi.openncp.pt.common;

import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.core.locator.dns.impl.DefaultDNSLookup;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditService;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.*;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.util.Assert;
import eu.europa.ec.sante.ehdsi.openncp.util.security.HashUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessListType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceEndpointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.http.HTTPUtil;
import tr.com.srdc.epsos.util.http.IPUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DynamicDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscoveryService.class);

    /**
     * Static constants for SMP identifiers.
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

    private static void sendAuditQuery(String sc_fullname, String sc_email, String sp_fullname, String sp_email,
                                       String partid, String sourceip, String targetip, String objectID,
                                       String EM_PatricipantObjectID, byte[] EM_PatricipantObjectDetail, String smpServer) {

        LOGGER.info("sendAuditQuery('{}', '{}','{}','{}','{}','{}','{}','{}','{}','{}')", sc_fullname, sc_email,
                sp_fullname, sp_email, partid, sourceip, targetip, objectID, "EM_PatricipantObjectID", "EM_PatricipantObjectDetail");
        try {
            AuditService asd = AuditServiceFactory.getInstance();
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            XMLGregorianCalendar date2 = null;
            try {
                date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            } catch (DatatypeConfigurationException ex) {
                LOGGER.error(null, ex);
            }
            String serviceConsumerUserId = HTTPUtil.getSubjectDN(false);
            String serviceProviderUserId = HTTPUtil.getTlsCertificateCommonName(smpServer);

            EventLog eventLog1 = EventLog.createEventLogPatientPrivacy(TransactionName.SMP_QUERY, EventActionCode.EXECUTE,
                    date2, EventOutcomeIndicator.FULL_SUCCESS, null, null, null,
                    serviceConsumerUserId, serviceProviderUserId, partid, null, EM_PatricipantObjectID,
                    EM_PatricipantObjectDetail, objectID, null, new byte[1], null,
                    new byte[1], sourceip, targetip);
            eventLog1.setNcpSide(NcpSide.NCP_B);
            eventLog1.setEventType(EventType.SMP_QUERY);

            // According to https://tools.ietf.org/html/rfc5424 (Syslog Protocol)
            // facility = 13 --> log audit | severity = 2 --> Critical: critical conditions
            asd.write(eventLog1, "13", "2");

        } catch (Exception e) {
            LOGGER.error("Error sending audit for eHealth SMP Query: '{}'", e.getMessage(), e);
        }
    }

    public String getEndpointUrl(String countryCode, RegisteredService service) {

        return getEndpointUrl(countryCode, service, false);
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public URL getAddress() {
        return address;
    }

    public void setAddress(URL address) {
        this.address = address;
    }

    public Document getExtension() {
        return extension;
    }

    public void setExtension(Document extension) {
        this.extension = extension;
    }

    public String getEndpointUrl(String countryCode, RegisteredService service, boolean refresh) {

        Assert.notNull(countryCode, "countryCode must not be null!");
        Assert.notNull(service, "service must not be null!");
        LOGGER.info("getEndpointUrl('{}', '{}')", countryCode, service.getServiceName());
        String key = countryCode.toLowerCase() + "." + service.getServiceName() + ".WSE";
        try {
            if (!refresh) {
                try {
                    return ConfigurationManagerFactory.getConfigurationManager().getProperty(key);
                } catch (PropertyNotFoundException e) {
                    LOGGER.warn("PropertyNotFoundException: '{}'", e.getMessage());
                    lookup(countryCode, service.getUrn(), key);
                    return getAddress().toExternalForm();
                }
            }
            lookup(countryCode, service.getUrn(), key);
            return getAddress().toExternalForm();
        } catch (ConfigurationManagerException e) {
            LOGGER.error("SMLSMPClientException: '{}'", e.getMessage(), e);
            throw new ConfigurationManagerException("An internal error occurred while retrieving the endpoint URL", e);
        }
    }

    private void lookup(String countryCode, String documentType, String key) throws ConfigurationManagerException {

        LOGGER.info("SML Client: '{}'-'{}'", countryCode, documentType);
        try {

            String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
            LOGGER.debug("****** participantIdentifierValue '{}'.", participantIdentifierValue);
            LOGGER.debug("****** NAPTR Hash: '{}'", HashUtil.getSHA256HashBase32(participantIdentifierValue));
            LOGGER.debug("****** CNAME Hash: '{}'", StringUtils.lowerCase("b-" + HashUtil.getMD5Hash(participantIdentifierValue)));
            KeyStore ks = KeyStore.getInstance("JKS");

            File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
            FileInputStream fileInputStream = new FileInputStream(file);
            ks.load(fileInputStream, ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

            DynamicDiscovery smpClient = ConfigurationManagerFactory.getConfigurationManager().initializeDynamicDiscoveryFetcher()
                    .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager().getProperty("SML_DOMAIN"), new DefaultDNSLookup()))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(ks)))
                    .build();

            DocumentIdentifier documentIdentifier = new DocumentIdentifier(documentType, DOCUMENT_IDENTIFIER_SCHEME);
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue, PARTICIPANT_IDENTIFIER_SCHEME);

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

                if (certificate == null) {
                    throw new Exception("no certificate found for endpoint: " + e.getEndpointURI());
                }
                LOGGER.info("Certificate: '{}'-'{}", certificate.getIssuerDN().getName(), certificate.getSerialNumber());
                setAddress(urlAddress);
                setCertificate(certificate);
            }

            URL endpointUrl = getAddress();
            if (endpointUrl == null) {
                throw new PropertyNotFoundException("Property '" + key + "' not found!");
            }

            String value = endpointUrl.toExternalForm();
            LOGGER.info("Storing endpoint to database: '{}' - '{}'", key, value);
            ConfigurationManagerFactory.getConfigurationManager().setProperty(key, value);

            X509Certificate certificate = getCertificate();
            if (certificate != null) {
                String endpointId = countryCode.toLowerCase() + "_" + StringUtils.substringAfter(documentType, "##");
                storeEndpointCertificate(endpointId, certificate);
            }

            //Audit vars
            String ncp = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country");
            String ncpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.email");
            String country = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");

            String localIp = IPUtil.getPrivateServerIp();
            String smp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_SUPPORT");
            String smpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_SUPPORT_EMAIL");
            //ET_ObjectID --> Base64 of url
            String objectID = getAddress().toString(); //ParticipantObjectID
            LOGGER.info("No address found for: '{}'", getAddress());
            LOGGER.info("objectID: '{}'", objectID);
            byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());
            if (encodedObjectID != null) {
                LOGGER.info("encodedObjectID not NULL");
            } else {
                LOGGER.info("encodedObjectID NULL");
            }
            LOGGER.info("Sending audit trail");
            //TODO: Request Audit SMP Query
            URI smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
            LOGGER.info("DNS: '{}'", smpURI);
            sendAuditQuery(ncp, ncpemail, smp, smpemail, country, localIp, smpURI.getHost(), new String(encodedObjectID),
                    null, null, smpURI.toASCIIString());


        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            throw new ConfigurationManagerException(e);
        }
    }

    private void storeEndpointCertificate(String endpointId, X509Certificate certificate) {

        // Store the endpoint certificate in the truststore
        String trustStorePath = ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.NCP_TRUSTSTORE);
        char[] trustStorePassword = ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.NCP_TRUSTSTORE_PASSWORD).toCharArray();

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream is = new FileInputStream(trustStorePath)) {
                trustStore.load(is, trustStorePassword);
            }
            String alias = Base64.encodeBase64String(DigestUtils.md5(certificate.getSubjectDN().getName()));
            trustStore.setCertificateEntry(alias, certificate);
            try (OutputStream os = new FileOutputStream(trustStorePath)) {
                trustStore.store(os, trustStorePassword);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new ConfigurationManagerException("An error occurred while storing the endpoint certificate in the truststore!", e);
        }

        // Store the endpoint certificate in the file system
        File certificateFile = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty(
                StandardProperties.NCP_CERTIFICATES_DIRECTORY), endpointId + ".der");
        try (OutputStream os = new FileOutputStream(certificateFile)) {
            os.write(certificate.getEncoded());

        } catch (CertificateException | IOException e) {
            throw new ConfigurationManagerException("An error occurred while storing the endpoint certificate in the file system!", e);
        }
    }
}
