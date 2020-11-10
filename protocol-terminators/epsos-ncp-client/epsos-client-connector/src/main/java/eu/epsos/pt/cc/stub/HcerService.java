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
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class HcerService {

    private HcerService() {
    }

    public static XdrResponse submit(final EpsosDocument1 document, final PatientDemographics patient, final String countryCode,
                                     final Assertion hcpAssertion, final Assertion trcAssertion) throws XDRException, ParseException {
        XdrRequest request;
        request = XdrRequestDts.newInstance(document, patient, hcpAssertion, trcAssertion);
        return XdrDocumentSource.provideAndRegisterDocSet(request, countryCode, Constants.HCER_CLASSCODE);
    }
}
