package eu.epsos.pt.server.it;

import eu.epsos.protocolterminators.integrationtest.common.AbstractIT;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ValidationReport;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.DynamicDiscoveryService;
import org.junit.Assert;
import org.w3c.dom.Document;

import javax.xml.soap.SOAPElement;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public abstract class ServerGenericIT extends AbstractIT {

    protected static final String CONF_MANAGER_FILE_PATH = System.getenv("EPSOS_PROPS_PATH") + "/integration/configmanager.hibernate.xml";
    /**
     * Aux instance of ConfigurationManagerService.
     */
    protected static final ConfigurationManager CONFIG_SERVICE = ConfigurationManagerFactory.getConfigurationManager();
    protected static final DynamicDiscoveryService DISCOVERY_SERVICE = new DynamicDiscoveryService();
    /**
     * Integration test fictional Country.
     */
    protected static final String COUNTRY_CODE = "zz";

    /**
     * This method will retrieve a ISO format patient id from a given
     * AdHocQueryRequest message.
     *
     * @param queryRequestPath the path for the message XML file
     * @return the ISO format Patient ID
     */
    protected static String getPatientIdIso(String queryRequestPath) {
        String pid;
        Document requestDoc = readDoc(queryRequestPath);
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            pid = xPath.evaluate("//*[local-name()='Slot' and @name='$XDSDocumentEntryPatientId'][1]/*[local-name()='ValueList']/*[local-name()='Value']", requestDoc);
        } catch (XPathExpressionException ex) {
            throw new RuntimeException(ex.getMessage(), ex.getCause());
        }

        return pid.substring(1, pid.length() - 1);
    }

    /**
     * @param testName Test name for logging purposes
     * @param expected expected error code
     * @param request  file with PRPA_IN201305UV02
     */
    protected void testFail(String testName, String expected, String request) {
        ValidationReport.cleanValidationDir(NcpSide.NCP_A);
        SOAPElement response;
        try {
            response = callService(request);
            Assert.assertNotNull(testName + ": response is not null", response);

        } catch (RuntimeException ex) {
            LOGGER.info(fail(testName));                                   // pretty status print to tests list
            Assert.fail(testName + ": " + ex.getLocalizedMessage());    // fail the test

            return;
        }

        String xml = soapElementToString(response);
        if (xml.contains(expected)) {
            LOGGER.info(success(testName));
        } else {
            LOGGER.info(fail(testName));
        }

        Assert.assertTrue(fail(testName), xml.contains(expected));
        ValidationReport.write(NcpSide.NCP_A, true);
    }

    @Override
    protected SOAPElement testGood(String testName, String request) {
        SOAPElement result;
        ValidationReport.cleanValidationDir(NcpSide.NCP_A);
        result = super.testGood(testName, request);
        ValidationReport.write(NcpSide.NCP_A, true);
        return result;
    }
}
