package eu.epsos.pt.cc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * ClientConnectorInit servlet.
 * <p>
 * This servlet is called at startup and set the environment
 * for security
 *
 * @author Ivo Pinheiro<code> - ivo.pinheiro@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class ClientConnectorInit extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectorInit.class);

    @Override
    public void init() throws ServletException {

        LOGGER.info("Initiating Client Connector");
        super.init();

        System.setProperty("javax.net.ssl.keyStore", Constants.SC_KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", Constants.SC_KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.key.alias", Constants.SC_PRIVATEKEY_ALIAS);
        System.setProperty("javax.net.ssl.privateKeyPassword", Constants.SC_PRIVATEKEY_PASSWORD);
        System.setProperty("javax.net.ssl.trustStore", Constants.TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", Constants.TRUSTSTORE_PASSWORD);
    }
}
