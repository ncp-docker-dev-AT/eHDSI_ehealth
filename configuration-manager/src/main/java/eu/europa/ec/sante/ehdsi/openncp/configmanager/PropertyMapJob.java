package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class PropertyMapJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyMapJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        Iterator<Map.Entry<String, String>> it = ConfigurationManagerFactory.getConfigurationManager().getProperties().entrySet().iterator();
        LOGGER.info("PropertyMapJob - Execute()...");

        while (it.hasNext()) {

            Map.Entry<String, String> property = it.next();

            try {
                String value = ConfigurationManagerFactory.getConfigurationManager().getProperty(property.getKey(), false);

                if (StringUtils.equalsIgnoreCase(property.getKey(), "eu.PatientIdentificationService.WSE")) {
                    LOGGER.info("\t\t-> Property Key OK: '{}' - DB: '{}'", property.getKey(), value);
                }
            } catch (PropertyNotFoundException e) {

                LOGGER.error("PropertyNotFoundException: '{}'", e.getMessage(), e);
                it.remove();
                LOGGER.info("Removing Key from Properties Map: '{}'", property.getKey());
            }
        }
    }
}
