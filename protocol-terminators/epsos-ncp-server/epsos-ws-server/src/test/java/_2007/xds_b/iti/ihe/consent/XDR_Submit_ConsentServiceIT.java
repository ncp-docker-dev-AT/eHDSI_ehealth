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
package _2007.xds_b.iti.ihe.consent;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.XSPARoleDeprecated;
import eu.epsos.pt.server.it.ServerGenericIT;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;

import java.util.Collection;

/**
 * Integration test class for the XDR Submit Document service.
 *
 * @author gareth
 */
public class XDR_Submit_ConsentServiceIT extends ServerGenericIT {

    @BeforeClass
    public static void setUpClass() {
        LOGGER.info("----------------------------");
        LOGGER.info(" Submit Document Consent");
        LOGGER.info("----------------------------");

        epr = DISCOVERY_SERVICE.getEndpointUrl(COUNTRY_CODE, RegisteredService.CONSENT_SERVICE, false);
    }

    /**
     * Test of respondingGateway_ProvideAndRegisterDocumentSet method, of class XDR_ServiceSkeleton.
     * Expected result is a response with success status.
     */
    @Test
    public void testSubmitConsent() {
        this.assertions = this.getAssertions("xdr/consent/testSubmitConsent.xml", XSPARoleDeprecated.LICENSED_HCP);
        testGood("testSubmitConsent", "xdr/consent/testSubmitConsent.xml");
    }

    @Override
    protected Collection<Assertion> getAssertions(String requestPath, XSPARoleDeprecated role) {
        return hcpAndTrcAssertionCreate(role);
    }
}
