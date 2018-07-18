package eu.epsos.protocolterminators.ws.server.xcpd.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.xcpd.PatientSearchInterfaceWithDemographics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.data.model.PatientDemographics.Gender;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Mock implementation of the PatientSearchInterface, to be replaced nationally.
 *
 * @author danielgronberg
 * @author Konstantin.Hypponen@kela.fi
 */
public class PatientSearchMockImpl extends NationalConnectorGateway implements PatientSearchInterfaceWithDemographics {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientSearchMockImpl.class);

    private static final String GENDER = "administrativeGender";
    private static final String BIRTH_DATE = "birthDate";
    private static final String BIRTH_DATE_YEAR = BIRTH_DATE + ".year";
    private static final String BIRTH_DATE_MONTH = BIRTH_DATE + ".month";
    private static final String BIRTH_DATE_DAY = BIRTH_DATE + ".day";
    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final String EMAIL = "email";
    private static final String FAMILY_NAME = "familyName";
    private static final String GIVEN_NAME = "givenName";
    private static final String POSTAL_CODE = "postalCode";
    private static final String STREET = "street";
    private static final String TELEPHONE = "telephone";

    @Override
    public String getPatientId(String citizenNumber) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<PatientDemographics> getPatientDemographics(List<PatientId> idList) {

        LOGGER.info("Searching patients at NI Mock...");

        List<PatientDemographics> result = new ArrayList<>(1);

        /*
         * Patient file
         */
        String patientFile = Constants.EPSOS_PROPS_PATH + "integration/";
        PatientId id = null;

        /* file path */
        for (PatientId aux : idList) {
            File rootDir = new File(patientFile + aux.getRoot());

            if (rootDir.exists()) {
                File extensionFile = new File(patientFile + aux.getRoot() + File.separator + aux.getExtension() + ".properties");

                if (extensionFile.exists()) {
                    patientFile += aux.getRoot() + File.separator + aux.getExtension() + ".properties";
                    id = aux;
                    break;
                }
            }
        }

        if (id == null) {
            LOGGER.info("Patient not found: " + idList.get(0));
            return new ArrayList<>(0);
        }

        /* read file */
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(patientFile));

            PatientDemographics patient = new PatientDemographics();
            patient.setIdList(Collections.singletonList(id));
            patient.setAdministrativeGender(Gender.parseGender(properties.getProperty(GENDER)));

            Calendar birth;
            Integer year = Integer.valueOf(properties.getProperty(BIRTH_DATE_YEAR));
            Integer month = Integer.valueOf(properties.getProperty(BIRTH_DATE_MONTH));
            Integer day = Integer.valueOf(properties.getProperty(BIRTH_DATE_DAY));
            birth = new GregorianCalendar(year, month, day);
            patient.setBirthDate(birth.getTime());

            patient.setCity(properties.getProperty(CITY));
            patient.setCountry(properties.getProperty(COUNTRY));
            patient.setEmail(properties.getProperty(EMAIL));
            patient.setFamilyName(properties.getProperty(FAMILY_NAME));
            patient.setGivenName(properties.getProperty(GIVEN_NAME));
            patient.setPostalCode(properties.getProperty(POSTAL_CODE));
            patient.setStreetAddress(properties.getProperty(STREET));
            patient.setTelephone(properties.getProperty(TELEPHONE));
            result.add(patient);
        } catch (Exception ex) {
            LOGGER.error(null, ex);
            return new ArrayList<>(0);
        }

        return result;
    }

    @Override
    public void setPatientDemographics(PatientDemographics pd) {
        LOGGER.info(pd.toString());
    }
}
