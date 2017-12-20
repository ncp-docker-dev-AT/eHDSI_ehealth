package eu.epsos.protocolterminators.ws.server.xcpd;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorInterface;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import org.opensaml.xml.io.MarshallingException;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.securityman.exceptions.InsufficientRightsException;

import java.util.List;

/**
 * This interface describes the National Connector API regarding Patient Identification Service.
 *
 * @author Konstantin Hypponen<code> - Konstantin.Hypponen@kela.fi</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public interface PatientSearchInterface extends NationalConnectorInterface {

    /**
     * Translates a National citizen number in an epSOS id.
     *
     * @param citizenNumber a valid citizen identifier
     * @return the citizen epSOS identifier
     */
    String getPatientId(String citizenNumber) throws NIException, InsufficientRightsException;

    /**
     * Searches the NI for all the patients that relates to the given <code>idList</code>.
     *
     * @param idList A set of patient's epSOS identifiers
     * @return A set of patient demographics
     */
    List<PatientDemographics> getPatientDemographics(List<PatientId> idList) throws NIException, InsufficientRightsException, MarshallingException;
}
