package tr.com.srdc.epsos.ws.xcpd.client;

import ee.affecto.epsos.util.EventLogClientUtil;
import eu.epsos.dts.xcpd.PRPAIN201305UV022DTS;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.DynamicDiscoveryService;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.util.OidUtil;

import java.util.Locale;

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
     *
     * @param pd          the Patient Demographics object.
     * @param idAssertion the assertion.
     * @param countryCode The two-letter country code
     * @return a PRPAIN201306UV02 (XCPD Response) message.
     * @see PRPAIN201306UV02
     * @see PatientDemographics
     * @see Assertion
     * @see String
     */
    public static PRPAIN201306UV02 respondingGateway_PRPA_IN201305UV02(final PatientDemographics pd,
                                                                       final Assertion idAssertion,
                                                                       final String countryCode) {

        DynamicDiscoveryService dynamicDiscoveryService = new DynamicDiscoveryService();
        String endpointUrl = dynamicDiscoveryService.getEndpointUrl(countryCode.toLowerCase(Locale.ENGLISH),
                RegisteredService.PATIENT_IDENTIFICATION_SERVICE);

        String dstHomeCommunityId = OidUtil.getHomeCommunityId(countryCode.toLowerCase(Locale.ENGLISH));
        PRPAIN201305UV02 hl7Request = PRPAIN201305UV022DTS.newInstance(pd, dstHomeCommunityId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ClientConnector is trying to contact remote NCP-A:\nEndpoint: '{}'\nHomeCommunityId: '{}'",
                    endpointUrl, dstHomeCommunityId);
        }
        return sendRequest(endpointUrl, hl7Request, idAssertion, countryCode);
    }

    /**
     * @param endpointUrl
     * @param pRPAIN201305UV022
     * @param idAssertion
     * @param countryCode
     * @return
     */
    private static PRPAIN201306UV02 sendRequest(String endpointUrl, PRPAIN201305UV02 pRPAIN201305UV022,
                                                Assertion idAssertion, final String countryCode) {

        RespondingGateway_ServiceStub stub = new RespondingGateway_ServiceStub(endpointUrl);
        // Dummy handler for any mustUnderstand
        EventLogClientUtil.createDummyMustUnderstandHandler(stub);
        stub.setCountryCode(countryCode);

        return stub.respondingGateway_PRPA_IN201305UV02(pRPAIN201305UV022, idAssertion);
    }
}
