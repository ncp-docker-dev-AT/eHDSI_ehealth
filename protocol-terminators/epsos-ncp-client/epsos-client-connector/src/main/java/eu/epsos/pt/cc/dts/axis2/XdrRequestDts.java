package eu.epsos.pt.cc.dts.axis2;

import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.PatientDemographics;
import tr.com.srdc.epsos.data.model.XdrRequest;
import tr.com.srdc.epsos.util.Constants;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XdrRequestDts {

    private XdrRequestDts() {
    }

    public static XdrRequest newInstance(final EpsosDocument1 document, final PatientDemographics patient) throws ParseException {

        if (document == null) {
            return null;
        }
        XdrRequest result = new XdrRequest();
        //  TODO: Review if the GenericDocumentCode is required at this level.
        result.setCda(new String(document.getBase64Binary(), StandardCharsets.UTF_8));
        result.setCdaId(document.getUuid());
        result.setSubmissionSetId(document.getSubmissionSetId());
        result.setPatient(eu.epsos.pt.cc.dts.PatientDemographicsDts.newInstance(patient));
        result.setCountryCode(Constants.COUNTRY_CODE);
        result.setCountryName(Constants.COUNTRY_NAME);

        return result;
    }
}
