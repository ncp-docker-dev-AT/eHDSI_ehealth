package eu.epsos.epsosxcpdwsclient;

import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.protocolterminators.integrationtest.common.HCPIAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TestConstants;
import eu.epsos.pt.ws.client.xcpd.XcpdInitGateway;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;
import eu.europa.ec.sante.ehdsi.openncp.util.AssertionEnum;
import org.junit.Ignore;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientId;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for invoking the XCPD Web Services (epsos-xcpd-ws-server) using the epsos-xcpd-ws-client. For a successful
 * run you must set EPSOS_PROPS_PATH containing epsos-srdc.properties.
 *
 * @author gareth
 */

@Ignore
public class XCPDTest {

    private static final Logger logger = LoggerFactory.getLogger(XCPDTest.class);

    //parameters needed to run this test
    private static final String PATIENT_COUNTRY = TestConstants.PATIENT_COUNTRY;
    private static final String PATIENT_ID = TestConstants.PATIENT_ID;
    private static final String HOME_COMMUNITY_ID = TestConstants.HOME_CUMMUNITY_ID;

    public XCPDTest() {
        TestConstants.checkEnvironmentVariables();
    }

    public static void main(String[] args) {

        new XCPDTest().getPatientDemographics();
        logger.info("+++++++++++++++++++++++ FINISHED! +++++++++++++++++++++++");
    }

    public void getPatientDemographics() {

        PatientDemographics patientDemographics = new PatientDemographics();

        // build patient Id
        PatientId patientId = new PatientId();
        patientId.setRoot(HOME_COMMUNITY_ID);
        patientId.setExtension(PATIENT_ID);
        List<PatientId> patientIds = new ArrayList<>();
        patientIds.add(patientId);
        patientDemographics.setIdList(patientIds);

        // countryId
        patientDemographics.setCountry(PATIENT_COUNTRY);

        // assertions
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARole.LICENSED_HCP);
        Map<AssertionEnum, Assertion> assertionEnumMap = new EnumMap<>(AssertionEnum.class);
        assertionEnumMap.put(AssertionEnum.CLINICIAN, idAssertion);
        // Call the service
        try {
            List<PatientDemographics> result;
            result = XcpdInitGateway.patientDiscovery(patientDemographics, assertionEnumMap, PATIENT_COUNTRY);
            logger.info("result: '{}'", result);

        } catch (NoPatientIdDiscoveredException ex) {
            logger.error(null, ex);
        }
    }
}
