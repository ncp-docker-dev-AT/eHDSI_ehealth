package eu.epsos.epsosxcawsclient;

import eu.epsos.exceptions.XCAException;
import eu.epsos.protocolterminators.integrationtest.common.HCPIAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TRCAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TestConstants;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import org.junit.Ignore;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * Test class for the XCA Retrieve Service.
 * For a successful run you must set EPSOS_PROPS_PATH containing epsos-srdc.properties.
 *
 * @author gareth
 */

@Ignore
public class XCARetrieveTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(XCARetrieveTest.class);

    //parameters needed to run this test
    private static final String REPOSITORY_ID = TestConstants.REPOSITORY_ID;
    private static final String HOME_COMMUNITY_ID = TestConstants.HOME_CUMMUNITY_ID;
    private static final String DOCUMENT_ID = TestConstants.DOCUMENT_ID_PDF;
    private static final String PATIENT_COUNTRY = TestConstants.PATIENT_COUNTRY;

    public XCARetrieveTest() {
        TestConstants.checkEnvironmentVariables();
    }

    public static void main(String[] args) throws XCAException {
        new XCARetrieveTest().doRetrieve();
        LOGGER.info("+++++++++++++++++++++++ FINISHED! +++++++++++++++++++++++");
    }

    public void doRetrieve() throws XCAException {

        // build assertions
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARole.LICENSED_HCP);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion(HOME_COMMUNITY_ID, PATIENT_COUNTRY);
        Map<AssertionEnum, Assertion> assertionMap = new EnumMap<>(AssertionEnum.class);
        assertionMap.put(AssertionEnum.CLINICIAN, idAssertion);
        assertionMap.put(AssertionEnum.TREATMENT, trcAssertion);

        // build XDS document
        XDSDocument document = new XDSDocument();
        document.setDocumentUniqueId(DOCUMENT_ID);
        document.setRepositoryUniqueId(REPOSITORY_ID);

        // call the service
        DocumentResponse result = XcaInitGateway.crossGatewayRetrieve(document, HOME_COMMUNITY_ID, PATIENT_COUNTRY,
                TestConstants.TARGET_LANGUAGE, assertionMap, RegisteredService.PATIENT_SERVICE.getServiceName());

        printResult(result);
    }

    private void printResult(DocumentResponse result) {
        if (result != null) {
            if (result.getDocument() != null) {
                LOGGER.info("document: " + new String(result.getDocument(), StandardCharsets.UTF_8));
            }
        }
    }
}
