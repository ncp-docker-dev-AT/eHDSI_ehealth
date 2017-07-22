package eu.epsos.configmanager.test;

import epsos.ccd.gnomon.configmanager.ConfigurationManagerSMP;
import epsos.ccd.gnomon.configmanager.SMLSMPClient;
import epsos.ccd.gnomon.configmanager.SMLSMPClientException;
import eu.epsos.configmanager.database.HibernateConfigFile;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertNotNull;

@Ignore // TODO Fix tests
public class TestSMP {

    @Test
    public void testNormalFlow() {
        HibernateConfigFile.name = "src/test/resources/configmanager.hibernate.xml";
        String endpoint = ConfigurationManagerSMP.getInstance().getProperty("za.PatientIdentificationService.WSE");
        assertNotNull(endpoint);
    }

    /**
     * @throws SMLSMPClientException
     * @throws TransformerException
     * @throws IOException
     * @throws MalformedURLException
     * @throws CertificateException
     * @throws TechnicalException
     */
    @Test
    public void testClass() throws SMLSMPClientException, CertificateException, MalformedURLException, IOException,
            TransformerException, TechnicalException {
        try {
            SMLSMPClient client = new SMLSMPClient();
            client.lookup("pt", "ITI-55");
            assertNotNull(client.getCertificate());
            assertNotNull(client.getEndpointReference());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
