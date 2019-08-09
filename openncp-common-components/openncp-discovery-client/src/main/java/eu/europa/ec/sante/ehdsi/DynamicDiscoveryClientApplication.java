package eu.europa.ec.sante.ehdsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class DynamicDiscoveryClientApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscoveryClientApplication.class);

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(DynamicDiscoveryClientApplication.class, args);

        LOGGER.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        LOGGER.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        ServiceMetadataLocatorManager serviceMetadataLocatorManager = context.getBean(ServiceMetadataLocatorManager.class);

        try {
            LOGGER.info("Start synchronization process");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            serviceMetadataLocatorManager.lookup();
            stopWatch.stop();
            LOGGER.info("Synchronization done in {} s", stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }
}
