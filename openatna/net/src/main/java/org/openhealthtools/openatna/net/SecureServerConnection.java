package org.openhealthtools.openatna.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;

/**
 * An encrypted tcp server connection.
 * This should not be created directly but rather, requested from the ConnectionFactory.
 */
public class SecureServerConnection extends GenericServerConnection {

    private final Logger logger = LoggerFactory.getLogger(SecureServerConnection.class);

    /**
     * Used by the factory to create a server connection.
     */
    public SecureServerConnection(IConnectionDescription connectionDescription) {
        super(connectionDescription);
    }

    /**
     * Checks to make sure the description matches requirements for SecureServerConnection.
     */
    @Override
    public boolean isServerConnectionValid() {
        boolean isValid = false;
        if ((description != null) &&
                description.isSecure() &&
                (description instanceof SecureConnectionDescription)) {
            isValid = super.isServerConnectionValid();
        }
        return isValid;
    }

    /**
     * Used by factory to start the server connection.
     */
    public void connect() {

        logger.info("Start connection to secured server...");
        SSLServerSocket secureServerSocket = null;
        if (description instanceof SecureConnectionDescription) {
            SecureConnectionDescription secureConnectionDescription = (SecureConnectionDescription) description;
            // Secure socket factory handles bidirectional certs.
            SecureSocketFactory secureSocketFactory = new SecureSocketFactory(secureConnectionDescription);
            secureServerSocket = (SSLServerSocket) secureSocketFactory.createServerSocket(description.getPort());
            logger.info("Client Authentication needed: '{}'", secureServerSocket.getNeedClientAuth());
            logger.info("Client Authentication required: '{}'", secureServerSocket.getWantClientAuth());
            // TODO Add ATNA logging.
            secureServerSocket.setNeedClientAuth(true);
            secureServerSocket.setWantClientAuth(true);
            logger.info("Client Authentication needed: '{}'", secureServerSocket.getNeedClientAuth());
            logger.info("Client Authentication required: '{}'", secureServerSocket.getWantClientAuth());
        }
        ssocket = secureServerSocket;
    }
}
