package eu.epsos.configmanager.test;

import epsos.ccd.gnomon.configmanager.ConfigurationManagerSMP;
import epsos.ccd.gnomon.configmanager.SMLSMPClient;
import epsos.ccd.gnomon.configmanager.SMLSMPClientException;
import eu.epsos.configmanager.database.HibernateConfigFile;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertNotNull;

public class TestSMP {

    private static final String ns = "http://busdox.org/serviceMetadata/publishing/1.0/";

    /* Joao: I commented this because I was having errors after pulling and AFAIK,
    log4j was dropped in favour of logback. We can analyse this after merging the branches */
    //	@BeforeClass
    //	public static void setup() {
    //		Logger rootLogger = Logger.getRootLogger();
    //		if (!rootLogger.getAllAppenders().hasMoreElements()) {
    //			rootLogger.setLevel(Level.OFF);
    //
    //			Logger hornetLogger = rootLogger.getLoggerRepository().getLogger("org.hornetq.core.server");
    //			hornetLogger.setLevel(Level.OFF);
    //			hornetLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
    //
    //		}
    //	}

    @Test
    public void testNormalFlow() {
        HibernateConfigFile.name = "src/test/resources/massi.hibernate.xml";
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
