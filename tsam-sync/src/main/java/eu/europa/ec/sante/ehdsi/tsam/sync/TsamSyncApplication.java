package eu.europa.ec.sante.ehdsi.tsam.sync;

import eu.europa.ec.sante.ehdsi.tsam.sync.client.TermServerClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class TsamSyncApplication {

    private static final Logger logger = LoggerFactory.getLogger(TsamSyncApplication.class);

    public TsamSyncApplication() {
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(TsamSyncApplication.class, args);

        logger.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        logger.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        logger.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        TsamSyncManager manager = context.getBean(TsamSyncManager.class);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logger.info("Start synchronization process");
            manager.synchronize();
            stopWatch.stop();
            logger.info("Synchronization done in {} s", stopWatch.getTotalTimeSeconds());
        } catch (TermServerClientException e) {
            logger.error("An error has occurred during the sync process:\nHTTP status: {} {}\nDetails: {}", e.statusCode(), e.statusText(), e.body(), e);
        }
    }
}
