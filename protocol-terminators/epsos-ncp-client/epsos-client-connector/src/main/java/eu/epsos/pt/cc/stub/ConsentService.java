package eu.epsos.pt.cc.stub;

import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.PatientDemographics;
import eu.epsos.exceptions.XDRException;
import eu.epsos.pt.cc.dts.axis2.XdrRequestDts;
import eu.epsos.pt.ws.client.xdr.XdrDocumentSource;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.XdrRequest;
import tr.com.srdc.epsos.data.model.XdrResponse;
import tr.com.srdc.epsos.util.Constants;

import java.text.ParseException;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class ConsentService {

    private ConsentService() {
    }

    /**
     * Notify the patient’s country of affiliation on a consent newly given or revoked in the country of care.
     * The consent status modification only applies to the country of care.
     * <p>
     * <br/> <dl> <dt><b>Preconditions</b> <dd>Service consumer and service provider share a common identifier
     * for the patient <dd>The patient has confirmed in the consent status change </dl>
     * <p>
     * <dl>
     * <dt><b>Fault Conditions</b>
     * <dd>Preconditions for a success scenario are not met
     * <dd>Security policy violation (e.g. the HCP’s role is not permitted to mediate consent changes)
     * <dd>A patient authentication is required (e.g. by signing the consent document)
     * <dd>Country-A legislation requires that a scanned consent document is provided with the request
     * <dd>Temporary failure (e.g. service provider is temporarily unable to access an internal service)
     * </dl>
     * <p>
     * <dl>
     * <dt><b>Warning Conditions</b>
     * <dd>Consent status change requests are not processed by the country of affiliation
     * <dd>Consent status changes are not applied automatically by the country of affiliation.
     * Therefore the consent status change will not be immediately operative.
     * </dl>
     *
     * @param document     document to be submitted
     * @param patient      patient's demographic data
     * @param hcpAssertion HCP Assertion
     * @param trcAssertion TRC Assertion
     * @throws ParseException
     */
    public static XdrResponse put(final EpsosDocument1 document, final PatientDemographics patient, final String countryCode,
                                  final Assertion hcpAssertion, final Assertion trcAssertion) throws XDRException, ParseException {
        XdrRequest request;
        request = XdrRequestDts.newInstance(document, patient, hcpAssertion, trcAssertion);
        request.setDocumentCode(eu.epsos.pt.cc.dts.GenericDocumentCodeDts.newInstance(document.getClassCode()));
        return XdrDocumentSource.provideAndRegisterDocSet(request, countryCode, Constants.CONSENT_CLASSCODE);
    }

    /**
     * Notify the patient’s country of affiliation on an erroneous consent
     * status change notification, in order to allow it to roll back any changes
     * made on its internal data that were triggered by the erroneous
     * notification.
     * <p>
     * <br/> <dl> <dt><b>Preconditions: </b> <dd>The service consumer has
     * previously triggered a consent change and is responsible for the consent
     * document that is to be discarded </dl> <dl> <dt><b>Fault Conditions: </b>
     * <dd>Preconditions for a success scenario are not met <dd>Country-A
     * legislation does not allow for discarding a consent; a new consent is
     * required <dd>The HCP was not the original mediator of the identified
     * consent document <dd>The identified document is not known <dd>Temporary
     * failure (e.g. service provider is temporarily unable to access an
     * internal service) </dl> <dl> <dt><b>Warning Conditions: </b> <dd>Consent
     * status change requests are not processed by the country of affiliation
     * <dd>Consent status changes are not rolled back automatically by the
     * country of affiliation </dl>
     * <p>
     * Not Implemented yet
     */
    public static XdrResponse discard() {
        throw new UnsupportedOperationException("Not implemented until epSOS v1.1");
    }
}
