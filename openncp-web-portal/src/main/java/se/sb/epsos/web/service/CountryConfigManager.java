package se.sb.epsos.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.model.CountryVO;
import se.sb.epsos.web.model.PatientIdVO;
import se.sb.epsos.web.util.InternationalConfigManager;
import se.sb.epsos.web.util.MasterConfigManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountryConfigManager {

    private static final String CONFIG_PREFIX = "CountryConfigManager.country.";
    private static final String TEXT = ".text";
    private static final String HELP_TEXT_FOR_TEST = ".HelpTextForTest";
    private static final String IDENTIFICATION_HELP_LINK = ".IdentificationHelpLink";

    private static final InternationalConfigManager internationalSearchConfig = new InternationalConfigManager(getCountries());
    private static final String INTERNATIONALSEARCHFIELDS = "country.searchFields";
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryConfigManager.class);

    private CountryConfigManager() {
    }

    public static List<Properties> getSearchFields(String countryCode) {
        return InternationalConfigManager.getProperties(countryCode, INTERNATIONALSEARCHFIELDS);
    }

    public static List<PatientIdVO> getPatientIdentifiers(CountryVO country) {
        List<PatientIdVO> patIds = new ArrayList<>();
        List<Properties> props = getSearchFields(country.getId());

        //List<String> list = InternationalConfigManager.getList(country.getId(), "country.searchFields.id[@label]");
        List<String> list = InternationalConfigManager.getList(country.getId(), "searchFields.country.patientSearch.identifier.id[@label]");
        int i = 0;

        for (String str : list) {
            LOGGER.info("Get Patient Identifier: '{}'", str);
            PatientIdVO vo = new PatientIdVO();
            vo.setLabel(str);
            for (Properties prop : props) {
                Set<Object> keys = prop.keySet();
                for (Object k : keys) {
                    String key = (String) k;
                    LOGGER.info("Properties: '{}'", prop.getProperty(key));
                }

                if (prop.containsKey("country.searchFields.id[@domain]" + i)) {
                    vo.setDomain(prop.getProperty("country.searchFields.id[@domain]" + i));
                } else if (prop.containsKey("country.searchFields.id[@max]" + i) && !prop.getProperty("country.searchFields.id[@max]" + i).equals("-1")) {
                    vo.setMax(Integer.parseInt(prop.getProperty("country.searchFields.id[@max]" + i)));
                } else if (prop.containsKey("country.searchFields.id[@min]" + i) && !prop.getProperty("country.searchFields.id[@min]" + i).equals("-1")) {
                    vo.setMin(Integer.parseInt(prop.getProperty("country.searchFields.id[@min]" + i)));
                }
            }
            patIds.add(vo);
            i++;
        }

        List<String> listTextField = InternationalConfigManager.getList(country.getId(), "country.searchFields.textField[@label]");
        for (String str : listTextField) {
            PatientIdVO vo = new PatientIdVO();
            vo.setLabel(str);
            patIds.add(vo);
        }

        List<String> listBirthDate = InternationalConfigManager.getList(country.getId(), "country.searchFields.birthDate[@label]");
        for (String str : listBirthDate) {
            PatientIdVO vo = new PatientIdVO();
            vo.setLabel(str);
            patIds.add(vo);
        }

        List<String> listAdministrativeGender = InternationalConfigManager.getList(country.getId(), "country.searchFields.sex[@label]");
        for (String str : listAdministrativeGender) {
            PatientIdVO vo = new PatientIdVO();
            vo.setLabel(str);
            patIds.add(vo);
        }

        return patIds;
    }

    public static String getHomeCommunityId(String countryCode) {
        return MasterConfigManager.get(CONFIG_PREFIX + countryCode + "[@homeCommunity]");
    }

    public static String getText(CountryVO country) {
        return MasterConfigManager.get(CONFIG_PREFIX + country.getId() + TEXT);
    }

    public static String getHelpTextForTest(CountryVO country) {
        return MasterConfigManager.get(CONFIG_PREFIX + country.getId() + HELP_TEXT_FOR_TEST);
    }

    public static String getIdentificationHelpLink(CountryVO country) {
        return MasterConfigManager.get(CONFIG_PREFIX + country.getId() + IDENTIFICATION_HELP_LINK);
    }

    public static List<CountryVO> getCountries() {
        List<CountryVO> countries = new ArrayList<>();
        Properties props = MasterConfigManager.getProperties("CountryConfigManager.country");
        for (Object key : props.keySet()) {
            Pattern p = Pattern.compile("^CountryConfigManager\\.country\\.([A-Z]*)\\.text$");
            Matcher m = p.matcher(key.toString());
            if (m.find()) {
                countries.add(new CountryVO(m.group(1), null, getHomeCommunityId(m.group(1))));
            }
        }
        Collections.sort(countries);
        return countries;
    }

    static CountryVO getCountry(String homeCommunityId) {
        if (homeCommunityId == null) return null;
        List<CountryVO> countries = getCountries();
        for (CountryVO country : countries) {
            if (homeCommunityId.equals(country.getHomeCommunityId())) {
                return country;
            }
        }
        return null;
    }
}
