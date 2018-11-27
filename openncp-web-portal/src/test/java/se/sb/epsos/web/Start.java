package se.sb.epsos.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {

    private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        
        Server server = new Server();
        SocketConnector connector = new SocketConnector();

        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath("/");
        bb.setWar("src/main/webapp");

        // START JMX SERVER
        // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
        // server.getContainer().addEventListener(mBeanContainer);
        // mBeanContainer.start();

        server.setHandler(bb);

        try {
            LOGGER.info(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
            server.start();
            System.in.read();
            LOGGER.info(">>> STOPPING EMBEDDED JETTY SERVER");
            // while (System.in.available() == 0) {
            //   Thread.sleep(5000);
            // }
            server.stop();
            server.join();
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
            System.exit(100);
        }
    }
}
