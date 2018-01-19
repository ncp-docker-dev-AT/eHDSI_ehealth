package eu.europa.ec.sante.ehdsi;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import java.security.NoSuchAlgorithmException;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = {"eu.europa.ec.sante.ehdsi"})
public class DynamicDiscoveryClientApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDiscoveryClientApplication.class);

    public DynamicDiscoveryClientApplication() {
    }

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(DynamicDiscoveryClientApplication.class, args);

        LOGGER.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        LOGGER.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        ServiceMetadataLocatorManager manager = context.getBean(ServiceMetadataLocatorManager.class);
        //  b-2c1a0fa2bc1b37789bc426e278b235a0
        //  b-2c1a0fa2bc1b37789bc426e278b235a0.ehealth-participantid-qns.ehealth.testa.eu: Name or service not known
        //  lyndpiwxe5x3jwund7pixgpqohifdeygdj6pzobcz2f66kcecvxa
        String PARTICIPANT_IDENTIFIER = "urn:ehealth:mt:ncp-idp";
        try {
            LOGGER.info("NAPTR Hash: '{}'", HashUtil.getSHA256HashBase32(PARTICIPANT_IDENTIFIER));
            LOGGER.info("CNAME Hash: '{}'", StringUtils.lowerCase("b-" + HashUtil.getMD5Hash(PARTICIPANT_IDENTIFIER)));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException: '{}'", e.getMessage(), e);
        }
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("Start synchronization process");
            //manager.lookup();
            manager.lookupOpenNCP();
            //  manager.validateOpenNCP();

            stopWatch.stop();
            LOGGER.info("Synchronization done in {} s", stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }
}
