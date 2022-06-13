package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.NSTestKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.SPMSTestKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.TianiTestKeyStoreManager;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
@Ignore("Test to revise - Exclude unit test from test execution")
public class MockCertTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockCertTest.class);

    public MockCertTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        /*
        try {
            Properties databaseProps;
            databaseProps = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
            databaseProps.load(is);

            //* Add JDBC connection parameters to environment, instead of traditional JNDI
            final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            final ComboPooledDataSource ds = new ComboPooledDataSource();
            try {
                ds.setDriverClass(databaseProps.getProperty("db.driverclass"));
            } catch (PropertyVetoException ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
            ds.setJdbcUrl(databaseProps.getProperty("db.jdbcurl"));
            ds.setUser(databaseProps.getProperty("db.user"));
            ds.setPassword(databaseProps.getProperty("db.password"));
            ds.setMaxPoolSize(1);
            ds.setMaxPoolSize(15);
            ds.setAcquireIncrement(3);
            ds.setMaxStatementsPerConnection(100);
            ds.setNumHelperThreads(20);
            builder.bind(databaseProps.getProperty("db.resname"), ds);
            try {
                builder.activate();
            } catch (IllegalStateException ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            } catch (NamingException ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        */
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

    @Ignore
    @Test
    public void TianiCertTest() throws IOException {

        try {
            KeyStoreManager ksm = new TianiTestKeyStoreManager();
            X509Certificate cert = (X509Certificate) ksm.getCertificate("server1");
            boolean[] ku = cert.getKeyUsage();

            CertificateValidator cv = new CertificateValidator(ksm.getTrustStore());
            cv.validateCertificate(cert);
            assertNull(ku);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void SPMSCertTest() throws IOException {

        try {
            KeyStoreManager ksm = new SPMSTestKeyStoreManager();
            X509Certificate cert = (X509Certificate) ksm.getCertificate("ppt.ncp-signature.epsos.spms.pt");
            boolean[] ku = cert.getKeyUsage();

            CertificateValidator cv = new CertificateValidator(ksm.getTrustStore());
            cv.validateCertificate(cert);
            assertNull(ku);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void NSCertTest() throws IOException {

        try {
            KeyStoreManager ksm = new NSTestKeyStoreManager();
            X509Certificate cert = (X509Certificate) ksm.getCertificate("testncp");
            boolean[] ku = cert.getKeyUsage();
            if (ku == null) {
                fail("Key Usage not Present");
            }
            LOGGER.info("Key Usage: '{}'", ku);
            CertificateValidator cv = new CertificateValidator(ksm.getTrustStore());
            cv.validateCertificate(cert);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void NSCert2Test() throws IOException {

        try {
            KeyStoreManager ksm = new NSTestKeyStoreManager();
            X509Certificate cert = (X509Certificate) ksm.getCertificate("testncp2");
            boolean[] ku = cert.getKeyUsage();
            if (ku == null) {
                fail("Key Usage not Present");
            }
            LOGGER.info("Key Usage: '{}'", ku);
            CertificateValidator cv = new CertificateValidator(ksm.getTrustStore());
            cv.validateCertificate(cert);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void NSRevokedCertTest() throws IOException {

        try {
            KeyStoreManager ksm = new NSTestKeyStoreManager();
            X509Certificate cert = (X509Certificate) ksm.getCertificate("testrevokedncp");
            boolean[] ku = cert.getKeyUsage();
            if (ku == null) {
                fail("Key Usage not Present");
            }
            CertificateValidator cv = new CertificateValidator(ksm.getTrustStore());
            cv.validateCertificate(cert);

            LOGGER.info("Key Usage: '{}'", ku);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }
}
