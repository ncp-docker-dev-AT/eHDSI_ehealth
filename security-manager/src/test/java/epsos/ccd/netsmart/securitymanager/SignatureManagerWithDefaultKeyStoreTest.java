package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import epsos.ccd.netsmart.securitymanager.key.impl.NSTestKeyStoreManager;
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
public class SignatureManagerWithDefaultKeyStoreTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureManagerWithDefaultKeyStoreTest.class);

    public SignatureManagerWithDefaultKeyStoreTest() {
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

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            File f = new File("mySignedFile.xml");
            Document doc;
            doc = dbf.newDocumentBuilder().parse(ClassLoader.getSystemResourceAsStream("ePsample_stripped.xml"));

            SignatureManager smgr = new SignatureManager();
            smgr.signXMLWithEnvelopedSig(doc);

            XMLUtils.sendXMLtoStream(doc, new FileOutputStream(f));
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
}
