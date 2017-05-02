package epsos.ccd.gnomon.configmanager;

import eu.epsos.util.net.ProxyCredentials;
import eu.epsos.util.net.ProxyUtil;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.Endpoint;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.w3c.dom.Document;

/**
 * This is the class which integrates the SMLSMP discovery client from the EU commission
 * (DG DIGIT) to be used by the Configuration Manager.
 *
 * @author max
 *
 */
public class SMLSMPClient {

	/** The logger. */
	private final static Logger L = LoggerFactory.getLogger(SMLSMPClient.class);

        /** Static constants for SMP identifiers */
        private static final String PARTICIPANT_IDENTIFIER_SCHEME = "ehealth-participantid-qns";
        private static final String PARTICIPANT_IDENTIFIER_VALUE = "urn:ehealth:%2s:ncpb-idp";
        private static final String DOCUMENT_IDENTIFIER_SCHEME = "ehealth-resid-qns";

	/** The certificate of the remote endpoint. */
	private X509Certificate certificate;

	/** The URL address of the remote endpoint. */
	private URL address;

        /** The XML contained in the Extension (used by search masks). */
        private Document extension;

	/**
	 * Lookup in the SMP, using the SML for a given country code and document type.
	 * And example query for, e.g., Portugal and XCPD is the following
	 * <pre>
	 * lookup("pt", "urn:ehealth:PatientIdentificationAndAuthentication::XCPD::CrossGatewayPatientDiscovery##ITI-55");
	 * </pre>
	 * then the methods {@link #getCertificate()} and {@link #getEndpointReference()} can be used.
	 * <b>Note</b> that this class is using a keystore to validate the signature of the SMP record, and
	 * it should use another one to validate the signature.
	 * <br/><br/>
	 * TODO: Make the keystores configurable.
	 * <br/><br/>
	 * @param countryCode The lowercase two-letter country code (ISO 3166-1 alpha-2)
	 * @param documentType The document type. Its format is given by the Document Identifiers defined in the CP-eHealthDSI-002 for SMP/SML capabilities.
	 * @throws SMLSMPClientException For any error (including not found)
	 */
	public void lookup(String countryCode, String documentType) throws SMLSMPClientException {
		try {
			KeyStore ks = this.loadTrustStore();

			DynamicDiscovery smpClient = this.createDynamicDiscoveryClient(ks);

                        String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
			ServiceMetadata sm = this.getServiceMetadata(smpClient, participantIdentifierValue, documentType);

			List<Endpoint> endpoints = sm.getEndpoints();

			/*
			 * Constraint: here I think I have just one endpoint
			 */
			int size = endpoints.size();
			if (size != 1) {
				throw new Exception("Invalid number of endpoints found ("+size+"). This implementation works just with 1.");
			}

			Endpoint e = endpoints.get(0);
			String address = e.getAddress();
			if (address==null) {
				throw new Exception("No address found for: " + documentType + ":" + participantIdentifierValue);
			}
			URL urlAddress = new URL(address);

			X509Certificate certificate = e.getCertificate();
			if (certificate == null) {
				throw new Exception("no certificate found for endpoint: " +e.getAddress());
			}

			setAddress(urlAddress);
			setCertificate(certificate);

		} catch (Exception e) {
			throw new SMLSMPClientException(e);
		}

	}

        /**
	 * Lookup for a search mask in the SMP, using the SML for a given country code and document type.
	 * This will be called directly by the Portal to fetch a search mask when it's not found.
         * And example query for, e.g., Portugal and ISM is the following
	 * <pre>
	 * lookup("pt", "urn:ehealth:ISM::InternationalSearchMask##ehealth-107");
	 * </pre>
	 * then the method {@link #getExtension()} can be used.
	 * <b>Note</b> that this class is using a keystore to validate the signature of the SMP record, and
	 * it should use another one to validate the signature.
	 * <br/><br/>
	 * TODO: Make the keystores configurable.
	 * <br/><br/>
	 * @param countryCode The lowercase two-letter country code (ISO 3166-1 alpha-2)
	 * @param documentType The document type. Its format is given by the Document Identifiers defined in the CP-eHealthDSI-002 for SMP/SML capabilities.
	 * @throws SMLSMPClientException For any error (including not found)
	 */
        public void fetchSearchMask(String countryCode, String documentType) throws SMLSMPClientException {
            try {
                KeyStore ks = this.loadTrustStore();

                DynamicDiscovery smpClient = this.createDynamicDiscoveryClient(ks);

                String participantIdentifierValue = String.format(PARTICIPANT_IDENTIFIER_VALUE, countryCode);
                ServiceMetadata sm = this.getServiceMetadata(smpClient, participantIdentifierValue, documentType);

                List<Endpoint> endpoints = sm.getEndpoints();
                /*
                * Constraint: here I think I have just one endpoint
                */
               int size = endpoints.size();
               if (size != 1) {
                       throw new Exception("Invalid number of endpoints found ("+size+"). This implementation works just with 1.");
               }
               Endpoint e = endpoints.get(0);

               // TODO: when DDC 1.3 is released: obtain the Extension of the Endpoint and read the search mask into the Document

            } catch (Exception e) {
                    throw new SMLSMPClientException(e);
            }
        }

