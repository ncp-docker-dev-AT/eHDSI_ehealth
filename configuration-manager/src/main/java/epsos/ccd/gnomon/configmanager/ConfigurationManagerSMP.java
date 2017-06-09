package epsos.ccd.gnomon.configmanager;

import eu.epsos.configmanager.database.HibernateUtil;
import eu.epsos.configmanager.database.model.Property;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

/**
 * ConfigurationManagerSMP. Obtain a configuration value. Usage: <br/>
 * <p>
 * <pre>
 * ConfigurationManagerSMP.getInstance().getProperty(sampleKey);
 * </pre>
 * <p>
 * This class uses <a href=
 * "http://docs.oasis-open.org/bdxr/bdx-smp/v1.0/cs01/bdx-smp-v1.0-cs01.html">BDXR-SMP</a>
 * and <a href=
 * "http://docs.oasis-open.org/bdxr/BDX-Location/v1.0/BDX-Location-v1.0.html">BDX-Location</a>
 * standards to obtain the configuration of remote NCPs. A detailed discussion
 * on the implementation strategy of this class can be found <a href=
 * "https://openncp.atlassian.net/wiki/display/ncp/Cache+implementation+through+ConfigurationManager+refactoring">here</a>.
 * <br/>
 * The flow is as follows.
 * <ol>
 * <li>During the startup, the property database is read and added into an
 * HashMap</li>
 * <li>When a third party needs to obtain a key, it calls the
 * {@link #getProperty(String)}. The property is obtained as
 * <ul>
 * <li>from the hashmap, and if not present</li>
 * <li>from the SMP, through a Query, and if not present</li>
 * <li>Run TSLSynchronizer, and if not present</li>
 * <li>Throw error</li>
 * </ul>
 * <li>When there is a system failure of a numerable set of exceptions (e.g.,
 * SSLPeerUnverified), the SMP query is performed</li>
 * </ol>
 * One point of intervention would be to add a TTL to a value in the hashmap.
 *
 * @author massimiliano.masi@bmg.gv.at
 */
public final class ConfigurationManagerSMP implements ConfigurationManagerInt {

    /**
     * This is the logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerSMP.class);

    /**
     * A map from OpenNCP Services/IHE Transactions to SMP DocumentIdentifiers.
     */
    private static final HashMap<String, String> mapMap = new HashMap<>();
    /**
     * This class is a singleton, this is the instance.
     */
    private static volatile ConfigurationManagerSMP instance;

    // These are the services that OpenNCP currently defines.
    // The keys come from the OpenNCP custom implementation of the deprecated D3.4.2 - 4.5.8.1 and IHE profiles
    // (e.g., same endpoint for XCA Query and Retrieve.
    static {
        // TODO: ideally this should be shared with SMP-Editor
        mapMap.put("PatientIdentificationService", "urn:ehealth:PatientIdentificationAndAuthentication::XCPD::CrossGatewayPatientDiscovery##ITI-55");
        mapMap.put("PatientService", "urn:ehealth:RequestOfData::XCA::CrossGatewayQuery##ITI-38");
        mapMap.put("OrderService", "urn:ehealth:RequestOfData::XCA::CrossGatewayQuery##ITI-38");
        mapMap.put("DispensationService", "urn:ehealth:ProvisioningOfData:Provide::XDR::ProvideandRegisterDocumentSet-b##ITI-41");
        mapMap.put("ConsentService", "urn:ehealth:ProvisioningOfData:BPPC-RegisterUpdate::XDR::ProvideandRegisterDocumentSet-b##ITI-41");
        // These are services both foreseen in the specification but not currently configurable.
        mapMap.put("ITI-63", "urn:ehealth:RequestOfData::XCF::CrossGatewayFetchRequest##ITI-63");
        mapMap.put("ITI-39", "urn:ehealth:RequestOfData::XCA::CrossGatewayRetrieve##ITI-39");
        mapMap.put("epsos-91", "urn:ehealth:CountryBIdentityProvider::identityProvider::HPAuthentication##epsos-91");
        mapMap.put("ITI-40", "urn:ehealth:CountryBIdentityProvider::XUA::ProvideX-UserAssertion##ITI-40");
        mapMap.put("ehealth-105", "urn:ehealth:VPN::VPNGatewayServer##ehealth-105");
        mapMap.put("ehealth-106", "urn:ehealth:VPN::VPNGatewayClient##ehealth-106");
        mapMap.put("ehealth-107", "urn:ehealth:ISM::InternationalSearchMask##ehealth-107");
    }

