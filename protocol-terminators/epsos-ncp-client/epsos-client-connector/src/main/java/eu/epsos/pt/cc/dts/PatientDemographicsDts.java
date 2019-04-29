package eu.epsos.pt.cc.dts;

import org.apache.commons.lang3.StringUtils;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientDemographics.Gender;

import java.text.ParseException;

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
     * Converts a QueryPatientDocument object into a PatienDemographics Object.
     *
     * @param patientDemographics representing a QueryPatientDocument.
     * @return a PatientDemographics object.
     * @throws ParseException
     * @see PatientDemographics
     * @see epsos.openncp.protocolterminator.clientconnector.QueryPatientDocument
     */
    public static PatientDemographics newInstance(final epsos.openncp.protocolterminator.clientconnector.PatientDemographics patientDemographics) throws ParseException {

        if (patientDemographics == null) {
            return null;
        }

        final PatientDemographics result = new PatientDemographics();

        if (StringUtils.isNotBlank(patientDemographics.getAdministrativeGender())) {
            result.setAdministrativeGender(Gender.parseGender(StringUtils.trim(patientDemographics.getAdministrativeGender())));
        }
        if (patientDemographics.getBirthDate() != null) {
            result.setBirthDate(patientDemographics.getBirthDate().getTime());
        }
        if (StringUtils.isNotBlank(patientDemographics.getCity())) {
            result.setCity(StringUtils.trim(patientDemographics.getCity()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getCountry())) {
            result.setCountry(StringUtils.trim(patientDemographics.getCountry()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getEmail())) {
            result.setEmail(StringUtils.trim(patientDemographics.getEmail()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getFamilyName())) {
            result.setFamilyName(StringUtils.trim(patientDemographics.getFamilyName()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getGivenName())) {
            result.setGivenName(StringUtils.trim(patientDemographics.getGivenName()));
        }
        if (patientDemographics.getPatientIdArray() != null) {
            result.setIdList(PatientIdDts.newInstance(patientDemographics.getPatientIdArray()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getPostalCode())) {
            result.setPostalCode(StringUtils.trim(patientDemographics.getPostalCode()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getStreetAddress())) {
            result.setStreetAddress(StringUtils.trim(patientDemographics.getStreetAddress()));
        }
        if (StringUtils.isNotBlank(patientDemographics.getTelephone())) {
            result.setTelephone(StringUtils.trim(patientDemographics.getTelephone()));
        }

        return result;
    }
}
