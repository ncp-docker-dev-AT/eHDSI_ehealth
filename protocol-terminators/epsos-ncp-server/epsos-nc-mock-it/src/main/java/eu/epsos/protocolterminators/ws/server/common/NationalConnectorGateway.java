package eu.epsos.protocolterminators.ws.server.common;

import org.w3c.dom.Element;

/**
 * Superclass for the DocumentSearch, PatientSearch and DocumentSubmit mock implementations.
 *
 * @author gareth
 */
public class NationalConnectorGateway implements NationalConnectorInterface {

    /**
     * Submits the SOAP header to the national infrastructure. The method must be called from service implementations
     * of IHE profiles in OpenNCP.
     * Information from the SOAP header may be used in national connectors for logging, decision making or as data
     * to be included for national service calls.
     *
     * @param shElement DOM Element representing the SOAP header of the request message
     */
    @Override
    public void setSOAPHeader(Element shElement) {
    }
}
