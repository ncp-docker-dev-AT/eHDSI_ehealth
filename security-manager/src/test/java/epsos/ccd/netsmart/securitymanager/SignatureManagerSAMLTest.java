package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.junit.Assert.fail;

/**
 * @author jerouris
 */
public class SignatureManagerSAMLTest {

    private static final Logger logger = LoggerFactory.getLogger(SignatureManagerSAMLTest.class);

    public SignatureManagerSAMLTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        InitializationService.initialize();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of signSAMLAssertion method, of class SignatureManager.
     */
    @Ignore
    @Test
    public void testSignAndVerifySAMLAssertion() {
        try {
            logger.info("signSAMLAssertion");

            // Get parser pool manager
            BasicParserPool ppMgr = new BasicParserPool();
            ppMgr.setNamespaceAware(true);
            // Parse metadata file
            InputStream in = ClassLoader.getSystemResourceAsStream("SAMLIdAssertion.xml");
            Document samlas = ppMgr.parse(in);
            Element samlasRoot = samlas.getDocumentElement();
            // Get appropriate unmarshaller
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlasRoot);
            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            Assertion as = (Assertion) unmarshaller.unmarshall(samlasRoot);

            SignatureManager instance = new SignatureManager();

            instance.signSAMLAssertion(as);
            //MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
            // Get the Subject marshaller
            // Marshall the Subject

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc = dbf.newDocumentBuilder().newDocument();
            //Configuration.getMarshallerFactory().getMarshaller(as).marshall(as, signedDoc);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(as).marshall(as, signedDoc);
            try {
                XMLUtils.sendXMLtoStream(signedDoc, new FileOutputStream("SignedSamlAssertion.xml"));
            } catch (FileNotFoundException ex) {
                logger.error(null, ex);
            }

            //Verify the Signed SAML Assertion
            instance.verifySAMLAssertion(as);

            try {

                // It can also be verified using the DOM
                instance.verifyEnvelopedSignature(signedDoc);
            } catch (SMgrException e) {
                fail("test fail: " + e.getMessage());
            }

            Unmarshaller unmarshaller2 = unmarshallerFactory.getUnmarshaller(signedDoc.getDocumentElement());
            Assertion as2 = (Assertion) unmarshaller2.unmarshall(signedDoc.getDocumentElement());

            try {
                instance.verifySAMLAssertion(as2);
            } catch (SMgrException ex) {
                fail(ex.getMessage());

            }

        } catch (XMLParserException ex) {
            logger.error(null, ex);
            fail("XmL Parser:" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            logger.error(null, ex);
            fail("Parse Conf:" + ex.getMessage());
        } catch (MarshallingException ex) {
            logger.error(null, ex);
            fail("Marshalling:" + ex.getMessage());
        } catch (SMgrException ex) {
            logger.error(null, ex);
            fail("SMGR :" + ex.getMessage());
        } catch (UnmarshallingException ex) {
            logger.error(null, ex);
            fail("Unmarshalling:" + ex.getMessage());
        }
    }
}
