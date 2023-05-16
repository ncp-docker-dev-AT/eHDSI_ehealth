package org.openhealthtools.openatna.net;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * An abstract implementation of IServerConnection which does a number of items required by all connections.
 * <p/>
 * To make a new type of connection which requires these features simply extend this class and implement
 * the additionally required features.
 * Remember that call to connect method is where the socket (or other connection type) should be made.
 */
public abstract class GenericServerConnection implements IServerConnection {

    /**
     * The actual connection.
     */
    protected ServerSocket ssocket = null;
    /**
     * The description of the connection. This includes everything needed to connect.
     */
    protected IConnectionDescription description;

    public GenericServerConnection(IConnectionDescription connectionDescription) {
        description = connectionDescription;
    }

    /* (non-Javadoc)
     * @see org.openhealthtools.openatna.net.IConnection#getConnectionDescription()
     */
    public IConnectionDescription getConnectionDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.openhealthtools.openatna.net.IServerConnection#isServerConnectionValid()
     */
    public boolean isServerConnectionValid() {
        boolean isValid = false;
        if (ssocket != null) {
            isValid = ssocket.isBound();
        }
        return isValid;
    }

    /* (non-Javadoc)
     * @see org.openhealthtools.openatna.net.IServerConnection#getServerSocket()
     */
    public ServerSocket getServerSocket() {
        ServerSocket returnVal = null;
        if (isServerConnectionValid()) {
            returnVal = ssocket;
        }
        //  TODO: add logging message.
        return returnVal;
    }

    /* (non-Javadoc)
     * @see org.openhealthtools.openatna.net.IServerConnection#closeServerConnection()
     */
    public void closeServerConnection() {
        if (isServerConnectionValid()) {
            try {
                ssocket.close();
            } catch (IOException e) {
            }
            //  TODO: add logging message.
            //  TODO: add ATNA message?
        }
        ssocket = null;
    }
}
