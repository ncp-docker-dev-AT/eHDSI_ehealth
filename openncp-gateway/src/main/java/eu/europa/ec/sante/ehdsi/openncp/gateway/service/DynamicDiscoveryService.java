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
import tr.com.srdc.epsos.util.XMLUtil;

import javax.net.ssl.SSLContext;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.net.URI;
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
    private static final String URN_EHDSI_ISM = "http://ec.europa.eu/sante/ehncp/ism";

    private DynamicDiscoveryService() {
    }

    /**
     * @param sslContext
     * @return
     */
    public static CloseableHttpClient buildHttpClient(SSLContext sslContext) {

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                //  new String[]{"TLSv1.2"}, // Allow TLSv1.2 protocol only
                //   null,
                //SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                new NoopHostnameVerifier());

        ProxyCredentials proxyCredentials = null;
        if (ProxyUtil.isProxyAuthenticationMandatory()) {
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
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        }
        return httpclient;
    }

    /**
     * @param countryCode
     */
    public static void fetchInternationalSearchMask(String countryCode) {

        String applicationBaseDir = System.getenv(StandardProperties.OPENNCP_BASEDIR) + "forms" + System.getProperty("file.separator");
        try {
            DynamicDiscovery smpClient = DynamicDiscoveryClient.getInstance();
            String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
            LOGGER.info("Querying ISM for participant identifier {}", participantIdentifierValue);
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                    PARTICIPANT_IDENTIFIER_SCHEME);

            ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier,
                    new DocumentIdentifier(RegisteredService.EHEALTH_107.getUrn(), DOCUMENT_IDENTIFIER_SCHEME));

            List<ProcessType> processTypes = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata()
                    .getServiceInformation().getProcessList().getProcess();

            if (!processTypes.isEmpty()) {
                List<EndpointType> endpointTypes = processTypes.get(0).getServiceEndpointList().getEndpoint();
                LOGGER.debug("EndpointType: '{}' - '{}'", endpointTypes, endpointTypes.size());
                if (!endpointTypes.isEmpty()) {
                    List<ExtensionType> extensionTypes = endpointTypes.get(0).getExtension();
                    LOGGER.debug("ExtensionType: '{}' - '{}'", extensionTypes, extensionTypes.size());
                    if (!extensionTypes.isEmpty()) {
                        Document document = ((ElementNSImpl) extensionTypes.get(0).getAny()).getOwnerDocument();

                        DOMSource source = new DOMSource(document.getElementsByTagNameNS(
                                URN_EHDSI_ISM, "patientSearch").item(0));

                        String outPath = applicationBaseDir + "InternationalSearch_" + StringUtils.upperCase(countryCode) + ".xml";
                        LOGGER.info("International Search Mask Path: '{}", outPath);
                        StreamResult result = new StreamResult(outPath);
                        XMLUtil.transformDocument(source, result);
                    }
                }
            }
            //Audit vars
            URI smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
            byte[] encodedObjectID = Base64.encodeBase64(smpURI.toASCIIString().getBytes());
            AuditManager.handleDynamicDiscoveryQuery(smpURI.toASCIIString(), new String(encodedObjectID), null, null);

        } catch (IOException | CertificateException | KeyStoreException | TechnicalException | TransformerException | NoSuchAlgorithmException e) {
            //TODO: [Specification] Analyze if an audit message is required in case of error.
            throw new ConfigurationManagerException("An internal error occurred while retrieving the International Search Mask from " + countryCode, e);
        }
    }
}
