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
package _2007.xds_b.iti.ihe.order;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARole;
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
public class XCA_Retrieve_OrderServiceIT extends ServerGenericIT {

    //    private static final String URL = CONFIG_SERVICE.getServiceWSE(COUNTRY_CODE, Constants.OrderService);
    private static final String QUERY_FILE = "xca/retrieve/order/xca-retrieve.xml";
    private static final String QUERY_FILE_INVALD_ID = "xca/retrieve/order/xca-retrieve-invalid-id.xml";
    private static final String QUERY_FILE_EMPTY_ID = "xca/retrieve/order/xca-retrieve-empty-id.xml";
    private static final String PATIENT_ID_ISO = "990000555^^^&2.16.840.1.113883.2.7.2.2&ISO";

    public XCA_Retrieve_OrderServiceIT() {
    }

    @BeforeClass
    public static void setUpClass() {
        LOGGER.info("----------------------------");
        LOGGER.info(" Retrieve Document");
        LOGGER.info("----------------------------");

        epr = DISCOVERY_SERVICE.getEndpointUrl(COUNTRY_CODE, RegisteredService.ORDER_SERVICE, false);
    }

    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARole role) {
        return hcpAndTrcAssertionCreate(PATIENT_ID_ISO, XSPARole.DEPRECATED_PHARMACIST);
    }

    /**
     * Test of respondingGateway_CrossGatewayRetrieve method, of class
     * XCA_ServiceSkeleton.
     */
    @Test
    public void testRetrieveDocument() {
        this.assertions = this.getAssertions(QUERY_FILE, XSPARole.DEPRECATED_PHARMACIST);
        testGood("testRetrieveDocument", QUERY_FILE);
    }

    @Ignore
    @Test
    public void testRetrieveInvalidDocument() {
        this.assertions = this.getAssertions(QUERY_FILE_INVALD_ID, XSPARole.DEPRECATED_PHARMACIST);
        testFail("testRetrieveInvalidDocument", "Failure", QUERY_FILE_INVALD_ID);
    }

    @Ignore
    @Test
    public void testRetrieveEmptyDocument() {
        this.assertions = this.getAssertions(QUERY_FILE_EMPTY_ID, XSPARole.DEPRECATED_PHARMACIST);
        testFail("testRetrieveEmptyDocument", "Failure", QUERY_FILE_EMPTY_ID);
    }
}
