package epsos.ccd.netsmart.securitymanager.sts.client;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.UUID;

public class AssertionRequest {

    public static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public static final String ACTION_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue";
    public static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
    public static final String NOK_NS = "https://ehdsi.eu/assertion/nok";
    public static final String TRC_NS = "https://ehdsi.eu/assertion/trc";
    public static final String WS_SEC_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";

    private final Logger logger = LoggerFactory.getLogger(AssertionRequest.class);

    private DocumentBuilder documentBuilder;

    public AssertionRequest() throws STSClientException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new STSClientException("Unable to create RST Message");
        }
    }

    public DocumentBuilder getDocumentBuilder() {
        return documentBuilder;
    }

    public void setDocumentBuilder(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    public Element convertAssertionToElement(Assertion assertion) {


        try {
            Document doc = documentBuilder.newDocument();
            Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion);
            if (marshaller == null) {
                logger.error("SAML Marshalling is NULL");
                return null;
            }
            marshaller.marshall(assertion, doc);
            return doc.getDocumentElement();
        } catch (MarshallingException e) {
            logger.error("MarshallingException: '{}", e.getMessage(), e);
            return null;
        }
    }

    public Assertion convertElementToAssertion(Element element) {

        // Unmarshalling using the document root element, an EntitiesDescriptor in this case
        try {

            Unmarshaller unmarshaller = XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(element);
            if (unmarshaller == null) {
                logger.error("SAML Unmarshalling is NULL");
                return null;
            }
            return (Assertion) unmarshaller.unmarshall(element);

        } catch (UnmarshallingException e) {
            logger.error("UnmarshallingException: '{}", e.getMessage(), e);
            return null;
        }
    }

    public String createMessageId() {
        return Constants.UUID_PREFIX + UUID.randomUUID();
    }

    public void createRSTHeader(SOAPHeader header, String messageId, Assertion assertion) {

        try {

            SOAPHeaderElement messageIdElem = header.addHeaderElement(new QName(ADDRESSING_NS, "MessageID", "wsa"));
            messageIdElem.setTextContent(messageId);

            SOAPHeaderElement securityHeaderElem = header.addHeaderElement(new QName(WS_SEC_NS, "Security", "wsse"));
            securityHeaderElem.setMustUnderstand(true);

            Element assertionElement = convertAssertionToElement(assertion);
            securityHeaderElem.appendChild(header.getOwnerDocument().importNode(assertionElement, true));

        } catch (SOAPException ex) {
            logger.error(null, ex);
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {

        SSLContext sslContext;
        try {
            KeyStoreManager keyStoreManager = new DefaultKeyStoreManager();
            String sigKeystorePassword = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PASSWORD");

            sslContext = SSLContext.getInstance("TLSv1.2");

            var keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStoreManager.getKeyStore(), sigKeystorePassword.toCharArray());

            var trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStoreManager.getTrustStore());

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();

        } catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.error("Exception: '{}'", e.getMessage(), e);
            return null;
        }
    }
}
