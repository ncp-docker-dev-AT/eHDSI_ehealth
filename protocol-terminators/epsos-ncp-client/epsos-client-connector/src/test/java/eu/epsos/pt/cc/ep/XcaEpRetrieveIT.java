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
package eu.epsos.pt.cc.ep;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaExtraction;
import eu.epsos.protocolterminators.integrationtest.ihe.cda.CdaModel;
import eu.epsos.pt.cc.ClientGenericIT;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;

import javax.naming.NamingException;
import javax.xml.soap.SOAPElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements all the Integration Test for the XCA Retrieve operation for
 * Patient Service.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XcaEpRetrieveIT extends ClientGenericIT {

    /**
     * Folder under test/resources were are soap requests for this tests.
     */
    private static final String REQ_FOLDER = "xca/orderservice/";

    @BeforeClass
    public static void setUpClass() throws NamingException {
        ClientGenericIT.setUpClass();
        LOGGER.info("----------------------------");
        LOGGER.info(" Retrieve EP Documents");
        LOGGER.info("----------------------------");
    }

    /*
     * Normal usage
     */

    /**
     * This test performs an XCA Retrieve for EP, using valid Document
     * Identifiers. It is a simple test designed uniquely for testing the normal
     * work-flow of the XCA Retrieve.
     */
    @Test
    public void testRetrieveEP() {
        List<String> permissions = new ArrayList<>(2);
        permissions.add("4");
        permissions.add("10");
        assertions = getAssertions(permissions, REQ_FOLDER + "PT_CLIENT_XCA_EP_#9.xml", XSPARoleDeprecated.LICENSED_HCP);

        SOAPElement rspSoapMsg = testGood("PT_CLIENT_XCA_EP_#9", REQ_FOLDER + "PT_CLIENT_XCA_EP_#9.xml");
        validateCDA(rspSoapMsg, CdaExtraction.MessageType.PORTAL, CdaModel.EP_PIVOT);
    }
    /*
     * Invalid scenarios
     */

    /**
     * Error messages related to the creation of the document content. There may
     * be cases where failure may result in some elements of clinical
     * information missing for example in a patient summary. These clinical
     * content errors should be conveyed within the document content.
     */
    @Test
    @Ignore
    public void testRetrieveEPCIM() {
        testFailScenario("PT_CLIENT_XCA_EP_#9.1", "N/A", REQ_FOLDER + "PT_CLIENT_XCA_EP_#9.1.xml");
    }

    /*
     * Auxiliar methods
     */
    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(role);
    }

    protected Collection<Assertion> getAssertions(List<String> permissions, String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate("041082-995W^^^&1.2.246.21&amp;ISO", permissions, role);
    }
}
