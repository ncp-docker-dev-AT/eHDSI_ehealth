package se.sb.epsos.web.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.shelob.ws.client.jaxws.PatientDemographics;
import se.sb.epsos.shelob.ws.client.jaxws.PatientId;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class PersonBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonBuilder.class);

    public static Person create(String id, String country, String fName, String lName, String sex, String birthDate) {
        return create(null, id, country, fName, lName, sex, birthDate);
    }

    public static Person create(String sessionId, String id, String country, String fName, String lName, String gender, String birthDate) {
        PatientDemographics patientDemographics = new PatientDemographics();

        PatientId patientId = new PatientId();
        patientId.setRoot("2.16.17.710.807.1000.990.1");
        patientId.setExtension(id);
        patientDemographics.getPatientId().add(patientId);
        patientDemographics.setGivenName(fName);
        patientDemographics.setFamilyName(lName);
        patientDemographics.setAdministrativeGender(gender);
        patientDemographics.setCountry(country);
        XMLGregorianCalendar cal = null;
        try {
            cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
        }
        cal.setYear(Integer.parseInt(birthDate.substring(0, 4)));
        cal.setMonth(Integer.parseInt(birthDate.substring(4, 6)));
        cal.setDay(Integer.parseInt(birthDate.substring(6, 8)));
        patientDemographics.setBirthDate(cal);
        return new Person(sessionId, patientDemographics, country);
    }

    public static Person create() {
        return create("191212121212", "SE", "Tolvan", "Tolvansson", "M", "19121212");
    }
}
