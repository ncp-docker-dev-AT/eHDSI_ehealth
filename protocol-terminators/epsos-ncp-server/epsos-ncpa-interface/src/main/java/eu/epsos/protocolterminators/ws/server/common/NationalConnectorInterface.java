package eu.epsos.protocolterminators.ws.server.common;

import org.w3c.dom.Element;

/**
 * Common interface for National Connector, to be extended by profile-specific interfaces
 */
public interface NationalConnectorInterface {

    /**
     * Submits the SOAP header to the national infrastructure. The method must be called from service implementations of IHE profiles in OpenNCP.
     * <p>
     * Information from the SOAP header may be used in national connectors for logging, decision making or as data to be included for national service calls.
     *
     * @param shElement DOM Element representing the SOAP header of the request message
     */
    void setSOAPHeader(Element shElement);
}
