package eu.epsos.epsosxdrwsclient;

import eu.epsos.exceptions.XDRException;
import eu.epsos.protocolterminators.integrationtest.common.HCPIAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.ResourceLoader;
import eu.epsos.protocolterminators.integrationtest.common.TRCAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TestConstants;
import eu.epsos.pt.ws.client.xdr.XdrDocumentSource;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import org.junit.Ignore;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientDemographics.Gender;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.XdrRequest;

import java.text.ParseException;
import java.util.*;

/**
 * Test class for the XDR Document Submit Service.
 * For a successful run you must set EPSOS_PROPS_PATH containing epsos-srdc.properties.
 *
 * @author gareth
 */
@Ignore
public class XDRSubmitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(XDRSubmitTest.class);

    //parameters needed to run this test
    private static final String PATIENT_COUNTRY = TestConstants.PATIENT_COUNTRY;
    private static final String PATIENT_COUNTRY_NAME = TestConstants.PATIENT_COUNTRY_NAME;
    private static final String PATIENT_ID = TestConstants.PATIENT_ID;
    private static final String HOME_COMMUNITY_ID = TestConstants.HOME_CUMMUNITY_ID;
    private static final String CDA_EDISP_ID = TestConstants.CDA_EDISP_ID;
    private static final String CDA_EDISP_RESOURCE_NAME = TestConstants.CDA_EDISP_RESOURCE_NAME;


    public XDRSubmitTest() {
        TestConstants.checkEnvironmentVariables();
    }

    public static void main(String[] args) throws Exception {

        new XDRSubmitTest().doSubmit();
        LOGGER.info("+++++++++++++++++++++++ FINISHED! +++++++++++++++++++++++");
    }

    public void doSubmit() throws XDRException, ParseException {

        // build assertions
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARoleDeprecated.LICENSED_HCP);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion("", "");
        Map<AssertionEnum, Assertion> assertionMap = new EnumMap<>(AssertionEnum.class);
        assertionMap.put(AssertionEnum.CLINICIAN, idAssertion);
        assertionMap.put(AssertionEnum.TREATMENT, trcAssertion);

        // build patient id
        PatientId patientId = new PatientId();
        patientId.setRoot(HOME_COMMUNITY_ID);
        patientId.setExtension(PATIENT_ID);
        List<PatientId> patientIds = new ArrayList<>();
        patientIds.add(patientId);

        // build patient demographics
        PatientDemographics patientDemographics = new PatientDemographics();
        patientDemographics.setIdList(patientIds);
        patientDemographics.setAdministrativeGender(Gender.FEMALE);
        patientDemographics.setBirthDate(new Date());
        patientDemographics.setCity("Stockholm");
        patientDemographics.setCountry("Sweden");
        patientDemographics.setFamilyName("Mouse");
        patientDemographics.setGivenName("Mickey");
        patientDemographics.setPostalCode("123-45");
        patientDemographics.setStreetAddress("Disneyland");
        patientDemographics.setTelephone("0123-456789");

        //fetch CDA XML doc
        String cda = new ResourceLoader().getResource(CDA_EDISP_RESOURCE_NAME);

        // build request
        XdrRequest request = new XdrRequest();
        request.setCda(cda);
        request.setCdaId(CDA_EDISP_ID);
        request.setCountryCode(PATIENT_COUNTRY);
        request.setCountryName(PATIENT_COUNTRY_NAME);
        request.setPatient(patientDemographics);

        // call the service
        XdrDocumentSource.initialize(request, TestConstants.PATIENT_COUNTRY, assertionMap);
    }
}
