package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import epsos.ccd.gnomon.configmanager.SMLSMPClient;
import epsos.ccd.gnomon.configmanager.SMLSMPClientException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.util.Assert;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

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
    public void setProperty(String key, String value) {
        Property property = new Property(key, value);

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(property);
        transaction.commit();

        properties.put(key, value);
    }

    /**
     * @param countryCode
     * @param service
     * @return
     */
    private String getEndpointUrl(String countryCode, RegisteredService service) {

        return getEndpointUrl(countryCode, service, false);
    }

    //@Override
    private String getEndpointUrl(String countryCode, RegisteredService service, boolean refresh) {

        Assert.notNull(countryCode, "countryCode must not be null!");
        Assert.notNull(service, "service must not be null!");
        LOGGER.info("getEndpointUrl('{}', '{}')", countryCode, service.getServiceName());
        String key = countryCode.toLowerCase() + "." + service.getServiceName() + ".WSE";
        if (!refresh) {
            Optional<String> endpoint = findProperty(key);
            if (endpoint.isPresent()) {
                return endpoint.get();
            }
        }

        SMLSMPClient client = new SMLSMPClient();

        try {
            client.lookup(countryCode, service.getUrn());
            URL endpointUrl = client.getEndpointReference();
            if (endpointUrl == null) {
                throw new PropertyNotFoundException("Property '" + key + "' not found!");
            }

            String value = endpointUrl.toExternalForm();
            setProperty(key, value);

            X509Certificate certificate = client.getCertificate();
            if (certificate != null) {
                String endpointId = countryCode.toLowerCase() + "_" + StringUtils.substringAfter(service.getUrn(), "##");
                storeEndpointCertificate(endpointId, certificate);
            }
            return value;
        } catch (SMLSMPClientException e) {
            throw new ConfigurationManagerException("An internal error occurred while retrieving the endpoint URL", e);
        }
    }

    public void fetchInternationalSearchMask(String countryCode) {

        try {
            LOGGER.info("fetchInternationalSearchMask({}) - '{}'", countryCode, RegisteredService.EHEALTH_107.getUrn());
            SMLSMPClient client = new SMLSMPClient();

            client.fetchSearchMask(StringUtils.lowerCase(countryCode), RegisteredService.EHEALTH_107.getUrn());
        } catch (SMLSMPClientException e) {
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

    private void storeEndpointCertificate(String endpointId, X509Certificate certificate) {

        // Store the endpoint certificate in the truststore
        String trustStorePath = getProperty(StandardProperties.NCP_TRUSTSTORE);
        char[] trustStorePassword = getProperty(StandardProperties.NCP_TRUSTSTORE_PASSWORD).toCharArray();

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
        File certificateFile = new File(getProperty(StandardProperties.NCP_CERTIFICATES_DIRECTORY), endpointId + ".der");
        try (OutputStream os = new FileOutputStream(certificateFile)) {
            os.write(certificate.getEncoded());

        } catch (CertificateException | IOException e) {
            throw new ConfigurationManagerException("An error occurred while storing the endpoint certificate in the file system!", e);
        }
    }
}
