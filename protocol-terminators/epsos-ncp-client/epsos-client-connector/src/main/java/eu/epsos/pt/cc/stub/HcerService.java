package eu.epsos.pt.cc.stub;

import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.PatientDemographics;
import eu.epsos.exceptions.XDRException;
import eu.epsos.pt.cc.dts.axis2.XdrRequestDts;
import eu.epsos.pt.ws.client.xdr.XdrDocumentSource;
import eu.europa.ec.sante.ehdsi.constant.ClassCode;
import eu.europa.ec.sante.ehdsi.constant.assertion.AssertionEnum;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.XdrRequest;
import tr.com.srdc.epsos.data.model.XdrResponse;

import java.text.ParseException;
import java.util.Map;

/**
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class HcerService {

    private HcerService() {
    }

    public static XdrResponse submit(final EpsosDocument1 document, final PatientDemographics patient, final String countryCode,
                                     final Map<AssertionEnum, Assertion> assertionMap) throws XDRException, ParseException {
        XdrRequest request;
        request = XdrRequestDts.newInstance(document, patient);
        return XdrDocumentSource.provideAndRegisterDocSet(request, countryCode, assertionMap, ClassCode.HCER_CLASSCODE);
    }
}
