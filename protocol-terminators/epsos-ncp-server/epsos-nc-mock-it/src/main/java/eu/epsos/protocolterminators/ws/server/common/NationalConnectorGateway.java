package eu.epsos.protocolterminators.ws.server.common;

import org.w3c.dom.Element;

/**
 * Superclass for the DocumentSearch, PatientSearch and DocumentSubmit mock implementations.
 */
public class NationalConnectorGateway implements NationalConnectorInterface {

    //  SOAP Header from the message received by NCP-A.
    private Element soapHeader;

    /**
     * Returns SOAP Header.
     *
     * @return SOAP Header from NCP-A message received and passed to National Connector.
     */
    public Element getSOAPHeader() {
        return soapHeader;
    }

    /**
     * Submits the SOAP header to the national infrastructure. The method must be called from service implementations
     * of IHE profiles in OpenNCP.
     * Information from the SOAP header may be used in national connectors for logging, decision making or as data
     * to be included for national service calls.
     *
     * @param soapHeader DOM Element representing the SOAP header of the request message
     */
    @Override
    public void setSOAPHeader(Element soapHeader) {
        this.soapHeader = soapHeader;
    }
}
