package eu.europa.ec.sante.ehdsi.openncp.gateway.web;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DynamicDiscovery {

    private final Logger logger = LoggerFactory.getLogger(DynamicDiscovery.class);

    @GetMapping(value = "/dynamicdiscovery/syncsearchmask")
    public String synchronizeSearchMask(Model model) {

        logger.info("synchronizeSearchMask('{}')", System.currentTimeMillis());
        String countryList = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.countries");
        String[] countries = StringUtils.split(countryList, ",");
        List<String> synchronizedCountry = new ArrayList<>();

        for (String s : countries) {
            logger.info("Fetching ISM for MS: '{}'", s);
            try {
                ConfigurationManagerFactory.getConfigurationManager().fetchInternationalSearchMask(s);
                synchronizedCountry.add(s);

            } catch (ConfigurationManagerException e) {
                logger.error("ConfigurationManagerException: '{}'", e.getMessage(), e);
                synchronizedCountry.add(e.getLocalizedMessage());
            }
        }
        model.addAttribute("synchronizedCountry", synchronizedCountry);

        return "synchronized";
    }
}
