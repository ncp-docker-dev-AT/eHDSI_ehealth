/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2012 SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
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
package _2007.xds_b.iti.ihe.mro;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.pt.server.it.ServerGenericIT;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;

import java.util.Collection;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XCA_Query_MroServiceIT extends ServerGenericIT {

    private static final String QUERY_FILE = "xca/query/mro/xca-query.xml";
    private static final String QUERY_FILE_INVALID_ID = "xca/query/mro/xca-query-invalid-id.xml";
    private static final String QUERY_FILE_EMPTY_ID = "xca/query/mro/xca-query-empty-id.xml";

    @BeforeClass
    public static void setUpClass() {
        LOGGER.info("----------------------------");
        LOGGER.info(" Query Documents (MRO)");
        LOGGER.info("----------------------------");

        epr = DISCOVERY_SERVICE.getEndpointUrl(COUNTRY_CODE, RegisteredService.ORDER_SERVICE, false);
    }

    /**
     * Test of respondingGateway_CrossGatewayQuery method, of class
     * XCA_ServiceSkeleton.
     */
    @Test
    public void testQueryDocuments() {
        this.assertions = this.getAssertions(QUERY_FILE, XSPARoleDeprecated.LICENSED_HCP);
        testGood("testQueryDocuments", QUERY_FILE);
    }

    @Test
    @Ignore
    public void testQueryDocumentInvalidId() {
        this.assertions = this.getAssertions(QUERY_FILE_INVALID_ID, XSPARoleDeprecated.LICENSED_HCP);
        testFail("testQueryInvalidDocument", "errorCode=\"1101\"", QUERY_FILE_INVALID_ID);
    }

    @Test
    @Ignore
    public void testQueryDocumentEmptyId() {
        this.assertions = this.getAssertions(QUERY_FILE_EMPTY_ID, XSPARoleDeprecated.LICENSED_HCP);
        testFail("testQueryDocumentEmptyId", "errorCode=\"1101\"", QUERY_FILE_EMPTY_ID);
    }

    @Override
    @Ignore
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(getPatientIdIso(requestPath), role);
    }
}
