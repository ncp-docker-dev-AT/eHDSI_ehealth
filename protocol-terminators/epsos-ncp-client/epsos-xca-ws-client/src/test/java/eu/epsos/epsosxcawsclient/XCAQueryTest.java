package eu.epsos.epsosxcawsclient;

import eu.epsos.exceptions.XCAException;
import eu.epsos.protocolterminators.integrationtest.common.HCPIAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TRCAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TestConstants;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import eu.epsos.util.IheConstants;
import eu.europa.ec.sante.ehdsi.constant.ClassCode;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import org.junit.Ignore;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocumentAssociation;
import tr.com.srdc.epsos.util.Constants;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for the XCA Query Service. For a successful run you must set
 * EPSOS_PROPS_PATH containing epsos-srdc.properties.
 *
 * @author gareth
 */
@Ignore
public class XCAQueryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(XCAQueryTest.class);

    //parameters needed to run this test
    private static final String PATIENT_COUNTRY = TestConstants.PATIENT_COUNTRY;
    private static final String PATIENT_ID = TestConstants.PATIENT_ID;
    private static final ClassCode CLASSCODE = ClassCode.EP_CLASSCODE;
    private static final String CLASSCODE_SCHEMA = IheConstants.CLASSCODE_SCHEME;
    private static final String HOME_COMMUNITY_ID = TestConstants.HOME_CUMMUNITY_ID;

    public XCAQueryTest() {
        TestConstants.checkEnvironmentVariables();
    }

    public static void main(String[] args) throws XCAException {

        new XCAQueryTest().doQuery();
        LOGGER.info("+++++++++++++++++++++++ FINISHED! +++++++++++++++++++++++");
    }

    public void doQuery() throws XCAException {

        // build assertions
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARoleDeprecated.LICENSED_HCP);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion(HOME_COMMUNITY_ID, PATIENT_ID);
        Map<AssertionEnum, Assertion> assertionMap = new EnumMap<>(AssertionEnum.class);
        assertionMap.put(AssertionEnum.CLINICIAN, idAssertion);
        assertionMap.put(AssertionEnum.TREATMENT, trcAssertion);
        // build patient ID
        PatientId patientId = new PatientId();
        patientId.setRoot(HOME_COMMUNITY_ID);
        patientId.setExtension(PATIENT_ID);

        // build GenericDocumentCode
        GenericDocumentCode genericDocumentCode = new GenericDocumentCode();
        genericDocumentCode.setSchema(CLASSCODE_SCHEMA);
        genericDocumentCode.setValue(CLASSCODE.getCode());

        // call the service
        QueryResponse result = XcaInitGateway.crossGatewayQuery(
                patientId,
                PATIENT_COUNTRY,
                List.of(genericDocumentCode),
                null,
                assertionMap, Constants.PatientService);

        printResult(result);
    }

    private void printResult(QueryResponse result) {

        if (result != null) {
            if (result.getDocumentAssociations() != null) {
                for (XDSDocumentAssociation documentAssociation : result.getDocumentAssociations()) {
                    if (documentAssociation.getCdaXML() != null) {
                        LOGGER.info("documentUniqueId: '{}'", documentAssociation.getCdaXML().getDocumentUniqueId());
                    }
                    if (documentAssociation.getCdaPDF() != null) {
                        LOGGER.info("documentUniqueId: '{}'", documentAssociation.getCdaPDF().getDocumentUniqueId());
                    }
                }
            }
        }
    }
}
