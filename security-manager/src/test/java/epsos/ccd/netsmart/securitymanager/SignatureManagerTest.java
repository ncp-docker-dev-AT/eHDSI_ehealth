package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.impl.NSTestKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.SPMSTestKeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.TianiTestKeyStoreManager;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * @author jerouris
 */
public class SignatureManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureManagerTest.class);

    public SignatureManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
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
     * Test of signXMLWithEnvelopedSig method, of class SignatureManager.
     */
    @Ignore
    @Test
    public void testSignXMLWithEnvelopedSig() {
        try {

            LOGGER.info("signXMLWithEnvelopedSig");
            String keyAlias = "testncp";
            String keyPassword = "epsos123";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            File f = new File("mySignedFile.xml");

            Document doc;
            doc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("ePsample_stripped.xml"));

            SignatureManager smgr = new SignatureManager(new NSTestKeyStoreManager());
            smgr.signXMLWithEnvelopedSig(doc, keyAlias, keyPassword.toCharArray());

            XMLUtils.sendXMLtoStream(doc, new FileOutputStream(f));

            smgr.verifyEnvelopedSignature(doc);

            Document signedDoc;
            signedDoc = dbf.newDocumentBuilder().parse(f);

            smgr.verifyEnvelopedSignature(signedDoc);

        } catch (SMgrException | ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void testSignXMLWithEnvelopedSigTiani() {

        try {

            LOGGER.info("signXMLWithEnvelopedSigTiani");
            String keyAlias = "server1";
            String keyPassword = "spirit";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("ePsample_stripped.xml"));

            SignatureManager smgr = new SignatureManager(new TianiTestKeyStoreManager());
            smgr.signXMLWithEnvelopedSig(doc, keyAlias, keyPassword.toCharArray());
            smgr.verifyEnvelopedSignature(doc);

        } catch (SMgrException | ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void testSignXMLWithEnvelopedSigSPMS() {
        try {

            LOGGER.info("signXMLWithEnvelopedSigSPMS");
            String keyAlias = "ppt.ncp-signature.epsos.spms.pt";
            String keyPassword = "changeit";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            Document doc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("ePsample_stripped.xml"));

            SignatureManager smgr = new SignatureManager(new SPMSTestKeyStoreManager());
            smgr.signXMLWithEnvelopedSig(doc, keyAlias, keyPassword.toCharArray());
            smgr.verifyEnvelopedSignature(doc);

        } catch (SMgrException | ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of verifyEnvelopedSignature method, of class SignatureManager.
     */
    @Ignore
    @Test
    public void testSuccessfulVerifyEnvelopedSignature() {

        try {
            LOGGER.info("verifyEnvelopedSignature");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc;
            signedDoc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("signed_ePsample_UNK.xml"));
            SignatureManager instance = new SignatureManager(new NSTestKeyStoreManager());
            instance.verifyEnvelopedSignature(signedDoc);
        } catch (SMgrException | ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void testFailedVerifyEnvelopedSignatureTiani() {

        try {
            LOGGER.info("failedVerifyEnvelopedSignature");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc;
            signedDoc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("signed_ePsample_UNK.xml"));

            // Provide a wrong keystore. This should make the signature invalid
            SignatureManager instance = new SignatureManager(new TianiTestKeyStoreManager());
            instance.verifyEnvelopedSignature(signedDoc);
        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }

    @Ignore
    @Test
    public void testFailedVerifyEnvelopedSignatureSPMS() {

        try {
            LOGGER.info("failedVerifyEnvelopedSignatureSPMS");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document signedDoc;
            signedDoc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("signed_ePsample_UNK.xml"));

            // Provide a wrong keystore. This should make the signature invalid
            SignatureManager instance = new SignatureManager(new SPMSTestKeyStoreManager());
            instance.verifyEnvelopedSignature(signedDoc);

        } catch (SMgrException ex) {
            LOGGER.error(null, ex);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(null, ex);
            fail(ex.getMessage());
        }
    }
}
