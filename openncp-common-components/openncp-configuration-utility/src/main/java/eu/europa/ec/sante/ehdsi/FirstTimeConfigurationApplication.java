package eu.europa.ec.sante.ehdsi;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = {"eu.europa.ec.sante.ehdsi"})
public class FirstTimeConfigurationApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirstTimeConfigurationApplication.class);

    public FirstTimeConfigurationApplication() {
    }

    public static void main(String[] args) throws ConfigurationException {

        LOGGER.info("Starting Import process...");
        ApplicationContext context = SpringApplication.run(FirstTimeConfigurationApplication.class, args);
        LOGGER.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        LOGGER.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        ConfigurationService manager = context.getBean(ConfigurationService.class);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("Start synchronization process");
            manager.loadProperties();
            stopWatch.stop();
            LOGGER.info("Synchronization done in {} s", stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }
}
