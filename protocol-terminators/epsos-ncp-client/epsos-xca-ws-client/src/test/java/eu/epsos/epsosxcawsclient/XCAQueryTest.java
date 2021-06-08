package eu.epsos.epsosxcawsclient;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;
import eu.epsos.exceptions.XCAException;
import eu.epsos.protocolterminators.integrationtest.common.HCPIAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TRCAssertionCreator;
import eu.epsos.protocolterminators.integrationtest.common.TestConstants;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import eu.epsos.util.IheConstants;
import org.junit.Ignore;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocumentAssociation;
import tr.com.srdc.epsos.util.Constants;

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
    private static final String CLASSCODE = Constants.EP_CLASSCODE;
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
        Assertion idAssertion = HCPIAssertionCreator.createHCPIAssertion(XSPARole.LICENSED_HCP);
        Assertion trcAssertion = TRCAssertionCreator.createTRCAssertion(HOME_COMMUNITY_ID, PATIENT_ID);

        // build patientid
        PatientId patientId = new PatientId();
        patientId.setRoot(HOME_COMMUNITY_ID);
        patientId.setExtension(PATIENT_ID);

        // build GenericDocumentCode
        GenericDocumentCode classcode = new GenericDocumentCode();
        classcode.setSchema(CLASSCODE_SCHEMA);
        classcode.setValue(CLASSCODE);

        // call the service
        QueryResponse result = XcaInitGateway.crossGatewayQuery(
                patientId,
                PATIENT_COUNTRY,
                classcode,
                idAssertion,
                trcAssertion, Constants.PatientService);

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
