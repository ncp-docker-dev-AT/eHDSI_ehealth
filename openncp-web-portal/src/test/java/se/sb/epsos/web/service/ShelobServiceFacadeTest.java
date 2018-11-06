package se.sb.epsos.web.service;

import org.junit.Ignore;
import org.junit.Test;
import se.sb.epsos.shelob.ws.client.jaxws.ClientConnectorServiceService;

import static org.junit.Assert.assertNotNull;

public class ShelobServiceFacadeTest {

    /**
     * Should be ignored if server aint running. Used to see if you can access the wsdl
     */
    @Test
    @Ignore
    public void createClientConnectorTest() {

        ClientConnectorServiceService service = NcpClientConnector.createClientConnector();
        assertNotNull(service);
        NcpServiceFacadeImpl impl = new NcpServiceFacadeImpl();
        assertNotNull(impl);
    }
}
