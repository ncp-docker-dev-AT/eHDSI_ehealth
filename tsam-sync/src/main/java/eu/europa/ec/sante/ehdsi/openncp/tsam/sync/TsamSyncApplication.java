package eu.europa.ec.sante.ehdsi.openncp.tsam.sync;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.service.TsamSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TsamSyncApplication {

    private static final Logger logger = LoggerFactory.getLogger(TsamSyncApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = new SpringApplicationBuilder(TsamSyncApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        logger.info("OS: {} ({}, {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        logger.info("JRE: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        logger.info("JVM: {} ({})", System.getProperty("java.vm.version"), System.getProperty("java.vm.name"));

        TsamSyncManager manager = context.getBean(TsamSyncManager.class);
        manager.synchronize();
    }
}
