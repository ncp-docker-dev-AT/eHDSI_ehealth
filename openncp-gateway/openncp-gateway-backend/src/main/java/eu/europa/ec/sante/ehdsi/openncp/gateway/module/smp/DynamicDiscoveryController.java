package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util.DateTimeUtil;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.PropertyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class DynamicDiscoveryController {

    private final Logger logger = LoggerFactory.getLogger(DynamicDiscoveryController.class);
    private final PropertyService propertyService;
    private final DynamicDiscoveryService dynamicDiscoveryService;

    public DynamicDiscoveryController(PropertyService propertyService, DynamicDiscoveryService dynamicDiscoveryService) {
        this.propertyService = propertyService;
        this.dynamicDiscoveryService = dynamicDiscoveryService;
    }

    @GetMapping(path = "/dynamicdiscovery/syncsearchmask")
    public ResponseEntity<List<String>> synchronizeSearchMask() {

        if (logger.isInfoEnabled()) {
            logger.info("[Gateway] Synchronize Search Mask at ('{}')", DateTimeUtil.formatTimeInMillis(System.currentTimeMillis()));
        }

        String countryList = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.countries");
        String[] countries = StringUtils.split(countryList, ",");
        countries = StringUtils.stripAll(countries);
        List<String> synchronizedCountry = new ArrayList<>();

        for (String countryCode : countries) {
            try {
                dynamicDiscoveryService.fetchInternationalSearchMask(countryCode);
                synchronizedCountry.add(countryCode);

            } catch (ConfigurationManagerException e) {
                logger.error("ConfigurationManagerException: '{}'", e.getMessage());
                synchronizedCountry.add(e.getLocalizedMessage() + " " + e.getMessage());
            }
        }

        return ResponseEntity.ok(synchronizedCountry);
    }
}
