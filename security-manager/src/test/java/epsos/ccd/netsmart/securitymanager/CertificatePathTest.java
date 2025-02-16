package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.NSTestKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.TianiTestKeyStoreManager;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class CertificatePathTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificatePathTest.class);

    public CertificatePathTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void TianiCertPathTest() {
        try {

            KeyStoreManager ksm = new TianiTestKeyStoreManager();

            // instantiate a KeyStore with type JKS
            KeyStore ks = ksm.getKeyStore();

            Certificate cert = ks.getCertificate("server1");
            X509CertSelector target = new X509CertSelector();
            target.setCertificate((X509Certificate) cert);
            LOGGER.info("Certificate: '{}'", cert);
            PKIXBuilderParameters builderParams = new PKIXBuilderParameters(
                    ksm.getTrustStore(), target);
            builderParams.setRevocationEnabled(false);

            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

            CertPathBuilderResult build = builder.build(builderParams);

            CertPath cp = build.getCertPath();

            List<? extends Certificate> certs = cp.getCertificates();
            LOGGER.info("--------------------------- Certificates as built ----------------------------");
            for (Certificate crt : certs) {
                LOGGER.info("Certificate: '{}'", crt);
            }

            LOGGER.info("--------------------------- END ----------------------------------------------");

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(ksm.getTrustStore());
            params.setRevocationEnabled(false);

            PKIXCertPathValidatorResult validationResult = (PKIXCertPathValidatorResult) cpv
                    .validate(cp, params);
            LOGGER.info("PKIXCertPathValidatorResult: '{}'", validationResult);

        } catch (CertPathBuilderException | KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertPathValidatorException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void NSCertPathTest() {
        try {

            KeyStoreManager ksm2 = new NSTestKeyStoreManager();

            // instantiate a KeyStore with type JKS
            KeyStore ks = ksm2.getKeyStore();
            Certificate[] certArray = ks.getCertificateChain("testncp");
            // convert chain to a List
            List<Certificate> certList = Arrays.asList(certArray);

            // instantiate a CertificateFactory for X.509
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // extract the certification path from
            // the List of Certificates
            CertPath cp = cf.generateCertPath(certList);
            X509CRL crl = (X509CRL) cf.generateCRL(ClassLoader
                    .getSystemResourceAsStream("keystores/crl/NSTestCRL.crl"));

            // print each certificate in the path
            List<? extends Certificate> certs = cp.getCertificates();
            for (Certificate cert : certs) {
                LOGGER.info("Certificate: '{}'", cert);
            }

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(ksm2.getTrustStore());
            params.setRevocationEnabled(true);
            params.addCertStore(CertStore.getInstance(
                    "Collection",
                    new CollectionCertStoreParameters(Collections
                            .singletonList(crl))));

            PKIXCertPathValidatorResult validationResult = (PKIXCertPathValidatorResult) cpv
                    .validate(cp, params);
            LOGGER.info("PKIXCertPathValidatorResult: '{}'", validationResult);

        } catch (CRLException ex) {
            LOGGER.error(null, ex);
        } catch (CertPathValidatorException | KeyStoreException | CertificateException | NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void NSRevokedCertPathTest() {
        try {

            KeyStoreManager ksm2 = new NSTestKeyStoreManager();

            // instantiate a KeyStore with type JKS
            KeyStore ks = ksm2.getKeyStore();
            Certificate[] certArray = ks.getCertificateChain("testrevokedncp");
            // convert chain to a List
            List<Certificate> certList = Arrays.asList(certArray);

            // instantiate a CertificateFactory for X.509
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // extract the certification path from
            // the List of Certificates
            CertPath cp = cf.generateCertPath(certList);
            X509CRL crl = (X509CRL) cf.generateCRL(ClassLoader
                    .getSystemResourceAsStream("keystores/crl/NSTestCRL.crl"));

            // print each certificate in the path
            List<? extends Certificate> certs = cp.getCertificates();
            for (Certificate cert : certs) {
                LOGGER.info("Certificate: '{}'", cert);
            }

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(ksm2.getTrustStore());
            params.setRevocationEnabled(true);
            params.addCertStore(CertStore.getInstance(
                    "Collection",
                    new CollectionCertStoreParameters(Collections
                            .singletonList(crl))));

            PKIXCertPathValidatorResult validationResult = (PKIXCertPathValidatorResult) cpv
                    .validate(cp, params);
            LOGGER.info("PKIXCertPathValidatorResult: '{}'", validationResult);

        } catch (CRLException ex) {
            LOGGER.error(null, ex);
        } catch (CertPathValidatorException ex) {
            LOGGER.error(null, ex);
            assertEquals("Certificate has been revoked, reason: unspecified",
                    ex.getMessage());
        } catch (InvalidAlgorithmParameterException | KeyStoreException | CertificateException | NoSuchAlgorithmException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }
}
