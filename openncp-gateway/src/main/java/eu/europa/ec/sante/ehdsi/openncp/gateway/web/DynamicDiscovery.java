package eu.europa.ec.sante.ehdsi.openncp.gateway.web;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DynamicDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscovery.class);

    @RequestMapping(value = "/dynamicdiscovery/syncsearchmask", method = RequestMethod.GET)
    public String synchronizeSearchMask(Model model) {

        LOGGER.info("synchronizeSearchMask('{}')", System.currentTimeMillis());
        String countryList = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.countries");
        String[] countries = StringUtils.split(countryList, ",");
        List<String> synchronizedCountry = new ArrayList<>();

        for (String s : countries) {
            LOGGER.info("Fetching ISM for MS: '{}'", s);
            try {
                ConfigurationManagerFactory.getConfigurationManager().fetchInternationalSearchMask(s);
                synchronizedCountry.add(s);

            } catch (ConfigurationManagerException e) {
                LOGGER.error("ConfigurationManagerException: '{}'", e.getMessage(), e);
                synchronizedCountry.add(e.getLocalizedMessage());
            }
        }
        model.addAttribute("synchronizedCountry", synchronizedCountry);

        return "synchronized";
    }
}
