package eu.epsos.pt.ws.client.xcpd;

import eu.epsos.dts.xcpd.RespondingGateway_RequestReceiver;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.europa.ec.sante.openncp.protocolterminator.commons.AssertionEnum;
import org.hl7.v3.PRPAIN201306UV02;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.ws.xcpd.client.RespondingGateway_RequestSender;

import java.util.List;
import java.util.Map;

/**
 * XCPD Initiating Gateway
 * <p>
 * This is an implementation of a IHE XCPD Initiation Gateway. This class provides the necessary operations to perform
 * PatientDiscovery.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class XcpdInitGateway {

    /**
     * Private constructor to disable class instantiation.
     */
    private XcpdInitGateway() {
    }

    /**
     * Performs a Patient Discovery for the given Patient Demographics.
     *
     * @param patientDemographics the Patient Demographics set to be used in the request.
     * @param assertionMap        HCP identity assertion.
     * @param countryCode         country code - ISO 3166-1 alpha-2
     * @return a List of matching Patient Demographics, each representing a patient person.
     * @throws NoPatientIdDiscoveredException contains the error message
     */
    public static List<PatientDemographics> patientDiscovery(final PatientDemographics patientDemographics,
                                                             final Map<AssertionEnum, Assertion> assertionMap,
                                                             final String countryCode) throws NoPatientIdDiscoveredException {

        PRPAIN201306UV02 response = RespondingGateway_RequestSender.respondingGateway_PRPA_IN201305UV02(patientDemographics, assertionMap, countryCode);
        return RespondingGateway_RequestReceiver.respondingGateway_PRPA_IN201306UV02(response);
    }
}
