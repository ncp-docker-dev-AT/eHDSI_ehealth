package eu.europa.ec.sante.openncp.sts.client;

import eu.ehdsi.openncp.sts.client.cxf.MessageBody;
import eu.ehdsi.openncp.sts.client.cxf.SecurityTokenService;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class SecurityTokenServiceClientImpl implements SecurityTokenServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTokenServiceClientImpl.class);
    private static final String DEFAULT_STS_URL;
    private static final SecurityTokenService securityTokenService;

    static {

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            logger.error("OpenSAML module cannot be initialized: '{}'", e.getMessage(), e);
        }
        if (ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.sts.url").length() == 0) {
            ConfigurationManagerFactory.getConfigurationManager().setProperty("secman.sts.url", "https://localhost:8443/TRC-STS/SecurityTokenService");
        }
        DEFAULT_STS_URL = ConfigurationManagerFactory.getConfigurationManager().getProperty("secman.sts.url");
        try {
            securityTokenService = new SecurityTokenService(new URL(DEFAULT_STS_URL), new WSAddressingFeature());
        } catch (MalformedURLException e) {
            throw new RuntimeException();
        }
    }


    @Override
    public Assertion issueNextOfKinToken() {
        MessageBody messageBody = securityTokenService.getSecurityTokenServicePort().issueNextOfKinToken(null);
        return null;
    }

    @Override
    public Assertion issueTreatmentConfirmationToken() {
        logger.info("method issueTreatmentConfirmationToken()");
        MessageBody messageBody = securityTokenService.getSecurityTokenServicePort().issueTreatmentToken(null);
        List<Object> objectList = messageBody.getAny();
        return null;
    }
}
