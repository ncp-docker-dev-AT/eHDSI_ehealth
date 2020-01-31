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
    private final Logger logger = LoggerFactory.getLogger(PatientSearchMockImpl.class);

    @Override
    public String getPatientId(String citizenNumber) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<PatientDemographics> getPatientDemographics(List<PatientId> idList) {

        List<PatientDemographics> result = new ArrayList<>(1);

        // Identifying mocked Patient File.
        StringBuilder patientFile = new StringBuilder();
        patientFile.append(Constants.EPSOS_PROPS_PATH).append("integration/");
        PatientId id = null;

        // Building Path File.
        for (PatientId patientId : idList) {
            logger.info("[National Infrastructure Mock] Searching patients with ID: '{}' and Assigning Authority: '{}'", patientId.getExtension(), patientId.getRoot());
            File rootDir = new File(patientFile + patientId.getRoot());

            if (rootDir.exists()) {
                File extensionFile = new File(patientFile + patientId.getRoot() + File.separator + patientId.getExtension() + ".properties");

                if (extensionFile.exists()) {
                    patientFile.append(patientId.getRoot()).append(File.separator).append(patientId.getExtension()).append(".properties");
                    id = patientId;
                    break;
                }
            }
        }

        if (id == null) {
            logger.info("[National Infrastructure Mock] Patient with ID: '{}' not found: ", idList.get(0));
            return new ArrayList<>(0);
        }

        // Load Patient properties file.
        Properties properties = new Properties();
        try {
            logger.info("[National Infrastructure Mock] Loading Patient information from filesystem resource: '{}'", patientFile.toString());
            properties.load(new FileInputStream(patientFile.toString()));

            PatientDemographics patient = new PatientDemographics();
            patient.setIdList(Collections.singletonList(id));
            patient.setAdministrativeGender(Gender.parseGender(properties.getProperty(GENDER)));
            int year = Integer.parseInt(properties.getProperty(BIRTH_DATE_YEAR));
            int month = Integer.parseInt(properties.getProperty(BIRTH_DATE_MONTH));
            int day = Integer.parseInt(properties.getProperty(BIRTH_DATE_DAY));
            Calendar birth = Calendar.getInstance();
            birth.set(Calendar.DAY_OF_MONTH, day);
            birth.set(Calendar.YEAR, year);
            birth.set(Calendar.MONTH, month);
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
            logger.info("[National Infrastructure Mock] Patient with ID: '{}' found.", id.getfullId());

        } catch (Exception e) {
            logger.error("[National Infrastructure Mock] Patient Not Found Exception: '{}'", e.getMessage(), e);
            return new ArrayList<>(0);
        }

        return result;
    }

    @Override
    public void setPatientDemographics(PatientDemographics patientDemographics) {

        if (logger.isDebugEnabled()) {
            logger.debug(patientDemographics.toString());
        }
    }
}
