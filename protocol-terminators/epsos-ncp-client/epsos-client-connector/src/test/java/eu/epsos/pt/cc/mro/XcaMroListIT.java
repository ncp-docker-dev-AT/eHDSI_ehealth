/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2013  SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
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
package eu.epsos.pt.cc.mro;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.pt.cc.ClientGenericIT;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements all the Integration Test for the XCA Retrieve operation for MRO
 * Service.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class XcaMroListIT extends ClientGenericIT {

    /**
     * Folder under test/resources were are soap requests for this tests.
     */
    private static final String REQ_FOLDER = "xca/mroservice/";

    @BeforeClass
    public static void setUpClass() throws NamingException {
        ClientGenericIT.setUpClass();
        LOGGER.info("----------------------------");
        LOGGER.info(" Query MRO Documents");
        LOGGER.info("----------------------------");
    }

    /*
     * Normal usage
     */

    /**
     * This test performs an XCA Query for MRO, using valid Patient Identifiers.
     * It is a simple test designed uniquely for testing the normal work-flow of
     * the XCA query.
     */
    @Test
    public void testQueryMro() {
        List<String> permissions = new ArrayList<>(2);
        permissions.add("4");
        permissions.add("10");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XCA_MRO_#0.xml", XSPARoleDeprecated.LICENSED_HCP);

        testGood("PT_CLIENT_XCA_MRO_#0", REQ_FOLDER + "PT_CLIENT_XCA_MRO_#0.xml");
    }

    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(getPatientIdIso(requestPath), role);
    }

    protected Collection<Assertion> getAssertions(List<String> permissions, String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(getPatientIdIso(requestPath), permissions, role);
    }
}
