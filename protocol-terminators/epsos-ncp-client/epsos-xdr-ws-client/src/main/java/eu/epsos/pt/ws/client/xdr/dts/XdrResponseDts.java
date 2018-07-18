package eu.epsos.pt.ws.client.xdr.dts;

import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import tr.com.srdc.epsos.data.model.XdrResponse;

/**
 * This is an Data Transformation Service. This provide functions to transform data into a XdrResponse object.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class XdrResponseDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private XdrResponseDts() {
    }

    public static XdrResponse newInstance(RegistryResponseType registryResponse) {

        final XdrResponse result = new XdrResponse();

        if (registryResponse.getStatus() != null) {
            result.setResponseStatus(registryResponse.getStatus());
        }

        return result;
    }
}
