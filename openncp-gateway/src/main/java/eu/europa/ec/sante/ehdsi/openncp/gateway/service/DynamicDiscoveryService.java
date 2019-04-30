package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import eu.epsos.util.net.ProxyCredentials;
import eu.epsos.util.net.ProxyUtil;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.DynamicDiscoveryClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ExtensionType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.net.ssl.SSLContext;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

@Service
public class DynamicDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscoveryService.class);
    /**
     * Static constants for SMP identifiers
     */
    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";

    private DynamicDiscoveryService() {
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

    public static void fetchInternationalSearchMask(String countryCode) {
        try {
            LOGGER.info("fetchInternationalSearchMask({}) - '{}'", countryCode, RegisteredService.EHEALTH_107.getUrn());
            String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH") + "forms" + System.getProperty("file.separator");
            try {

                KeyStore trustStore = KeyStore.getInstance("JKS");
                File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    trustStore.load(fileInputStream, ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

                    DynamicDiscovery smpClient = DynamicDiscoveryClient.getInstance();

                    String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
                    LOGGER.info("Querying for participant identifier {}", participantIdentifierValue);
                    ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                            PARTICIPANT_IDENTIFIER_SCHEME);

                    LOGGER.info("Querying for service metadata");
                    ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier,
                            new DocumentIdentifier(RegisteredService.EHEALTH_107.getUrn(), DOCUMENT_IDENTIFIER_SCHEME));

                    LOGGER.info("DocumentIdentifier: '{}' - '{}'",
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getScheme(),
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getValue());

                    LOGGER.info("ParticipantIdentifier: '{}' - '{}'",
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getScheme(),
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getValue());

                    List<ProcessType> processTypes = sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess();
                    LOGGER.info("ProcessType: '{}' - '{}'", processTypes, processTypes.size());
                    if (!processTypes.isEmpty()) {
                        List<EndpointType> endpointTypes = processTypes.get(0).getServiceEndpointList().getEndpoint();
                        LOGGER.info("EndpointType: '{}' - '{}'", endpointTypes, endpointTypes.size());
                        if (!endpointTypes.isEmpty()) {
                            List<ExtensionType> extensionTypes = endpointTypes.get(0).getExtension();
                            LOGGER.info("ExtensionType: '{}' - '{}'", extensionTypes, extensionTypes.size());
                            if (!extensionTypes.isEmpty()) {
                                Document document = ((ElementNSImpl) extensionTypes.get(0).getAny()).getOwnerDocument();

                                DOMSource source = new DOMSource(document.getElementsByTagNameNS(
                                        "http://ec.europa.eu/sante/ehncp/ism", "patientSearch").item(0));
                                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                                Transformer transformer = transformerFactory.newTransformer();
                                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                                String outPath = epsosPropsPath + "InternationalSearch_" + StringUtils.upperCase(countryCode) + ".xml";
                                LOGGER.info("International Search Mask Path: '{}", outPath);
                                StreamResult result = new StreamResult(outPath);
                                transformer.transform(source, result);
                            }
                        }
                    }
                    //Audit vars
                    String ncp = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country");
                    String ncpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.email");
                    String country = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
                    //String remoteip = ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.SMP_SML_ADMIN_URL);//Source Gateway
                    String localip = ConfigurationManagerFactory.getConfigurationManager().getProperty("SERVER_IP");//Target Gateway
                    String smp = ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.SMP_SML_SUPPORT);
                    String smpemail = ConfigurationManagerFactory.getConfigurationManager().getProperty(StandardProperties.SMP_SML_SUPPORT_EMAIL);
                    //ET_ObjectID --> Base64 of url
                    //String objectID = uri.toString(); //ParticipantObjectID

                    URI smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
                    LOGGER.info("DNS: '{}'", smpURI);
                    byte[] encodedObjectID = Base64.encodeBase64(smpURI.toASCIIString().getBytes());
                    AuditManager.handleDynamicDiscoveryQuery(smpURI.toASCIIString(), new String(encodedObjectID), null, null);
                    //AuditManager.sendAuditQuery(smp, smpemail, ncp, ncpemail, country, localip, smpURI.toASCIIString(),
                    //       new String(encodedObjectID), null, null, smpURI.toASCIIString());
                }

            } catch (NoSuchAlgorithmException e) {
                throw new ConfigurationManagerException(e);
            } catch (IOException | CertificateException | KeyStoreException | TechnicalException | TransformerException e) {
                LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
            }

        } catch (Exception e) {
            throw new ConfigurationManagerException("An internal error occurred while retrieving the International Search Mask", e);
        }
    }
}
