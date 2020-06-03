package eu.europa.ec.sante.ehdsi.openncp.gateway.web;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DynamicDiscoveryController {

    private final Logger logger = LoggerFactory.getLogger(DynamicDiscoveryController.class);

    @GetMapping(value = "/dynamicdiscovery/syncsearchmask")
    public String synchronizeSearchMask(Model model) {

        if (logger.isInfoEnabled()) {
            logger.info("[Gateway] Synchronize Search Mask at ('{}')", DateTimeUtil.formatTimeInMillis(System.currentTimeMillis()));
        }
        String countryList = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.countries");
        String[] countries = StringUtils.split(countryList, ",");
        countries = StringUtils.stripAll(countries);
        List<String> synchronizedCountry = new ArrayList<>();

        for (String countryCode : countries) {
            try {
                DynamicDiscoveryService.fetchInternationalSearchMask(countryCode);
                synchronizedCountry.add(countryCode);

            } catch (ConfigurationManagerException e) {
                logger.error("ConfigurationManagerException: '{}'", e.getMessage());
                synchronizedCountry.add(e.getLocalizedMessage() + " " + e.getMessage());
            }
        }
        model.addAttribute("synchronizedCountry", synchronizedCountry);

        return "synchronized";
    }
}
