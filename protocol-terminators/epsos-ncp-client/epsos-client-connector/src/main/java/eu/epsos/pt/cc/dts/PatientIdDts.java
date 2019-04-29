package eu.epsos.pt.cc.dts;

import org.apache.commons.lang3.StringUtils;
import tr.com.srdc.epsos.data.model.PatientId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an Data Transformation Service. This provide functions to transform data into a PatientDemographics object.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public final class PatientIdDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private PatientIdDts() {
    }

    public static List<PatientId> newInstance(final epsos.openncp.protocolterminator.clientconnector.PatientId[] patientIdList) {

        if (patientIdList == null) {
            return Collections.emptyList();
        }

        List<PatientId> result = new ArrayList<>(patientIdList.length);
        for (epsos.openncp.protocolterminator.clientconnector.PatientId patientId : patientIdList) {
            result.add(newInstance(patientId));
        }

        return result;
    }

    public static PatientId newInstance(final epsos.openncp.protocolterminator.clientconnector.PatientId patientId) {

        if (patientId == null) {
            return null;
        }

        PatientId result = new PatientId();
        result.setRoot(StringUtils.trim(patientId.getRoot()));
        result.setExtension(StringUtils.trim(patientId.getExtension()));

        return result;
    }
}
