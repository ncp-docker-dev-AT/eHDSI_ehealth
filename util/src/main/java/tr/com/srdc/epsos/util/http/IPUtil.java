package tr.com.srdc.epsos.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class IPUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(IPUtil.class);
    private static final String LOCAL_IP_ADDRESS = "127.0.0.1";

    private IPUtil() {
    }

    /**
     * This static method depends on the automation page of WhatIsMyIP.com
     *
     * @return IP Address of the server discovered by the remote service.
     */
    public static String getMyPublicIP() {

        URL autoIP;
        try {
            autoIP = new URL("http://automation.whatismyip.com/n09230945.asp");
        } catch (MalformedURLException e) {
            LOGGER.error("whatismyip.com was not accessible for learning our IP: '{}'", e.getMessage(), e);
            return LOCAL_IP_ADDRESS;
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIP.openStream()))) {

            return (in.readLine()).trim();

        } catch (IOException e) {
            LOGGER.error("whatismyip.com was not accessible for learning our IP: '{}'", e.getMessage(), e);
        }

        return LOCAL_IP_ADDRESS;
    }

    /**
     * This returns the SERVER_IP value from the OpenNCP property database.
     *
     * @return IP Address of the server defined into the OpenNCP Configuration Database.
     */
    public static String getServerIPStatic() {
        return Constants.SERVER_IP;
    }
}
