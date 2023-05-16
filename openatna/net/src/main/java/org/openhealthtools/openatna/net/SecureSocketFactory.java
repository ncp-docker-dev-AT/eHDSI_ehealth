package org.openhealthtools.openatna.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Used by the connection factory to create customized secure sockets. <p> SecureSocketFactory can be used to validate
 * the identity of the HTTPS server against a list of trusted certificates and to authenticate to the HTTPS server using
 * a private key. </p> <p/> <p> SecureSocketFactory will enable server authentication when supplied with a {@link
 * KeyStore truststore} file contain one or several trusted certificates. The client secure socket will reject the
 * connection during the SSL session handshake if the target HTTPS server attempts to authenticate itself with a
 * non-trusted certificate. </p> <p/> <p> Use JDK keytool utility to import a trusted certificate and generate a
 * truststore file:
 * <pre>
 *     keytool -import -alias "my server cert" -file server.crt -keystore misys.jks
 *    </pre>
 * Alternately, generate a pkcs12 keychain and use that as a truststore:
 * <pre>
 *     openssl pkcs12 -in provided.cert -export -out misys.p12 -name provided
 * 	  </pre>
 * </p> <p/> <p> SecureSocketFactory will enable client authentication when supplied with a {@link KeyStore keystore}
 * file containg a private key/public certificate pair. The client secure socket will use the private key to
 * authenticate itself to the target HTTPS server during the SSL session handshake if requested to do so by the server.
 * The target HTTPS server will in its turn verify the certificate presented by the client in order to establish
 * client's authenticity </p> <p/> <p> Use the following sequence of actions to generate a keystore file </p> <p> Use
 * JDK keytool utility to generate a new key, make sure the store has the same password as the key.  Then check to make
 * sure that the key generation took.  You can use a keystore and a truststore, or one that does both. </p>
 * <pre>
 *  keytool -genkey -v -alias "misys" -validity 365 -keystore misys.jks
 *  keytool -list -v -keystore my.keystore
 * </pre>
 * <p> The lame keytool tool can't import private keys, so if you need to use another privte key (e.g. one given to you
 * for IHE connectathon) then you need to use a different system.  Change it to pkcs12 format and make sure that you
 * have the filname end in .p12 then the factory wil deal with it correctly.  How to use OpenSSL to generate the
 * appropriate files: </p>
 * <pre>
 *  openssl pkcs12 -in provided.cert -inkey provided.key -export -out misys.p12 -name provided
 * </pre>
 * <p> That makes a single keystore for both the private key and the public certs. </p> <p> Example of using custom
 * protocol socket factory for a specific host:
 * <pre>
 *     Protocol authhttps = new Protocol("https",
 *          new SecureSocketFactory(
 *              new URL("file:my.keystore"), "mypassword",
 *              new URL("file:my.truststore"), "mypassword"), 443);
 * <p/>
 *     HttpClient client = new HttpClient();
 *     client.getHostConfiguration().setHost("localhost", 443, authhttps);
 *     // use relative url only
 *     GetMethod httpget = new GetMethod("/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p> <p> Example of using custom protocol socket factory per default instead of the standard one:
 * <pre>
 *     Protocol authhttps = new Protocol("https",
 *          new SecureSocketFactory(
 *              new URL("file:my.keystore"), "mypassword",
 *              new URL("file:my.truststore"), "mypassword"), 443);
 *     Protocol.registerProtocol("https", authhttps);
 * <p/>
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https://localhost/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p>
 */
public class SecureSocketFactory {

    static Logger log = LoggerFactory.getLogger("org.openhealthtools.openatna.net.SecureSocketFactory");
    private final SecureConnectionDescription scd;
    private final URL keystoreUrl;
    private final String keystorePassword;
    private final URL truststoreUrl;
    private final String truststorePassword;
    private SSLContext sslcontext = null;

    /**
     * Constructor for HttpStreamHandler. Either a keystore or truststore file must be given.
     * Otherwise, SSL context initialization error will result.
     *
     * @param secureConnectionDescription The secure connection description
     */
    public SecureSocketFactory(SecureConnectionDescription secureConnectionDescription) {
        super();
        this.keystoreUrl = secureConnectionDescription.getKeyStore();
        this.keystorePassword = secureConnectionDescription.getKeyStorePassword();
        this.truststoreUrl = secureConnectionDescription.getTrustStore();
        this.truststorePassword = secureConnectionDescription.getTrustStorePassword();
        this.scd = secureConnectionDescription;
    }

    private SSLContext createSSLContext() throws IOException {

        try {
            log.debug("Attempting to create ssl context.");
            KeyManager[] keyManagers;
            TrustManager[] trustManagers = null;
            if (this.keystoreUrl == null) {
                throw new IOException("Cannot create SSL context without keystore");
            } else {
                KeyStore keystore = ConnectionCertificateHandler
                        .createKeyStore(this.keystoreUrl, this.keystorePassword);
                if (log.isDebugEnabled()) {
                    ConnectionCertificateHandler.printKeyCertificates(keystore);
                }
                keyManagers = ConnectionCertificateHandler.createKeyManagers(keystore, this.keystorePassword);
            }
            if (this.truststoreUrl != null) {
                KeyStore keystore = ConnectionCertificateHandler
                        .createKeyStore(this.truststoreUrl, this.truststorePassword);
                if (log.isDebugEnabled()) {
                    ConnectionCertificateHandler.printTrustCerts(keystore);
                }
                trustManagers = ConnectionCertificateHandler.createTrustManagers(keystore, this.scd);
            }
            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(keyManagers, trustManagers, null);

            return sslcontext;
        } catch (NoSuchAlgorithmException e) {
            log.error("NSA: '{}'", e.getMessage(), e);
            throw new IOException("Unsupported algorithm exception: " + e.getMessage());
        } catch (KeyStoreException e) {
            log.error("Key Store: '{}'", e.getMessage(), e);
            throw new IOException("Keystore exception: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error("General: '{}", e.getMessage(), e);
            throw new IOException("Key management exception: " + e.getMessage());
        } catch (IOException e) {
            log.error("I/O exception: '{}'", e.getMessage(), e);
            throw new IOException("I/O error reading keystore/truststore file: " + e.getMessage());
        }
    }

    public SSLContext getSSLContext() throws IOException {
        if (this.sslcontext == null) {
            this.sslcontext = createSSLContext();
        }
        return this.sslcontext;
    }

    public String[] getAtnaProtocols() {
        return new String[]{"TLSv1.2"};
    }

    private void setAtnaProtocols(SSLSocket secureSocket) {
        secureSocket.setEnabledProtocols(getAtnaProtocols());

        //String[] strings = {"SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA"};
        secureSocket.setEnabledCipherSuites(getAtnaCipherSuites());
        // Useful debugging information:
        //secureSocket.setSoTimeout(1000);
        //String[] strings = secureSocket.getSupportedCipherSuites();
        //for (String s: strings) log.info(s);
        //strings = secureSocket.getEnabledCipherSuites();
        //for (String s: strings) log.info(s);
    }

    public String[] getAtnaCipherSuites() {
        return new String[]{"SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA"};
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) {
        Socket socket = null;
        try {
            socket = getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
            setAtnaProtocols((SSLSocket) socket);
        } catch (java.net.ConnectException e) {
            log.error("Connection was refused when connecting to socket.", e);
        } catch (IOException e) {
            log.error("I/O problem creating socket.", e);
        } catch (Exception e) {
            log.error("Problem creating socket.", e);
        }
        return socket;
    }

    public Socket createSocket(String host, int port) {
        Socket socket = null;
        try {
            socket = getSSLContext().getSocketFactory().createSocket(host, port);
            setAtnaProtocols((SSLSocket) socket);
        } catch (java.net.ConnectException e) {
            log.error("Connection was refused when connecting to socket.", e);
        } catch (IOException e) {
            log.error("I/O problem creating socket.", e);
        } catch (Exception e) {
            log.error("Problem creating socket.", e);
        }
        return socket;
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) {
        Socket lsocket = null;
        try {
            lsocket = getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
            setAtnaProtocols((SSLSocket) lsocket);
        } catch (java.net.ConnectException e) {
            log.error("Connection was refused when connecting to socket.", e);
        } catch (IOException e) {
            log.error("I/O problem creating socket.", e);
        } catch (Exception e) {
            log.error("Problem creating socket.", e);
        }
        return lsocket;
    }

    /**
     * Extra socket creation for servers only.
     */
    public ServerSocket createServerSocket(int port) {
        log.info("createServerSocket on port: '{}'", port);
        SSLServerSocket sslServerSocket = null;
        try {
            sslServerSocket = (SSLServerSocket) getSSLContext().getServerSocketFactory().createServerSocket(port);
            //sslServerSocket.setNeedClientAuth(true);
            sslServerSocket.setWantClientAuth(true);
            String[] strings = {
                    "SSL_RSA_WITH_NULL_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_RSA_WITH_DES_CBC_SHA"};
            sslServerSocket.setEnabledCipherSuites(strings);
        } catch (IOException e) {
            log.error("I/O problem creating server socket.", e);
        }
        return sslServerSocket;
    }
}