    /**
     * The hibernate session. Here I may have problems of thread safety.
     */
    private org.hibernate.classic.Session session;
    /**
     * This is the Hash Map that holds the configuration entries.
     */
    private HashMap<String, PropertySearchableContainer> configuration = new HashMap<>();

    /**
     * Constructor. Here I have to load all the properties from the database.
     */
    private ConfigurationManagerSMP() {

        long start = System.currentTimeMillis();
        LOGGER.info("Loading the Hibernate session object");
        session = HibernateUtil.getSessionFactory().openSession();
        long end = System.currentTimeMillis();
        long total = end - start;
        LOGGER.info("Loaded took: '{}'ms", total);

        populate();
        LOGGER.info("Constructor ends");
    }

    /**
     * Get an instance of the ConfigurationManagerSMP.
     *
     * @return an instance of the ConfigurationManagerSMP class.
     */
    public static ConfigurationManagerSMP getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManagerSMP.class) {
                if (instance == null) {
                    LOGGER.info("Instatiating a new ConfigurationManagerSMP");
                    instance = new ConfigurationManagerSMP();
                }
            }
        }
        return instance;
    }

    /**
     * Adds the values from the DB to the memory.
     */
    private void populate() {
        LOGGER.info("Loading all the values");
        long start = System.currentTimeMillis();

        @SuppressWarnings("unchecked")
        List<Property> properties = session.createCriteria(Property.class).list();

        long end = System.currentTimeMillis();
        long total = end - start;
        LOGGER.info("Getting all the properties took: '{}'ms", total);
        int size = properties.size();
        LOGGER.info("Adding '{}' properties into the hashmap", size);

        start = System.currentTimeMillis();

        for (Property property : properties) {
            String name = property.getName();
            String value = property.getValue();
            PropertySearchableContainer psc = new PropertySearchableContainer();
            psc.setValue(value);
            // psc.setSearchable(property.isSMP());
            configuration.put(name, psc);
            LOGGER.debug("Added the couple (name, value) - '{}':'{}'", name, value);
        }
        end = System.currentTimeMillis();
        total = end - start;
        LOGGER.info("Loading in memory the full database took: '{}'ms", total);
    }

    public void updateCache(String key,String value) {
        if (value != null) {
            PropertySearchableContainer psc1 = new PropertySearchableContainer();
            psc1.setSearchable(true);
            psc1.setValue(value);
            configuration.put(key, psc1);
        }
    }

    public String queryProperty(String key) {
        LOGGER.debug("Searching for '{}'", key);
        String value = query(key);
        if (value != null) {
            PropertySearchableContainer psc = new PropertySearchableContainer();
            psc.setSearchable(true);
            psc.setValue(value);
            configuration.put(key, psc);
            return psc.getValue();
        }
        return null;
    }

    /**
     * Obtain a property configuration property.
     *
     * @param key The key to search.
     * @return The given property, if found
     * @throws PropertyNotFoundException if the property can't be found either in the hashmap, SMP, or
     *                                   after TSLSynchronizer
     */
    public String getProperty(String key) {
        LOGGER.debug("Searching for '{}'", key);
        LOGGER.debug("Trying hashmap first");

        PropertySearchableContainer psc = configuration.get(key);

        // Ok, here two things: one is that the entry does not exist, the second
        // is that it is not
        // Searchable. So, if it does not exist, we try SMP anyway. If it
        // exists, then we use it.
        // To update we remove first and we re-add it.
        if (psc == null) {
            LOGGER.debug("Nothing found in the hashmap, let's try to SMP");
            String value = query(key);
            if (value != null) {
                PropertySearchableContainer psc1 = new PropertySearchableContainer();
                psc1.setSearchable(true);
                psc1.setValue(value);
                configuration.put(key, psc1);
                psc = psc1;
            }
        }
        if (psc == null) {
            LOGGER.info("Value is still null, let's run TSLSynchronizer");
            // TODO: Launch the TSL Sync process?
            // TSLSynchronizer.sync();
        }
        if (psc != null) {
            LOGGER.info("Returning the value: " + psc.getValue());
            return psc.getValue();
        }

        // TODO: Which value to return?
        return null;
    }

    /**
     * What query will do. Check firstly if the value is one which is related to
     * SMP (e.g., it is a value available in the ServiceMetadata). If not,
     * return null. If yes, then do pack the record and do a DNS query to
     * discover which SMP (e.g., use the SML). If the SML returns no record,
     * return null. Else, obtain the service metadata, verify the signature (the
     * Trust must be established by means of the eIDAS). <br/>
     * If the trust is ok, obtain all the values, store all of them in the
     * database and in the hashmap, and return the one requested.
     *
     * @param key The key to be searched in the SMP
     * @return the value if known, null in all the other cases
     */
    private String query(String key) {

		/*
         * Participant identifier is: urn:ehealth:lu:ncpb-idp document
		 * identifier is relatd to the transaction
		 * epsos-resid-qns::urn:ehealth:PatientIdentificationAndAuthentication::XCPD::CrossGatewayPatientDiscovery##ITI-55.
		 * 
		 * How the participant identifier is calculated: we split the string in
		 * three (must be three). The first is the country, the second is the to
		 * be mapped into the transaction
		 */

        String[] values = key.split("\\.");
        if (values == null || values.length != 3) {
            throw new RuntimeException("The key to be selected in SMP has a length which is not allowed");
        }

        String countryCode = values[0];
        LOGGER.debug("Found country code: '{}'", countryCode);
        String documentType = mapMap.get(values[1]);
        LOGGER.debug("Found documentType: '{}'", documentType);
        SMLSMPClient client = new SMLSMPClient();
        try {
            LOGGER.debug("Doing SML/SMP");
            client.lookup(countryCode, documentType);
            LOGGER.debug("Found values!!!!");
            /*
             * What to do with the property? One is to return to the caller the
			 * endpoint, the second is to put it into the certificate
			 */
            X509Certificate cert = client.getCertificate();
            if (cert != null) {
                LOGGER.debug("Storing the certificate in the truststore, configuration/DB and folder");
                String subject = cert.getSubjectDN().getName();
                String alias = Base64.encodeBase64String(MessageDigest.getInstance("MD5").digest(subject.getBytes()));
                storeCertificateToTrustStore(cert, alias);
                String eventId = documentType.substring(documentType.lastIndexOf("##") + 2); // "e.g., obtain "ITI-55"
                storeCertificateInConfigurationAndDB(cert, countryCode, eventId);
                storeCertificateInCertsFolder(cert, countryCode, eventId);
            }
            URL endpoint = client.getEndpointReference();
            LOGGER.debug("Found endpoint: '{}'", endpoint);
            if (endpoint != null) {
                LOGGER.debug("Storing the new endpoint for '{}' in DB", key);
                updateProperty(key, endpoint.toString());
                return endpoint.toString();
            } else {
                return null;
            }
        } catch (SMLSMPClientException e) {
            LOGGER.error("SMP/SML Exception", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Invalid exception in message digest", e);
            return null;
        }
    }

    /**
     * Stores the fetched certificate in the OpenNCP truststore.
     *
     * @param cert  The certificate to be stored
     * @param alias The alias to assign to the certificate
     * @throws SMLSMPClientException
     */
    private void storeCertificateToTrustStore(X509Certificate cert, String alias) throws SMLSMPClientException {

        ConfigurationManagerService cms = ConfigurationManagerService.getInstance();
        String TRUST_STORE = cms.getProperty("TRUSTSTORE_PATH");
        String TRUST_STORE_PASS = cms.getProperty("TRUSTSTORE_PASSWORD");
        LOGGER.debug("Storing in truststore: '{}'", TRUST_STORE);
        LOGGER.debug("Storing the certificate with DN: '{}' and SN: '{}'", cert.getSubjectDN(), cert.getSerialNumber());

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            File keystoreFile = new File(TRUST_STORE);
            // Load the keystore contents
            try (FileInputStream in = new FileInputStream(keystoreFile)) {
                keystore.load(in, TRUST_STORE_PASS.toCharArray());
            }

            keystore.setCertificateEntry(alias, cert);
            LOGGER.debug("CERTALIAS: '{}'", alias);
            // Save the new keystore contents
            try (FileOutputStream out = new FileOutputStream(keystoreFile)) {
                keystore.store(out, TRUST_STORE_PASS.toCharArray());
            }

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error("Unable to store the message in the truststore", e);
            throw new SMLSMPClientException(e);
        }
    }

    /**
     * Stores the certificate fetched from the SMP record into the folder given
     * by the certificates.storepath property. E.g., for the certificate used
     * to protect the Portuguese XCPD (ITI-55) endpoint, it stores the certificate
     * under the filename pt_ITI-55.der.
     *
     * @param certificate The certificate to store
     * @param countryCode The lowercase two-letter country code (ISO 3166-1 alpha-2)
     * @param eventId     The suffix of the SMP Document Identifier, corresponding
     *                    to the epSOS EventIDs from D3.A.7 AuditTrail - 2.3.5.7, plus some custom
     *                    ones, both outlined in the CP-eHealthDSI-002 for SMP/SML capabilities.
     * @throws SMLSMPClientException
     */
    private void storeCertificateInCertsFolder(X509Certificate certificate, String countryCode, String eventId) throws SMLSMPClientException {
        // export the certificate to der format
        String storepath = configuration.get("certificates.storepath").getValue();
        String filename = countryCode + "_" + eventId;
        boolean exp = exportCertificate(certificate, new File(storepath + filename + ".der"), true);
        if (exp) {
            LOGGER.info("Certificate '{}'.der exported successfully", filename);
        } else {
            LOGGER.info("Failed to export the certificate '{}'.der", filename);
        }
    }

    /**
     * Stores the certificate fetched from the SMP record into the hashmap and
     * also in the properties DB. E.g., for the certificate used to protect the
     * Portuguese XCPD (ITI-55) endpoint, it stores the certificate
     * under the key pt_ITI-55.
     *
     * @param certificate The certificate to store
     * @param countryCode The lowercase two-letter country code (ISO 3166-1 alpha-2)
     * @param eventId     The suffix of the SMP Document Identifier, corresponding
     *                    to the epSOS EventIDs from D3.A.7 AuditTrail - 2.3.5.7, plus some custom
     *                    ones, both outlined in the CP-eHealthDSI-002 for SMP/SML capabilities.
     */
    private void storeCertificateInConfigurationAndDB(X509Certificate certificate, String countryCode, String eventId) {
        // create a keypair value with certid and country code and update both the hashmap and DB
        configuration.put(certificate.getSerialNumber().toString(), new PropertySearchableContainer(countryCode + "_" + eventId, false));
        updateProperty(certificate.getSerialNumber().toString(), countryCode + "_" + eventId);
    }

    /**
     * This method exports a certificate either to text (pem format), either to
     * binary (der format)
     *
     * @param cert
     * @param file
     * @param binary
     */
    private boolean exportCertificate(java.security.cert.Certificate cert, File file, boolean binary) throws SMLSMPClientException {

        boolean exp = false;
        try {
            // Get the encoded form which is suitable for exporting
            byte[] buf = cert.getEncoded();
            try (FileOutputStream os = new FileOutputStream(file)) {
                if (binary) { // Write in binary form
                    os.write(buf);
                } else { // Write in text form
                    Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                    wr.write("-----BEGIN CERTIFICATE-----\n");
                    wr.write(new String(Base64.encodeBase64(buf)));
                    wr.write("\n-----END CERTIFICATE-----\n");
                    wr.flush();
                }
                //exp = true;
            }
        } catch (CertificateEncodingException | IOException e) {
            exp = false;
            throw new SMLSMPClientException(e);
        }
        return true;
    }

    /**
     * Get the endpoint URL for a specified country and a service name
     *
     * @param ISOCountryCode the iso country code
     * @param ServiceName    the service name
     * @return
     */
    public String getServiceWSE(String ISOCountryCode, String ServiceName) {
        return getProperty(ISOCountryCode + "." + ServiceName + ".WSE");
    }

    @Override
    public void setServiceWSE(String ISOCountryCode, String ServiceName, String URL) {
        throw new IllegalArgumentException("Unable to set WSE: wrong concept in SMP");
    }

    /**
     * This method persists the updated property.
     */
    @Override
    public String updateProperty(String key, String value) {
        OLDConfigurationManagerDb.getInstance().updateProperty(key, value);
        return value;
    }

    /**
     * Removes a key from the hashmap.
     *
     * @param key The key to delete.
     */
    public void deleteKeyFromHashMap(String key) {
        LOGGER.debug("Going to remove from the hashmap the following key: '{}'", key);
        configuration.remove(key);
    }
}
