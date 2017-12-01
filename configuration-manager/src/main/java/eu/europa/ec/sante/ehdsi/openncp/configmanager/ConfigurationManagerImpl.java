package eu.europa.ec.sante.ehdsi.openncp.configmanager;

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
import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ExtensionType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
    /**
     * Static constants for SMP identifiers
     */
    private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncp-idp";
    private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";

    private SessionFactory sessionFactory;

    private Map<String, String> properties = new HashMap<>();

    public ConfigurationManagerImpl(SessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "sessionFactory must not be null!");
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getProperty(String key) {
        Assert.notNull(key, "key must not be null!");
        return findProperty(key)
                .orElseThrow(() -> new PropertyNotFoundException("Property '" + key + "' not found!"));
    }

    @Override
    public boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key));
    }

    @Override
    public int getIntegerProperty(String key) {
        return Integer.valueOf(getProperty(key));
    }

    @Override
    public void setProperty(String key, String value) {
        Property property = new Property(key, value);

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(property);
        transaction.commit();

        properties.put(key, value);
    }

    public void fetchInternationalSearchMask(String countryCode) {

        try {
            LOGGER.info("fetchInternationalSearchMask({}) - '{}'", countryCode, RegisteredService.EHEALTH_107.getUrn());
            String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH") + "forms" + System.getProperty("file.separator");
            try {

                KeyStore trustStore = KeyStore.getInstance("JKS");
                File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    trustStore.load(fileInputStream, ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD").toCharArray());

                    DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                            .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager().getProperty("SML_DOMAIN"), new DefaultDNSLookup()))
                            .reader(new DefaultBDXRReader(new DefaultSignatureValidator(trustStore)))
                            .build();

                    String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
                    LOGGER.info("Querying for participant identifier {}", participantIdentifierValue);
                    ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                            PARTICIPANT_IDENTIFIER_SCHEME);

                    LOGGER.info("Querying for service metadata");
                    ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, new DocumentIdentifier(RegisteredService.EHEALTH_107.getUrn(), DOCUMENT_IDENTIFIER_SCHEME));
                    //ServiceMetadata sm = smpClient.getServiceMetadata(smpClient, participantIdentifierValue, RegisteredService.EHEALTH_107.getUrn());

                    LOGGER.info("DocumentIdentifier: '{}' - '{}'",
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getScheme(),
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getDocumentIdentifier().getValue());

                    LOGGER.info("ParticipantIdentifier: '{}' - '{}'",
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getScheme(),
                            sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getParticipantIdentifier().getValue());

                    List<ProcessType> processTypes = sm.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess();
                    LOGGER.info("ProcessType: '{}' - '{}'", processTypes.toString(), processTypes.size());
                    if (!processTypes.isEmpty()) {
                        List<EndpointType> endpointTypes = processTypes.get(0).getServiceEndpointList().getEndpoint();
                        LOGGER.info("EndpointType: '{}' - '{}'", endpointTypes.toString(), endpointTypes.size());
                        if (!endpointTypes.isEmpty()) {
                            List<ExtensionType> extensionTypes = endpointTypes.get(0).getExtension();
                            LOGGER.info("ExtensionType: '{}' - '{}'", extensionTypes.toString(), extensionTypes.size());
                            if (!extensionTypes.isEmpty()) {
                                Document document = ((ElementNSImpl) extensionTypes.get(0).getAny()).getOwnerDocument();

                                DOMSource source = new DOMSource(document.getElementsByTagNameNS("http://ec.europa.eu/sante/ehncp/ism",
                                        "patientSearch").item(0));
                                TransformerFactory transformerFactory = TransformerFactory.newInstance();
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

    public void setServiceWSE(String ISOCountryCode, String ServiceName, String URL) {
        setProperty(ISOCountryCode + "." + ServiceName + ".WSE", URL);
    }

    private Optional<String> findProperty(String key) {
        String value = properties.get(key);
        if (value == null) {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Property property = session.get(Property.class, key);
            transaction.commit();

            if (property == null) {
                return Optional.empty();
            }
            value = property.getValue();
            properties.put(key, value);
        }
        return Optional.of(value);
    }
}
