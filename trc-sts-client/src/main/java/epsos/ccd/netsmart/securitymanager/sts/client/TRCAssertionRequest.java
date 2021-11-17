package epsos.ccd.netsmart.securitymanager.sts.client;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.OpenNCPValidation;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The TRC STS client. It can be used as a reference implementation for requesting a TRC Assertion from TRC-STS Service.
 * It uses the Builder Design Pattern to create the request, in order to create an immutable final object.
 */
public class TRCAssertionRequest extends AssertionRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TRCAssertionRequest.class);

    private static final QName MESSAGING_TO = new QName("http://www.w3.org/2005/08/addressing", "To");
    private static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing"; // WSA Namespace
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private static final String DEFAULT_STS_URL;
    private static final String CHECK_FOR_HOSTNAME;

    static {

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            LOGGER.error("OpenSAML module cannot be initialized: '{}'", e.getMessage(), e);
        }
        if (ConfigurationManagerFactory.getConfigurationManager().getProperty(STSConstant.URL_WS_TRC).length() == 0) {
            ConfigurationManagerFactory.getConfigurationManager()
                    .setProperty(STSConstant.URL_WS_TRC, "https://localhost:8443/TRC-STS/SecurityTokenService");
        }
        DEFAULT_STS_URL = ConfigurationManagerFactory.getConfigurationManager().getProperty(STSConstant.URL_WS_TRC);

        if (ConfigurationManagerFactory.getConfigurationManager().getProperty(STSConstant.STS_CHECK_HOSTNAME).length() == 0) {
            ConfigurationManagerFactory.getConfigurationManager().setProperty(STSConstant.STS_CHECK_HOSTNAME, "false");
        }
        CHECK_FOR_HOSTNAME = ConfigurationManagerFactory.getConfigurationManager().getProperty(STSConstant.STS_CHECK_HOSTNAME);
    }

    private final Assertion idAssert;
    private final String purposeOfUse;
    private final String prescriptionId;
    private final String patientId;
    private final String dispensationPinCode;
    private final SOAPMessage rstMsg;
    private final String messageId;
    private final URL location;

    /**
     * The builder is the only class that can call the constructor and for that, the following will be surely initialized.
     *
     * @param builder
     * @throws STSClientException
     */
    private TRCAssertionRequest(Builder builder) throws STSClientException {

        try {
            this.idAssert = builder.idAssert;
            this.patientId = builder.patientId;
            this.dispensationPinCode = builder.dispensationPinCode;
            this.prescriptionId = builder.prescriptionId;
            this.purposeOfUse = builder.purposeOfUse;
            this.location = builder.location;
            this.messageId = createMessageId();

            rstMsg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
            createRSTHeader(rstMsg.getSOAPHeader(), messageId, idAssert);
            createRSTBody(rstMsg.getSOAPBody());
        } catch (SOAPException ex) {
            throw new STSClientException("Unable to create RST Message");
        }
    }

    /**
     * @param body
     */
    private void createRSTBody(SOAPBody body) {

        try {
            var soapFactory = SOAPFactory.newInstance();
            var rstName = soapFactory.createName("RequestSecurityToken", "wst", WS_TRUST_NS);
            SOAPBodyElement rstElem = body.addBodyElement(rstName);

            var reqTypeName = soapFactory.createName("RequestType", "wst", WS_TRUST_NS);
            SOAPElement reqTypeElem = rstElem.addChildElement(reqTypeName);
            reqTypeElem.addTextNode(ACTION_URI);

            var tokenName = soapFactory.createName("TokenType", "wst", WS_TRUST_NS);
            SOAPElement tokenElem = rstElem.addChildElement(tokenName);
            tokenElem.addTextNode(SAML20_TOKEN_URN);

            var trcParamsName = soapFactory.createName("TRCParameters", "trc", TRC_NS);
            SOAPElement trcParamsElem = rstElem.addChildElement(trcParamsName);

            var purposeOfUseName = soapFactory.createName("PurposeOfUse", "trc", TRC_NS);
            SOAPElement purposeOfUseElem = trcParamsElem.addChildElement(purposeOfUseName);
            purposeOfUseElem.addTextNode(purposeOfUse);

            var patientIdName = soapFactory.createName("PatientId", "trc", TRC_NS);
            SOAPElement patientIdElem = trcParamsElem.addChildElement(patientIdName);
            patientIdElem.addTextNode(patientId);

            if (StringUtils.isNotBlank(dispensationPinCode)) {
                var dispensationPinCodeName = soapFactory.createName("DispensationPinCode", "trc", TRC_NS);
                SOAPElement dispensationPinCodeElement = trcParamsElem.addChildElement(dispensationPinCodeName);
                dispensationPinCodeElement.addTextNode(dispensationPinCode);
            }
            if (StringUtils.isNotBlank(prescriptionId)) {
                var prescriptionIdName = soapFactory.createName("PrescriptionId", "trc", TRC_NS);
                SOAPElement prescriptionIdElement = trcParamsElem.addChildElement(prescriptionIdName);
                prescriptionIdElement.addTextNode(prescriptionId);
            }
        } catch (SOAPException ex) {
            LOGGER.error(null, ex);
        }
    }

    /**
     * Sends the request to the TRC STS Service.
     *
     * @return the TRC Assertion that was received from the STS, if the request was successful.
     * @throws STSClientException if the request failed.
     */
    public Assertion request() throws STSClientException {

        try {
            LOGGER.info("TRC-STS client request Assertion");
            HttpURLConnection httpConnection = (HttpURLConnection) location.openConnection();
            //Set headers
            httpConnection.setRequestProperty("Content-Type", "application/soap+xml");
            httpConnection.setRequestProperty("SOAPAction", "");
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            LOGGER.info("Checking SSL Hostname Verifier: '{}'", CHECK_FOR_HOSTNAME);
            if (httpConnection instanceof HttpsURLConnection) {  // Going SSL
                ((HttpsURLConnection) httpConnection).setSSLSocketFactory(getSSLSocketFactory());
                if (StringUtils.equals(CHECK_FOR_HOSTNAME, "false"))
                    ((HttpsURLConnection) httpConnection).setHostnameVerifier(
                            (hostname, sslSession) -> true);
            }

            String value = System.getProperty("javax.net.ssl.key.alias");

            //  Write and send the SOAP message
            LOGGER.info("Sending SOAP request - Default Key Alias: '{}'", StringUtils.isNotBlank(value) ? value : "N/A");
            rstMsg.writeTo(httpConnection.getOutputStream());
            SOAPMessage response = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
                    .createMessage(new MimeHeaders(), httpConnection.getInputStream());

            LOGGER.info("Receiving SOAP response");
            if (response.getSOAPBody().hasFault()) {

                SOAPFault newFault = response.getSOAPBody().getFault();
                String code = newFault.getFaultCode();
                var string = newFault.getFaultString();
                throw new SOAPException("Code:" + code + ", Error String:" + string);
            }
            var assertionTRCA = extractTRCAssertionFromRSTC(response);
            LOGGER.info("TRC Assertion: '{}'", assertionTRCA != null ? assertionTRCA.getID() : "TRC Assertion is NULL");
            return assertionTRCA;

        } catch (SOAPException | IOException ex) {
            throw new STSClientException("SOAP Exception: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            throw new STSClientException("Unsupported Operation: " + ex.getMessage());
        }
    }

    private Assertion extractTRCAssertionFromRSTC(SOAPMessage response) throws STSClientException {

        try {
            LOGGER.info("[TRC-STS Client] Extract TRC from RSTC");
            var body = response.getSOAPBody();
            if (body.getElementsByTagNameNS(SAML20_TOKEN_URN, "Assertion").getLength() != 1) {
                throw new STSClientException("TRC Assertion is missing from the RSTRC body");
            }
            SOAPElement assertion = (SOAPElement) body.getElementsByTagNameNS(SAML20_TOKEN_URN, "Assertion").item(0);
            Document assertDoc = getDocumentBuilder().newDocument();

            Node dupBody = assertDoc.importNode(assertion, true);
            assertDoc.appendChild(dupBody);
            if (assertion == null) {
                return null;
            }

            var unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            var unmarshaller = unmarshallerFactory.getUnmarshaller(assertion);
            var trcAssertion = (Assertion) unmarshaller.unmarshall(assertDoc.getDocumentElement());
            if (OpenNCPValidation.isValidationEnable()) {

                OpenNCPValidation.validateTRCAssertion(trcAssertion, NcpSide.NCP_B);
            }
            return trcAssertion;

        } catch (Exception ex) {
            throw new STSClientException("Error while trying to extract the SAML TRC Assertion from RSTRC Body: " + ex.getMessage());
        }
    }

    /**
     * The Builder class is responsible for the creation of the final TRCAssertionRequest Object.
     * It is used to incrementally create the TRCAssertionRequest and then when calling #build returns the final immutable object
     */
    public static class Builder {

        //  Required attributes
        private final Assertion idAssert;
        private final String patientId;
        //  Optional attributes
        private String purposeOfUse = "TREATMENT";
        private String prescriptionId;
        private String dispensationPinCode;
        private URL location = null;

        /**
         * The Builder class constructor. Its parameters are the required fields of the TRCAssertionRequest Object.
         *
         * @param idAssert  The OpenSAML Identity Assertion
         * @param patientId the relevant patient id.
         */
        public Builder(Assertion idAssert, String patientId) {

            this.idAssert = idAssert;
            this.patientId = patientId;
            try {

                this.location = new URL(DEFAULT_STS_URL);
            } catch (MalformedURLException ex) {
                LOGGER.error(null, ex);
            }
        }

        public Builder prescriptionId(String prescriptionId) {

            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder dispensationPinCode(String dispensationPinCode) {

            this.dispensationPinCode = dispensationPinCode;
            return this;
        }

        /**
         * Method to incrementally add the Purpose Of Use parameter of the TRC Request.
         *
         * @param purposeOfUse Purpose Of use. Either TREATMENT or EMERGENCY
         * @return the Builder object for further initialization
         */
        public Builder purposeOfUse(String purposeOfUse) {

            this.purposeOfUse = purposeOfUse;
            return this;
        }

        /**
         * method to incrementally add the STS URL the request. If not added, the builder will use the one that exists
         * in the secman.sts.url parameter of the OpenNCP database properties.
         *
         * @param url the URL of the STS that the client will make the request
         * @return the Builder object for further initialization
         */
        public Builder location(String url) {

            try {
                this.location = new URL(url);
            } catch (MalformedURLException ex) {
                LOGGER.error(null, ex);
            }
            return this;
        }

        public TRCAssertionRequest build() throws STSClientException {

            return new TRCAssertionRequest(this);
        }
    }
}
