package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.*;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class SamlTRCIssuerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlTRCIssuerTest.class);

    public SamlTRCIssuerTest() {
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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of issueTrcToken method, of class SamlTRCIssuer.
     */
    @Ignore
    @Test
    public void testIssueTrcToken() {

        try {
            LOGGER.info("issueTrcToken");
            // Get parser pool manager
            BasicParserPool ppMgr = new BasicParserPool();
            ppMgr.setNamespaceAware(true);
            // Parse metadata file
            InputStream in = ClassLoader.getSystemResourceAsStream("SignedSamlAssertion.xml");
            //InputStream in = ClassLoader.getSystemResourceAsStream("SAMLSignedIdentityAssertion.xml");
            Document samlas = ppMgr.parse(in);
            Element samlasRoot = samlas.getDocumentElement();
            // Get appropriate unmarshaller
            //UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlasRoot);
            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            Assertion hcpIdentityAssertion = (Assertion) unmarshaller.unmarshall(samlasRoot);
            LOGGER.info("Name Id Value:{0}", hcpIdentityAssertion.getSubject().getNameID().getValue());
            String patientID = "theID";
            List<Attribute> attrValuePair = null;
            SamlTRCIssuer instance = new SamlTRCIssuer();

            Assertion result = instance.issueTrcToken(hcpIdentityAssertion, patientID, null, attrValuePair);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc = dbf.newDocumentBuilder().newDocument();
            //Configuration.getMarshallerFactory().getMarshaller(result).marshall(result, signedDoc);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(result).marshall(result, signedDoc);

            XMLUtils.sendXMLtoStream(signedDoc, new FileOutputStream("trc.xml"));

        } catch (FileNotFoundException | MarshallingException ex) {
            LOGGER.error(null, ex);
        } catch (UnmarshallingException | XMLParserException | SMgrException ex) {
            fail(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of verifyTrcToken method, of class SamlTRCIssuer.
     *
     * @throws java.lang.Exception
     */
    @Ignore
    @Test
    public void testVerifyTrcToken() throws Exception {

        LOGGER.info("verifyTrcToken");
        // Get parser pool manager
        Assertion idas = loadSamlAssertionAsResource("SignedSamlAssertion.xml");
        Assertion trc = null;
        String patientID = "theID";
        List<Attribute> attrValuePair = null;
        SamlTRCIssuer instance = new SamlTRCIssuer();
        SignatureManager sm = new SignatureManager();

        try {

            sm.verifySAMLAssertion(idas);
        } catch (SMgrException e) {
            fail("IdAssert SigVal: " + e.getMessage());
        }

        try {

            sm.verifySAMLAssertion(idas);
        } catch (SMgrException e) {
            fail("IdAssert2 SigVal: " + e.getMessage());
        }

        writeSAMLObjectToStream(idas, "assertion_before_verification.xml");
        try {

            trc = instance.issueTrcToken(idas, patientID, null, attrValuePair);
        } catch (SMgrException e) {
            fail("TRC Issue: " + e.getMessage());
        }

        writeSAMLObjectToStream(idas, "assertion_after_verification.xml");

        idas = loadSamlAssertionAsResource("SignedSamlAssertion.xml");
        try {
            instance.verifyTrcToken(trc, idas, patientID);

        } catch (SMgrException e) {
            fail("verifyToken: " + e.getMessage());
        }
        try {

            sm.verifySAMLAssertion(idas);
        } catch (SMgrException e) {
            fail("IdAssert SigVal: " + e.getMessage());
        }

        try {

            sm.verifySAMLAssertion(trc);
        } catch (SMgrException e) {
            fail("TRC SigVal: " + e.getMessage());
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
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlasRoot);
            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            hcpIdentityAssertion = (Assertion) unmarshaller.unmarshall(samlasRoot);
        } catch (UnmarshallingException | XMLParserException ex) {
            LOGGER.error(null, ex);
        }

        return hcpIdentityAssertion;

    }

    private void writeSAMLObjectToStream(SAMLObject so, String f) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc = dbf.newDocumentBuilder().newDocument();
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(so).marshall(so, signedDoc);
            XMLUtils.sendXMLtoStream(signedDoc, new FileOutputStream(f));
        } catch (FileNotFoundException | MarshallingException | ParserConfigurationException ex) {
            LOGGER.error(null, ex);
        }
    }
}
