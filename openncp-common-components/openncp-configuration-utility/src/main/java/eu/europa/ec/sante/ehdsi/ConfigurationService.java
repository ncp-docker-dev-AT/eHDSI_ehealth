package eu.europa.ec.sante.ehdsi;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Iterator;
import java.util.List;

@Service
public class ConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    private final Environment environment;

    private final PropertyRepository propertyRepository;

    @Autowired
    public ConfigurationService(Environment environment, PropertyRepository propertyRepository) {

        Assert.notNull(environment, "environment must not be null");
        Assert.notNull(propertyRepository, "jdbcTemplate must not be null");
        this.environment = environment;
        this.propertyRepository = propertyRepository;
    }

    /**
     * @throws ConfigurationException
     */
    public void loadProperties() throws ConfigurationException {

        try {
            String propertiesFile = environment.getRequiredProperty("openncp.property-file.path");
            PropertiesConfiguration config = new PropertiesConfiguration(new File(propertiesFile));
            config.setReloadingStrategy(new FileChangedReloadingStrategy());

            LOGGER.info("FILLING DATABASE WITH PROPERTIES...");

            List<Property> list = propertyRepository.findAll();
            LOGGER.info("Database contains '{}' entry(ies)", list.size());
            if (list.isEmpty()) {

                Iterator it = config.getKeys();

                while (it.hasNext()) {

                    String key = (String) it.next();
                    LOGGER.info("Key: '{}'-'{}'", key, config.getString(key));
                    Property property = new Property();
                    property.setKey(key);
                    property.setSmp(false);
                    property.setValue(config.getString(key));
                    propertyRepository.save(property);
                }
                LOGGER.info("{} value sets retrieved from the database", propertyRepository.findAll().size());
            } else {
                LOGGER.info("No need to initialize...");
            }
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException: Cannot retrieve OpenNCP configuration file and property: '{}'", e.getMessage(), e);
        }
    }
}
