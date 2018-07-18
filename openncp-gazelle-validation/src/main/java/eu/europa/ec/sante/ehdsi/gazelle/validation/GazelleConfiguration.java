package eu.europa.ec.sante.ehdsi.gazelle.validation;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GazelleConfiguration {

    private static final String nationalConfig = System.getenv("EPSOS_PROPS_PATH") + "validation"
            + File.separatorChar + "gazelle.ehdsi.properties";
    private static GazelleConfiguration instance;
    private final Logger logger = LoggerFactory.getLogger((GazelleConfiguration.class));
    private Configuration configuration;

    private GazelleConfiguration() throws ConfigurationException {

        File file = new File(nationalConfig);
        String gazelleConfig;
        if (file.exists()) {
            logger.info("Loading National Gazelle Configuration");
            gazelleConfig = nationalConfig;
        } else {
            logger.info("Loading Default Gazelle Configuration");
            gazelleConfig = "gazelle.ehdsi.properties";
        }
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFileName(gazelleConfig));
        configuration = builder.getConfiguration();
    }

    public static GazelleConfiguration getInstance() {

        if (null == instance) {
            try {
                instance = new GazelleConfiguration();
            } catch (ConfigurationException ex) {
                throw new RuntimeException(ex);
            }
        }
        return instance;
    }

    public Configuration getConfigure() {
        return configuration;
    }

    public void setConfig(Configuration configure) {
        this.configuration = configure;
    }
}
