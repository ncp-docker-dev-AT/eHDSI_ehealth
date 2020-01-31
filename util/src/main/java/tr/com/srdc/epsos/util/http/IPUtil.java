package tr.com.srdc.epsos.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import java.net.*;
import java.util.Enumeration;

public class IPUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(IPUtil.class);
    @SuppressWarnings("squid:S1313")
    private static final String LOCAL_IP_ADDRESS = "127.0.0.1";
    private static final String ERROR_UNKNOWN_HOST = "UNKNOWN_HOST";

    private IPUtil() {
    }

    /**
     * This returns the SERVER_IP value from the OpenNCP property database.
     *
     * @return IP Address of the server defined into the OpenNCP Configuration Database.
     */
    public static String getStaticServerIP() {
        return Constants.SERVER_IP;
    }

    /**
     * Returns Local IP address of the machine executing the method.
     *
     * @return IP address of the server or "UNKNOWN_HOST" if an IP cannot be retrieved.
     */
    public static String getPrivateServerIp() {

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()
                            && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("SocketException: Unable retrieve server IP address: '{}'", e.getMessage());
        }
        return ERROR_UNKNOWN_HOST;
    }

    /**
     * @param ipAddress
     * @return
     */
    public static boolean isLocalLoopbackIp(String ipAddress) {

        try {
            LOGGER.debug("Checking if Local IP: '{}'", ipAddress);
            LOGGER.debug("Canonical: '{}', Host Address: '{}', Host Name: '{}'", InetAddress.getByName(ipAddress).getCanonicalHostName(),
                    InetAddress.getByName(ipAddress).getHostAddress(), InetAddress.getByName(ipAddress).getHostName());
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            // Check if the address is a valid special local or loop back
            if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
                return true;
            }
        } catch (UnknownHostException e) {
            LOGGER.info("UnknownHostException: '{}'", e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * @param ipAddress
     * @return
     */
    public static boolean isLocalIp(String ipAddress) {

        try {
            //Check if the address is a loopback or any local address (i.e. 127.0.0.1 or localhost).
            if (isLocalLoopbackIp(ipAddress)) {
                return true;
            }
            // Check if the address is defined on any interface on the machine.
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return NetworkInterface.getByInetAddress(inetAddress) != null;

        } catch (UnknownHostException | SocketException e) {
            LOGGER.info("Exception: '{}'", e.getMessage());
            return false;
        }
    }
}
