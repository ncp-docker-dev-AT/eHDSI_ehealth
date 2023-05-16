package org.openhealthtools.openatna.net;

import java.net.ServerSocket;

/**
 * This is the interface for all server socket connections.
 * <p/>
 * Use this interface instead of using the server sockets directly.  By doing so, you will be able to side step all
 * the details of making the connection.
 * In oder to get a connection, call the factory with the appropriate connection name.
 */
public interface IServerConnection {

    /**
     * Checks if the server connection is valid.
     * <p/>
     * A server connection might not be valid for any number of reasons, including an invalid certificate,
     * an unknown connection type.  This might be expanded in the future, or the factory might take care of informing
     * the user of these problems via exceptions.
     *
     * @return True if you can use this connection to get input and output streams.
     * @see ConnectionFactory
     */
    boolean isServerConnectionValid();

    /**
     * Get a direct handle on the server socket, beware!
     *
     * @return The sever socket that is being used for communication.
     */
    ServerSocket getServerSocket();

    /**
     * Closes the server connection.  Please close your own streams.
     */
    void closeServerConnection();

    /**
     * Gets the description object for this connection.
     *
     * @return The description object for this connection.
     */
    IConnectionDescription getConnectionDescription();

    /**
     * Connects the server connection. Only called by the factory.
     */
    void connect();
}
