package epsos.ccd.netsmart.securitymanager.sts.client;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.*;
import org.junit.runner.RunWith;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.fail;

@RunWith(JMockit.class)
public class TRCAssertionRequestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TRCAssertionRequestTest.class);

    @Mocked
    private TRCAssertionRequest req;

    @Mocked
    private Marshaller marshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {

        try {
            Properties databaseProps;
            databaseProps = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
            databaseProps.load(is);

            /* Add JDBC connection parameters to environment, instead of traditional JNDI */
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
            } catch (IllegalStateException | NamingException ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.trustStore", "C:/Users/Jerouris/testKeystore.jks");
        systemProps.put("javax.net.ssl.keyStore", "C:/Users/Jerouris/testKeystore.jks");
        systemProps.put("javax.net.ssl.trustStorePassword", "tomcat");
        systemProps.put("javax.net.ssl.keyStorePassword", "tomcat");
        //  systemProps.put("javax.net.debug", "SSL,handshake");
        //  systemProps.put("java.security.debug", "all");

        System.setProperties(systemProps);
        //for localhost testing only

        DefaultBootstrap.bootstrap();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of request method, of class TRCAssertionRequest.
     */
    @Test
    public void testMakeRequestSSL() throws Exception {

        new NonStrictExpectations() {{
            req.request();
            returns(any);
        }};

        try {
            //makeRequest - with SSL
            Assertion idAs = loadSamlAssertionAsResource("SampleIdAssertion.xml");

            TRCAssertionRequest req = new TRCAssertionRequest.Builder(idAs, "anId")
                    .PurposeOfUse("TREATMENT")
                    .build();

            Assertion trc = req.request();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc = dbf.newDocumentBuilder().newDocument();

            marshaller.marshall(trc, signedDoc);

            try {
                XMLUtils.sendXMLtoStream(signedDoc, new FileOutputStream("SignedTRCAssertion.xml"));
            } catch (FileNotFoundException ex) {
                LOGGER.error(null, ex);
            }
        } catch (Exception ex) {
            LOGGER.error(null, ex);
            fail("Could not make the request: " + ex.getMessage());
        }
    }

    @Test
    public void testMakeRequestPlainConnection() {
        try {
            // makeRequest - No SSL
            Assertion idAs = loadSamlAssertionAsResource("SampleIdAssertion.xml");

            TRCAssertionRequest req = new TRCAssertionRequest.Builder(idAs, "TestId2")
                    .STSLocation("http://localhost:8080/TRC-STS/SecurityTokenService")
                    .PurposeOfUse("EMERGENCY")
                    .build();

            Assertion trc = req.request();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc = dbf.newDocumentBuilder().newDocument();

            marshaller.marshall(trc, signedDoc);
            try {
                XMLUtils.sendXMLtoStream(signedDoc, new FileOutputStream("SignedTRCAssertion.xml"));
            } catch (FileNotFoundException ex) {
                LOGGER.error(null, ex);
            }

        } catch (Exception ex) {
            LOGGER.error(null, ex);
            fail("Could not make the request: " + ex.getMessage());
        }
    }

    private Assertion loadSamlAssertionAsResource(String filename) {
        Assertion hcpIdentityAssertion = null;
        try {
            BasicParserPool ppMgr = new BasicParserPool();
            ppMgr.setNamespaceAware(true);
            // Parse metadata file
            InputStream in = ClassLoader.getSystemResourceAsStream(filename);
            Document samlas = ppMgr.parse(in);
            Element samlasRoot = samlas.getDocumentElement();
            // Get apropriate unmarshaller
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlasRoot);
            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            hcpIdentityAssertion = (Assertion) unmarshaller.unmarshall(samlasRoot);
        } catch (UnmarshallingException | XMLParserException ex) {
            LOGGER.error(null, ex);
        }
        return hcpIdentityAssertion;
    }
}
