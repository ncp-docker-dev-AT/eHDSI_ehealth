package tr.com.srdc.epsos.ws.xcpd.client;

import ee.affecto.epsos.util.EventLogClientUtil;
import eu.epsos.dts.xcpd.PRPAIN201305UV022DTS;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.util.OidUtil;

import java.util.Locale;
import java.util.Map;

/**
 * RespondingGateway_RequestSender class.
 * <p>
 * Contains the necessary operations to build a XCPD request and to send it to the NCP-A.
 *
 * @author SRDC <code> - epsos@srdc.com.tr</code>
 * @author Aarne Roosi<code> - Aarne.Roosi@Affecto.com</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public final class RespondingGateway_RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RespondingGateway_RequestSender.class);

    private RespondingGateway_RequestSender() {
    }

    /**
     * Builds and sends a PRPA_IN201305UV02 HL7 message, representing an XCPD Request process.
     */
    public static PRPAIN201306UV02 respondingGateway_PRPA_IN201305UV02(final PatientDemographics patientDemographics,
                                                                       final Map<AssertionEnum, Assertion> assertionMap,
                                                                       final String countryCode) {

        var dynamicDiscoveryService = new DynamicDiscoveryService();
        String endpointUrl = dynamicDiscoveryService.getEndpointUrl(countryCode.toLowerCase(Locale.ENGLISH),
                RegisteredService.PATIENT_IDENTIFICATION_SERVICE);

        String dstHomeCommunityId = OidUtil.getHomeCommunityId(countryCode.toLowerCase(Locale.ENGLISH));
        var hl7Request = PRPAIN201305UV022DTS.newInstance(patientDemographics, dstHomeCommunityId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ClientConnector is trying to contact remote NCP-A:\nEndpoint: '{}'\nHomeCommunityId: '{}'",
                    endpointUrl, dstHomeCommunityId);
        }
        return sendRequest(endpointUrl, hl7Request, assertionMap, countryCode);
    }

    private static PRPAIN201306UV02 sendRequest(String endpointUrl, PRPAIN201305UV02 pRPAIN201305UV022,
                                                Map<AssertionEnum, Assertion> assertionMap, final String countryCode) {

        var respondingGatewayServiceStub = new RespondingGateway_ServiceStub(endpointUrl);
        // Dummy handler for any mustUnderstand
        EventLogClientUtil.createDummyMustUnderstandHandler(respondingGatewayServiceStub);
        respondingGatewayServiceStub.setCountryCode(countryCode);

        return respondingGatewayServiceStub.respondingGateway_PRPA_IN201305UV02(pRPAIN201305UV022, assertionMap);
    }
}
