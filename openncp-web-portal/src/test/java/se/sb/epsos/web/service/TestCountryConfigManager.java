package se.sb.epsos.web.service;

import junit.framework.TestCase;
import se.sb.epsos.web.model.CountryVO;
import se.sb.epsos.web.model.PatientIdVO;

import java.util.List;


public class TestCountryConfigManager extends TestCase {

    public void testGetHomeCommunityId() {
        String test = CountryConfigManager.getHomeCommunityId("SE");
        assertNotNull(test);
        assertEquals(test, "2.16.17.710.807.1000.990.1");
    }

    public void testGetText() {
        CountryVO vo = new CountryVO("SE", "Sverige");
        String test = CountryConfigManager.getText(vo);
        assertNotNull(test);
        assertTrue(test.startsWith("Ange"));
    }

    public void testGetCountries() {
        List<CountryVO> countries = CountryConfigManager.getCountries();
        assertNotNull(countries);
        assertEquals("AT", countries.get(0).getId());
    }

    public void testGetPatientIdentifiers() {
        CountryVO se = new CountryVO("SE", "Sverige");
        List<PatientIdVO> patIds = CountryConfigManager.getPatientIdentifiers(se);
        assertNotNull(patIds);
        assertFalse(patIds.isEmpty());
        assertEquals(1, patIds.size());
        assertEquals("2.16.17.710.807.1000.990.1", patIds.get(0).getDomain());
        assertNull(patIds.get(0).getValue());
        assertEquals("patient.search.patient.svnr", patIds.get(0).getLabel());
    }
}
