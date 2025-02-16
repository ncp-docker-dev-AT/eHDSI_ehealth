/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2012  SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact email: epsos@iuz.pt
 */
package eu.epsos.pt.cc.ps;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.pt.cc.ClientGenericIT;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements all the Integration Test for the XCA List operation for Patient
 * Service.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XcaPsListIT extends ClientGenericIT {

    /**
     * Folder under test/resources were are soap requests for this tests.
     */
    private static final String REQ_FOLDER = "xca/patientservice/";

    @BeforeClass
    public static void setUpClass() throws NamingException {
        ClientGenericIT.setUpClass();
        LOGGER.info("----------------------------");
        LOGGER.info(" Query PS Documents");
        LOGGER.info("----------------------------");
    }

    /*
     * Normal usage
     */

    /**
     * This test performs an XCA Query for PS, using valid Patient Identifiers.
     * It is a simple test designed uniquely for testing the normal work-flow of
     * the XCA query
     */
    @Test
    public void testQueryPS() {
        List<String> permissions = new ArrayList<>(4);
        permissions.add("3");
        permissions.add("5");
        permissions.add("10");
        permissions.add("16");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XCA_PS_#0.xml", XSPARoleDeprecated.LICENSED_HCP);

        testGood("PT_CLIENT_XCA_PS_#0", REQ_FOLDER + "PT_CLIENT_XCA_PS_#0.xml");
    }

    /*
     * Invalid scenarios
     * see D3.4.2 | 3.3.1.5 & 3.4.1.5
     */

    /**
     * (ERROR)
     * <p>
     * The patient has not given consent to the requested service.
     * <p>
     * Response Status: Failure, Message: No Consent, Code: 4701
     */
    @Test
    @Ignore
    public void testQueryPSNoConsent() {
        testFailScenario("PT_CLIENT_XCA_PS_#1", "4701", REQ_FOLDER + "PT_CLIENT_XCA_PS_#1.xml");
    }

    /**
     * (ERROR)
     * <p>
     * Country A requests a higher authentication trust level than assigned to
     * the HCP (e.g. password-based login not accepted for the requested
     * operation).
     * <p>
     * Response Status: Failure, Message: Weak, Authentication Code: 4702
     */
    @Test
    @Ignore
    public void testQueryPSLowAuth() {
        testFailScenario("PT_CLIENT_XCA_PS_#2", "4702", REQ_FOLDER + "PT_CLIENT_XCA_PS_#2.xml");
    }

    /**
     * (ERROR)
     * <p>
     * Either the security policy of country A or a privacy policy of the
     * patient (that was given in country A) does not allow the requested
     * operation to be performed by the HCP.
     * <p>
     * Response Status: Failure, Message: Insufficient Rights, Code: 4703
     */
    @Test
    public void testQueryPSInsRights() {
        List<String> permissions = new ArrayList<>(1);
        permissions.add("4");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XCA_PS_#3.xml", XSPARoleDeprecated.LICENSED_HCP);
        testFailScenario("PT_CLIENT_XCA_PS_#3", "4703", REQ_FOLDER + "PT_CLIENT_XCA_PS_#3.xml");
    }

    /**
     * (WARNING)
     * <p>
     * No patient summary is registered for the given patient.
     * <p>
     * Response Status: Success, Message: No Data, Code: 1102
     */
    @Test
    public void testQueryNoPS() {
        List<String> permissions = new ArrayList<>(4);
        permissions.add("3");
        permissions.add("5");
        permissions.add("10");
        permissions.add("16");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XCA_PS_#4.xml", XSPARoleDeprecated.LICENSED_HCP);
        testFailScenario("PT_CLIENT_XCA_PS_#4", "1102", REQ_FOLDER + "PT_CLIENT_XCA_PS_#4.xml");
    }

    /**
     * (INFO)
     * <p>
     * If PDF-coded patient summary is requested: Country A does not provide the
     * (optional) source coded version of the patient summary.
     * <p>
     * Response Status: Success, Message: Unsupported Feature, Code: 4201
     */
    @Test
    @Ignore
    public void testQueryPDFcoded() {
        testFailScenario("PT_CLIENT_XCA_PS_#5", "4201", REQ_FOLDER + "PT_CLIENT_XCA_PS_#5.xml");
    }

    /**
     * (ERROR)
     * <p>
     * The query argument slots used by the service consumer are not supported
     * by the service provider.
     * <p>
     * Response Status: Failure, Message: Unknown Signifier, Code: 4202
     */
    @Test
    @Ignore
    public void testQueryPSUnsSlots() {
        testFailScenario("PT_CLIENT_XCA_PS_#6", "4202", REQ_FOLDER + "PT_CLIENT_XCA_PS_#6.xml");
    }

    /**
     * (ERROR)
     * <p>
     * The requested encoding cannot be provided due to a transcoding error.
     * <p>
     * Response Status: Failure, Message: Transcoding Error, Code: 4203
     */
    @Test
    @Ignore
    public void testQueryPSTranscErr() {
        testFailScenario("PT_CLIENT_XCA_PS_#7", "4203", REQ_FOLDER + "PT_CLIENT_XCA_PS_#7.xml");
    }

    /**
     * (ERROR).
     * <p>
     * The service provider is unable to evaluate the given argument.
     * <p>
     * Response Status: Failure, Message: Unknown Filters, Code: 4204
     */
    @Test
    @Ignore
    public void testQueryPSInvArgs() {
        testFailScenario("PT_CLIENT_XCA_PS_#8", "4204", REQ_FOLDER + "PT_CLIENT_XCA_PS_#8.xml");
    }

    /*
     * Additional Invalid scenarios
     */

    /**
     * Not all of the requested encodings are provided (e.g. due to inability to
     * transcode a certain national code).
     */
    @Test
    @Ignore
    public void testQueryPSPartTrnscSucc() {
        testFailScenario("PT_CLIENT_XCA_PS_#7.1", "4101", REQ_FOLDER + "PT_CLIENT_XCA_PS_#7.1.xml");
    }

    /**
     * The HCP MUST consider additionally the source coded document because it
     * MAY contain information that is not included in the epSOS pivot CDA .
     * Reason: field were nullified due to missing code mappings!
     */
    @Test
    @Ignore
    public void testQueryPSConsiderAddDoc() {
        testFailScenario("PT_CLIENT_XCA_PS_#7.2", "2102", REQ_FOLDER + "PT_CLIENT_XCA_PS_#7.2.xml");
    }


    /*
     * Auxiliar methods
     */
    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(getPatientIdIso(requestPath), role);
    }

    protected Collection<Assertion> getAssertions(List<String> permissions, String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(getPatientIdIso(requestPath), permissions, role);
    }
}
