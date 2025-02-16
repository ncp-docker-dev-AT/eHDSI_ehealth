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
package eu.epsos.pt.cc.ed;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaExtraction;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaModel;
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
 * Implements all the Integration Test for the XDR operations for eDispensation
 * Service.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class XdrEdIT extends ClientGenericIT {

    private static final String REQ_FOLDER = "xdr/dispensationservice/";

    @BeforeClass
    public static void setUpClass() throws NamingException {
        ClientGenericIT.setUpClass();
        LOGGER.info("----------------------------");
        LOGGER.info(" Submit eD Documents");
        LOGGER.info("----------------------------");
    }
    /*
     * Test cases
     */
    /*
     * Normal Usage
     */

    /**
     * This test performs a simple submitting action for a eDispensation.
     * <p>
     * It is a simple test designed uniquely for testing the normal work-flow of
     * the XDR.
     */
    @Test
    public void testSubmitED() {
        List<String> permissions = new ArrayList<>(1);
        permissions.add("46");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XDR_ED_#0.xml", XSPARoleDeprecated.LICENSED_HCP);

        testGood("PT_CLIENT_XDR_ED_#0", REQ_FOLDER + "PT_CLIENT_XDR_ED_#0.xml");
        validateCDA(REQ_FOLDER + "PT_CLIENT_XDR_ED_#0.xml", CdaExtraction.MessageType.PORTAL, CdaModel.ED_FRIENDLY);
    }

    /*
     * Invalid Scenarios
     */

    /**
     * (ERROR)
     * <p>
     * No matching ePrescription was found.
     * <p>
     * Response Status: Failure, Message: No Match, Code: 4105
     */
    @Test
    public void testSubmitNoEP() {
        List<String> permissions = new ArrayList<>(1);
        permissions.add("46");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XDR_ED_#1.xml", XSPARoleDeprecated.LICENSED_HCP);

        testFailScenario("PT_CLIENT_XDR_ED_#1", "4105", REQ_FOLDER + "PT_CLIENT_XDR_ED_#1.xml");
    }

    /**
     * (ERROR)
     * <p>
     * ePrescription has already been dispensed.
     * <p>
     * Response Status: Failure Message: Invalid Dispensation Code: 4106
     */
    @Test
    public void testSubmitDispEP() {
        List<String> permissions = new ArrayList<>(1);
        permissions.add("46");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XDR_ED_#2.xml", XSPARoleDeprecated.LICENSED_HCP);

        testFailScenario("PT_CLIENT_XDR_ED_#2", "4106", REQ_FOLDER + "PT_CLIENT_XDR_ED_#2.xml");
    }

    /**
     * (ERROR)
     * <p>
     * Country A requests a higher authentication trust level than assigned to
     * the HCP (e.g. password-based login is not accepted for the requested
     * operation).
     * <p>
     * Response Status: Failure, Message: Weak Authentication, Code: 4702
     */
    @Test
    @Ignore
    public void testSubmitLowAuth() {
        testFailScenario("PT_CLIENT_XDR_ED_#3", "4702", REQ_FOLDER + "PT_CLIENT_XDR_ED_#3.xml");
    }

    /**
     * (ERROR)
     * <p>
     * The eDispensation service provider only accepts dispensation data that is
     * digitally signed by an HCP.
     * <p>
     * Response Status: Failure, Message: No Signature, Code: 4704
     */
    @Test
    @Ignore
    public void testSubmitNoSign() {
        testFailScenario("PT_CLIENT_XDR_ED_#4", "4704", REQ_FOLDER + "PT_CLIENT_XDR_ED_#4.xml");
    }

    /**
     * (ERROR)
     * <p>
     * The service consumer did not provide the source coded PDF document for an
     * eDispensation.
     * <p>
     * Response Status: Failure, Message: Original data missing, Code: 4107
     */
    @Test
    @Ignore
    public void testSubmitNoPDF() {
        testFailScenario("PT_CLIENT_XDR_ED_#5", "4107", REQ_FOLDER + "PT_CLIENT_XDR_ED_#5.xml");
    }

    /**
     * (ERROR)
     * <p>
     * The service consumer did not provide the epSOS pivot coded document for
     * an eDispensation.
     * <p>
     * Response Status: Failure, Message: Original data missing, Code: 4108
     */
    @Test
    @Ignore
    public void testSubmitNoDoc() {
        List<String> permissions = new ArrayList<>(1);
        permissions.add("46");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XDR_ED_#6.xml", XSPARoleDeprecated.LICENSED_HCP);
        testFailScenario("PT_CLIENT_XDR_ED_#6", "4108", REQ_FOLDER + "PT_CLIENT_XDR_ED_#6.xml");
    }

    /*
     * Auxiliar Methods
     */
    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(role);
    }

    /**
     * Allows the customization of specific assertions.
     *
     * @param permissions
     * @return a Collection of customized assertions.
     */
    protected Collection<Assertion> getAssertions(List<String> permissions, String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate("", permissions, role);
    }
}