	/**
	 * Set the certificate of the newly discovered endpoint.
	 * @param certificate The certificate
	 */
	private synchronized void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;

	}

	/**
	 * Set the address of the newly discovered endpoint.
	 * @param address the address as URL
	 */
	private synchronized void setAddress(URL address) {
		this.address = address;
	}

	/**
	 * After calling the {@link #lookup(String, String)}, this method returns the certificate
	 * @return the certificate of the newly discovered endpoint
	 */
	public X509Certificate getCertificate() {
		return this.certificate;
	}

	/**
	 * After calling the {@link #lookup(String, String)}, this method returns the url
	 * @return the url of the newly discovered endpoint
	 */
	public URL getEndpointReference() {
		return this.address;
	}

        /**
	 * Set the extension of the newly discovered resource (e.g., search masks).
	 * @param extension the XML extension as a Document
	 */
        private synchronized void setExtension(Document extension) {
            this.extension = extension;
        }

        /**
         * After calling the {@link #lookup(String, String)}, this method returns the XML of the extension
         * @return the XML of the extension
         */
        public Document getExtension() {
            return this.extension;
        }

        /**
         * Loads the truststore containing the certificate used by the SMP server
         * to sign SMP files, to be used when building the Dynamic Discovery Client.
         * @return The truststore containing the certificate used by the SMP server to sign SMP files
         * @throws KeyStoreException
         * @throws IOException
         * @throws NoSuchAlgorithmException
         * @throws CertificateException
         */
        private KeyStore loadTrustStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            ks.load(new FileInputStream(ConfigurationManagerSMP.getInstance().getProperty("TRUSTSTORE_PATH")),
                    ConfigurationManagerSMP.getInstance().getProperty("TRUSTSTORE_PASSWORD").toCharArray());
            return ks;
        }

        /**
         * Creates an instance of the Dynamic Discovery Client with a custom validator for validating
         * both the SMP server signature and the eHealth specific national authority signature of the
         * SMP file. OpenNCP proxy settings are also taken into account.
         * @param ks The truststore containing the certificate used by SMP server to sign SMP files.
         * @return The Dynamic Discovery Client ready to use.
         * @throws TechnicalException
         */
        private DynamicDiscovery createDynamicDiscoveryClient(KeyStore ks) throws TechnicalException {
            L.debug("Instantiating the smpClient.");
            L.warn("TODO: make configurable the keystore");

            // Instantiate the DIGIT client using our customized signature validator. This is due to
            // the fact that we need to evaluate our signature as well. We also instantiate proxy credentials
            // in case they're needed.
            DynamicDiscovery smpClient = null;
            ProxyCredentials proxyCredentials = null;
            if (ProxyUtil.isProxyAnthenticationMandatory()) {
                proxyCredentials = ProxyUtil.getProxyCredentials();
            }
            if (proxyCredentials != null) {
                smpClient = DynamicDiscoveryBuilder.newInstance()
                        .locator(new DefaultBDXRLocator(ConfigurationManagerService.getInstance().getProperty("SML_DOMAIN")))
                        .fetcher(new DefaultURLFetcher(new CustomProxy(proxyCredentials.getProxyHost(), Integer.parseInt(proxyCredentials.getProxyPort()), proxyCredentials.getProxyUser(), proxyCredentials.getProxyPassword())))
                        .reader(new CustomizedBDXRReader(new CustomizedSignatureValidator(ks))).build();
            } else {
                smpClient = DynamicDiscoveryBuilder.newInstance()
                        .locator(new DefaultBDXRLocator(ConfigurationManagerService.getInstance().getProperty("SML_DOMAIN")))
                        .reader(new CustomizedBDXRReader(new CustomizedSignatureValidator(ks))).build();
            }
            return smpClient;
        }

        /**
         * Fetches the ServiceMetadata from the SMP server based on the
         * combination of the country code and document identifier.
         * @param smpClient The Dynamic Discovery Client ready to be used
         * @param participantIdentifierValue The SMP Participant Identifier
         * @param documentType The SMP Document Identifier
         * @return The ServiceMetadata for the requested ParticipantIdentifier and Document Identifier
         * @throws TechnicalException
         */
        private ServiceMetadata getServiceMetadata(DynamicDiscovery smpClient, String participantIdentifierValue, String documentType) throws TechnicalException {
            L.debug("Querying for participant identifier " + participantIdentifierValue);
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantIdentifierValue,
                            PARTICIPANT_IDENTIFIER_SCHEME);

            L.debug("Querying for service metadata");
            ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier,
                            new DocumentIdentifier(documentType, DOCUMENT_IDENTIFIER_SCHEME));
            return sm;
        }

}
