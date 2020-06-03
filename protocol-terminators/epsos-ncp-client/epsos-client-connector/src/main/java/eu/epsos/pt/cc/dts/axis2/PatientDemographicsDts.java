package eu.epsos.pt.cc.dts.axis2;

import epsos.openncp.protocolterminator.clientconnector.PatientDemographics;

import java.util.*;

/**
 * This is an Data Transformation Service. This provide functions to transform data into a PatientDemographics object.
 *
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public final class PatientDemographicsDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private PatientDemographicsDts() {
    }

    /**
     * Converts a list of
     * {@link tr.com.srdc.epsos.data.model.PatientDemographics} into a list of
     * {@link epsos.openncp.protocolterminator.clientconnector.PatientDemographics}.
     *
     * @param patientDemList a list of
     *                       {@link tr.com.srdc.epsos.data.model.PatientDemographics} objects - to be transformed.
     * @return a list of
     * {@link epsos.openncp.protocolterminator.clientconnector.PatientDemographics}
     * objects - the result of the transformation.
     * @see PatientDemographics
     * @see tr.com.srdc.epsos.data.model.PatientDemographics
     * @see List
     */
    public static List<PatientDemographics> newInstance(final List<tr.com.srdc.epsos.data.model.PatientDemographics> patientDemList) {

        if (patientDemList == null) {
            return Collections.emptyList();
        }

        final List<PatientDemographics> result = new ArrayList<>(patientDemList.size());

        for (tr.com.srdc.epsos.data.model.PatientDemographics pd : patientDemList) {
            result.add(newInstance(pd));
        }

        return result;
    }

    /**
     * Converts a {@link tr.com.srdc.epsos.data.model.PatientDemographics} into a
     * {@link epsos.openncp.protocolterminator.clientconnector.PatientDemographics}.
     *
     * @param patientDemographics a {@link tr.com.srdc.epsos.data.model.PatientDemographics} object to be transformed.
     * @return a {@link epsos.openncp.protocolterminator.clientconnector.PatientDemographics} object - the result of the transformation.
     * @see PatientDemographics
     * @see tr.com.srdc.epsos.data.model.PatientDemographics
     */
    public static PatientDemographics newInstance(final tr.com.srdc.epsos.data.model.PatientDemographics patientDemographics) {

        if (patientDemographics == null) {
            return null;
        }

        final PatientDemographics result = PatientDemographics.Factory.newInstance();

        if (patientDemographics.getAdministrativeGender() != null) {
            result.setAdministrativeGender(patientDemographics.getAdministrativeGender().toString());
        }

        if (patientDemographics.getBirthDate() != null) {
            final Calendar calendar = new GregorianCalendar();
            calendar.setTime(patientDemographics.getBirthDate());
            result.setBirthDate(calendar);
        }
        result.setCity(patientDemographics.getCity());
        result.setCountry(patientDemographics.getCountry());
        result.setEmail(patientDemographics.getEmail());
        result.setFamilyName(patientDemographics.getFamilyName());
        result.setGivenName(patientDemographics.getGivenName());
        result.setPatientIdArray(PatientIdDts.newInstance(patientDemographics.getIdList()));
        result.setPostalCode(patientDemographics.getPostalCode());
        result.setStreetAddress(patientDemographics.getStreetAddress());
        result.setTelephone(patientDemographics.getTelephone());

        return result;
    }
}
