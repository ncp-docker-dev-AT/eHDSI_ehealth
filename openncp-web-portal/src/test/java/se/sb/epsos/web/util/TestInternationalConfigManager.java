package se.sb.epsos.web.util;

import org.junit.Before;
import org.junit.Test;
import se.sb.epsos.web.model.CountryVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestInternationalConfigManager {

    private InternationalConfigManager config;
    private CountryVO vo;

    @Before
    public void setUp() {
        System.setProperty("epsos-internationalsearch-config-path", "src/test/resources/");
        List<CountryVO> list = new ArrayList<>();
        vo = new CountryVO("SE", "Sverige");
        list.add(vo);
        config = new InternationalConfigManager(list);
    }

    @Test
    public void testGetList() {
        List<String> list = config.getList(vo.getId(), "country.searchFields.id[@label]");
        assertNotNull(list);
        assertEquals("patient.search.patient.svnr", list.get(0));
    }

    @Test
    public void testGetProperty() {
        List<Properties> list = config.getProperties(vo.getId(), "country.searchFields");
        assertNotNull(list);
        Properties prop = new Properties();
        prop.setProperty("country.searchFields.id[@domain]0", "2.16.17.710.807.1000.990.1");
        assertEquals(prop, list.get(0));
    }
}
