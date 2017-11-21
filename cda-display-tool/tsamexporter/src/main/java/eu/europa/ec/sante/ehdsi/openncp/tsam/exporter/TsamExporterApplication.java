package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class TsamExporterApplication {

    private static final Logger logger = LoggerFactory.getLogger(TsamExporterApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(TsamExporterApplication.class, args);

        logger.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        logger.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        logger.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        TsamExporterManager manager = context.getBean(TsamExporterManager.class);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Start export process");

        manager.export();

        stopWatch.stop();
        logger.info("Export done in {} s", stopWatch.getTotalTimeSeconds());
    }
}
