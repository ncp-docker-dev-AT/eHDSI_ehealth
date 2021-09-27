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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The STS client. It can be used as a reference implementation for requesting a NoK Assertion from STS Service.
 * It uses the Builder Design Pattern to create the request, in order to create an immutable final object.
 */
public class NextOfKinAssertionRequest extends AssertionRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextOfKinAssertionRequest.class);

    private static final QName MESSAGING_TO = new QName("http://www.w3.org/2005/08/addressing", "To");
    private static final String SAML20_TOKEN_URN = "urn:oasis:names:tc:SAML:2.0:assertion"; // What can be only requested from the STS
    private static final String ACTION_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue"; // Only Issuance is supported
    private static final String WS_TRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512"; // WS-Trust Namespace
    private static final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing"; // WSA Namespace
    private static final String WS_SEC_UTIL_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WS_SEC_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String DEFAULT_STS_URL;
    private static final String CHECK_FOR_HOSTNAME;

    static {

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            LOGGER.error("OpenSAML module cannot be initialized: '{}'", e.getMessage(), e);
        }
        if (ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.nextOfKin.url").length() == 0) {
            ConfigurationManagerFactory.getConfigurationManager().setProperty("secman.nextOfKin.url", "https://localhost:8443/TRC-STS/SecurityTokenService");
        }
        DEFAULT_STS_URL = ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.nextOfKin.url");

        if (ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.sts.checkHostname").length() == 0) {
            ConfigurationManagerFactory.getConfigurationManager().setProperty("secman.sts.checkHostname", "false");
        }
        CHECK_FOR_HOSTNAME = ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.sts.checkHostname");
    }

    private final Assertion idAssert;
    private final String purposeOfUse;
    private final String patientId;
    private final String nextOfKinId;
    private final String nextOfKinFamilyName;
    private final String nextOfKinFirstName;
    private final Date nextOfKinBirthDate;
    private final String nextOfKinGender;
    private final String nextOfKinAddressStreet;
    private final String nextOfKinAddressPostalCode;
    private final String nextOfKinAddressCity;
    private final String nextOfKinAddressCountry;
    private final SOAPMessage rstMsg;
    private final String messageId;

    private final URL location;

    /**
     * The builder is the only class that can call the constructor and for that, the following will be surely initialized.
     *
     * @param builder
     * @throws Exception
     */
    private NextOfKinAssertionRequest(Builder builder) throws STSClientException {

        this.idAssert = builder.idAssert;
        this.patientId = builder.patientId;
        this.nextOfKinId = builder.nextOfKinId;
        this.purposeOfUse = builder.purposeOfUse;
        this.nextOfKinFamilyName = builder.nextOfKinFamilyName;
        this.nextOfKinFirstName = builder.nextOfKinFirstName;
        this.nextOfKinBirthDate = builder.nextOfKinBirthdate;
        this.nextOfKinGender = builder.nextOfKinAdministrativeGender;
        this.nextOfKinAddressStreet = builder.nextOfKinAddressStreet;
        this.nextOfKinAddressCity = builder.nextOfKinAddressCity;
        this.nextOfKinAddressCountry = builder.nextOfKinAddressCountry;
        this.nextOfKinAddressPostalCode = builder.nextOfKinAddressPostalCode;
        this.location = builder.location;
        this.messageId = createMessageId();

        try {
            rstMsg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
            createRSTHeader(rstMsg.getSOAPHeader(), messageId, idAssert);
            createRequestSecurityTokenBody(rstMsg.getSOAPBody());
        } catch (SOAPException ex) {
            throw new STSClientException("Unable to create RST Message");
        }
    }

    /**
     * @param body
     */
    private void createRequestSecurityTokenBody(SOAPBody body) {

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

            var assertionParamName = soapFactory.createName("NoKParameters", "nok", NOK_NS);
            SOAPElement assertionParamElement = rstElem.addChildElement(assertionParamName);

            var purposeOfUseName = soapFactory.createName("PurposeOfUse", "nok", NOK_NS);
            SOAPElement purposeOfUseElem = assertionParamElement.addChildElement(purposeOfUseName);
            purposeOfUseElem.addTextNode(purposeOfUse);

            var patientIdName = soapFactory.createName("PatientId", "nok", NOK_NS);
            SOAPElement patientIdElem = assertionParamElement.addChildElement(patientIdName);
            patientIdElem.addTextNode(patientId);

            if (StringUtils.isNotBlank(nextOfKinId)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinId", "nok", NOK_NS);
                SOAPElement dispensationPinCodeElement = assertionParamElement.addChildElement(nextOfKinIdName);
                dispensationPinCodeElement.addTextNode(nextOfKinId);
            }
            if (StringUtils.isNotBlank(nextOfKinFirstName)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinFirstName", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinFirstName);
            }
            if (StringUtils.isNotBlank(nextOfKinFamilyName)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinFamilyName", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinFamilyName);
            }
            if (StringUtils.isNotBlank(nextOfKinGender)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinGender", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinGender);
            }
            if (StringUtils.isNotBlank(nextOfKinAddressStreet)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinAddressStreet", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinAddressStreet);
            }
            if (StringUtils.isNotBlank(nextOfKinAddressCity)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinAddressCity", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinAddressCity);
            }
            if (StringUtils.isNotBlank(nextOfKinAddressPostalCode)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinAddressPostalCode", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinAddressPostalCode);
            }
            if (StringUtils.isNotBlank(nextOfKinAddressCountry)) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinAddressCountry", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                nextOfKinGivenNameElement.addTextNode(nextOfKinAddressCountry);
            }
            if (nextOfKinBirthDate != null) {
                var nextOfKinIdName = soapFactory.createName("NextOfKinBirthDate", "nok", NOK_NS);
                SOAPElement nextOfKinGivenNameElement = assertionParamElement.addChildElement(nextOfKinIdName);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                nextOfKinGivenNameElement.addTextNode(dateFormat.format(nextOfKinBirthDate));
            }
        } catch (SOAPException ex) {
            LOGGER.error(null, ex);
        }
    }

    /**
     * Sends the request to the STS Service.
     *
     * @return the NoK Assertion that was received from the STS, if the request was successful.
     * @throws Exception if the request failed.
     */
    public Assertion request() throws Exception {

        try {
            LOGGER.info("Next of Kin - STS client request Assertion: '{}'", DEFAULT_STS_URL);
            HttpURLConnection httpConnection = (HttpURLConnection) location.openConnection();
            //Set headers
            httpConnection.setRequestProperty("Content-Type", "application/soap+xml");
            httpConnection.setRequestProperty("SOAPAction", "");
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            LOGGER.info("Checking SSL Hostname Verifier: '{}'", CHECK_FOR_HOSTNAME);
            if (httpConnection instanceof HttpsURLConnection) {
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
            var assertionNOK = extractNoKAssertionFromRSTC(response);
            LOGGER.info("NoK Assertion: '{}'", assertionNOK != null ? assertionNOK.getID() : "NoK Assertion is NULL");
            return assertionNOK;

        } catch (SOAPException ex) {
            throw new Exception("SOAP Exception: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            throw new Exception("Unsupported Operation: " + ex.getMessage());
        }
    }

    private Assertion extractNoKAssertionFromRSTC(SOAPMessage response) throws Exception {

        try {
            LOGGER.info("[STS Client] Extract NoK from Request Security Token");
            var body = response.getSOAPBody();
            if (body.getElementsByTagNameNS(SAML20_TOKEN_URN, "Assertion").getLength() != 1) {
                throw new Exception("NoK Assertion is missing from the RSTC body");
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

            var nokAssertion = (Assertion) unmarshaller.unmarshall(assertDoc.getDocumentElement());
            if (OpenNCPValidation.isValidationEnable()) {
                OpenNCPValidation.validateNoKAssertion(nokAssertion, NcpSide.NCP_B);
            }
            return nokAssertion;

        } catch (Exception ex) {
            throw new Exception("Error while trying to extract the SAML NoK Assertion from RSTC Body: " + ex.getMessage());
        }
    }

    /**
     * The Builder class is responsible for the creation of the final NextOfKinAssertionRequest Object.
     * It is used to incrementally create the NoKAssertionRequest and then when calling #build returns the final immutable object
     */
    public static class Builder {

        //  Required attributes
        private final Assertion idAssert;
        private final String patientId;
        //  Optional attributes
        private String purposeOfUse = "TREATMENT";
        private String nextOfKinId;
        private String nextOfKinFirstName;
        private String nextOfKinFamilyName;
        private String nextOfKinAdministrativeGender;
        private Date nextOfKinBirthdate;
        private String nextOfKinAddressStreet;
        private String nextOfKinAddressPostalCode;
        private String nextOfKinAddressCity;
        private String nextOfKinAddressCountry;
        private URL location = null;

        /**
         * The Builder class constructor. Its parameters are the required fields of the NextOfKinAssertionRequest Object.
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

        public Builder nextOfKinId(String nextOfKinId) {

            this.nextOfKinId = nextOfKinId;
            return this;
        }

        public Builder nextOfKinFirstName(String nextOfKinFirstName) {

            this.nextOfKinFirstName = nextOfKinFirstName;
            return this;
        }

        public Builder nextOfKinFamilyName(String nextOfKinFamilyName) {

            this.nextOfKinFamilyName = nextOfKinFamilyName;
            return this;
        }

        public Builder nextOfKinAdministrativeGender(String nextOfKinAdministrativeGender) {

            this.nextOfKinAdministrativeGender = nextOfKinAdministrativeGender;
            return this;
        }

        public Builder nextOfKinBirthdate(Date nextOfKinBirthdate) {

            this.nextOfKinBirthdate = nextOfKinBirthdate;
            return this;
        }

        public Builder nextOfKinAddressStreet(String nextOfKinAddressStreet) {

            this.nextOfKinAddressStreet = nextOfKinAddressStreet;
            return this;
        }

        public Builder nextOfKinAddressPostalCode(String nextOfKinAddressPostalCode) {

            this.nextOfKinAddressPostalCode = nextOfKinAddressPostalCode;
            return this;
        }

        public Builder nextOfKinAddressCity(String nextOfKinAddressCity) {

            this.nextOfKinAddressCity = nextOfKinAddressCity;
            return this;
        }

        public Builder nextOfKinAddressCountry(String nextOfKinAddressCountry) {

            this.nextOfKinAddressCountry = nextOfKinAddressCountry;
            return this;
        }

        /**
         * Method to incrementally add the Purpose Of Use parameter of the NoK Request.
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

        public NextOfKinAssertionRequest build() throws Exception {

            return new NextOfKinAssertionRequest(this);
        }
    }
}
