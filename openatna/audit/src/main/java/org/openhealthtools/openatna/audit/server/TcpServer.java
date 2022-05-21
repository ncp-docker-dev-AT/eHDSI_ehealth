package org.openhealthtools.openatna.audit.server;

import org.openhealthtools.openatna.net.ConnectionFactory;
import org.openhealthtools.openatna.net.IConnectionDescription;
import org.openhealthtools.openatna.net.IServerConnection;
import org.openhealthtools.openatna.syslog.SyslogException;
import org.openhealthtools.openatna.syslog.SyslogMessage;
import org.openhealthtools.openatna.syslog.SyslogMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class TcpServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
    private AtnaServer atnaServer;
    private IConnectionDescription tlsConnection;
    private IServerConnection tlsConn = null;
    private boolean running = false;
    private TcpServerThread thread;
    private ServerSocket ss = null;

    public TcpServer(AtnaServer atnaServer, IConnectionDescription tlsConnection) {
        this.atnaServer = atnaServer;
        this.tlsConnection = tlsConnection;
    }

    public void start() {
        tlsConn = ConnectionFactory.getServerConnection(tlsConnection);
        ss = tlsConn.getServerSocket();
        running = true;
        thread = new TcpServerThread(ss);
        thread.start();
        LOGGER.info("TLS Server running on port:" + tlsConnection.getPort());
    }

    public void stop() {
        running = false;
        try {
            ss.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close Tcp server socket.", e);
        }
        thread.interrupt();
        tlsConn.closeServerConnection();
        LOGGER.info("TLS Server shutting down...");
    }

    private class TcpServerThread extends Thread {

        private ServerSocket server;

        public TcpServerThread(ServerSocket server) {
            this.server = server;
        }

        @Override
        public void run() {
            if (server == null) {
                LOGGER.info("Server socket is null. Cannot start server.");
                running = false;
                return;
            }
            while (running && !interrupted()) {
                Socket s = null;
                try {
                    s = server.accept();
                    LOGGER.debug(logSocket(s));
                    atnaServer.execute(new WorkerThread(s));
                } catch (RuntimeException e) {
                    throw (e);
                } catch (SocketException e) {
                    LOGGER.debug("Socket closed.");
                    LOGGER.error("Socket Exception: {}", e.getMessage(), e);
                } catch (IOException e) {
                    SyslogException ex = new SyslogException(e.getMessage(), e);
                    atnaServer.notifyException(ex);
                } catch (Exception e) {
                    SyslogException ex = new SyslogException(e.getMessage(), e);
                    if (s != null) {
                        ex.setSourceIp(((InetSocketAddress) s.getRemoteSocketAddress()).getAddress().getHostAddress());
                    }
                    atnaServer.notifyException(ex);
                }
            }
        }

        private String logSocket(Socket socket) {
            InetSocketAddress local = (InetSocketAddress) socket.getLocalSocketAddress();
            InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            return "TCP data received from:" + socketAddress.getHostName() + ":" + socketAddress.getPort()
                    + " to:" + local.getHostName() + ":" + local.getPort();
        }
    }

    private class WorkerThread extends Thread {
        private Socket socket;

        private WorkerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream in = socket.getInputStream();
                while (true) {

                    byte[] b = new byte[128];
                    int count = 0;
                    while (count < b.length) {
                        int c = in.read();
                        if (c == -1) {
                            return;
                        }
                        if ((c & 0xff) == ' ') {
                            break;
                        }
                        b[count++] = (byte) c;
                    }
                    if (count > 0) {
                        int length;
                        try {
                            length = Integer.parseInt(new String(b, 0, count));
                            LOGGER.debug("length of incoming message: '{}'", length);
                        } catch (NumberFormatException e) {
                            SyslogException ex = new SyslogException(e, b);
                            ex.setSourceIp(((InetSocketAddress) socket.getRemoteSocketAddress())
                                    .getAddress().getHostAddress());
                            atnaServer.notifyException(ex);
                            break;
                        }
                        byte[] bytes = new byte[length];
                        int len = in.read(bytes);
                        while (len < length) {
                            int curr = in.read(bytes, len, bytes.length - len);
                            if (curr == -1) {
                                break;
                            }
                            len += curr;
                        }
                        LOGGER.debug("read in '{}' bytes to convert to message.", len);
                        SyslogMessage msg = null;
                        try {
                            msg = createMessage(bytes);
                        } catch (SyslogException e) {
                            e.setBytes(bytes);
                            e.setSourceIp(((InetSocketAddress) socket.getRemoteSocketAddress())
                                    .getAddress().getHostAddress());
                            atnaServer.notifyException(e);
                        }
                        if (msg != null) {
                            InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
                            msg.setSourceIp(addr.getAddress().getHostAddress());
                            atnaServer.notifyListeners(msg);
                        }
                    }
                }

            } catch (IOException e) {
                SyslogException ex = new SyslogException(e);
                ex.setSourceIp(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress());
                atnaServer.notifyException(ex);
            }
        }

        private SyslogMessage createMessage(byte[] bytes) throws SyslogException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("creating message from bytes: " + new String(bytes, StandardCharsets.UTF_8));
            }
            return SyslogMessageFactory.getFactory().read(new ByteArrayInputStream(bytes));
        }
    }
}
