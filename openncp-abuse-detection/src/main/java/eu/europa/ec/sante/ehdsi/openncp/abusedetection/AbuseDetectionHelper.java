package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

import epsos.ccd.gnomon.utils.Utils;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

public class AbuseDetectionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseDetectionHelper.class);
    public static final String NAME_OF_JOB = "AbuseDetectionJob";
    public static final String NAME_OF_GROUP = "openNCP";
    private static final String NAME_OF_TRIGGER = "triggerStart";

    //create variable scheduler of type Scheduler
    private static Scheduler scheduler;

    private AbuseDetectionHelper() {
    }

    public static void AbuseDetectionShutdown() throws Exception {
        boolean schedulerEnabled = Boolean.parseBoolean(Constants.ABUSE_SCHEDULER_TIME_INTERVAL);
        if(schedulerEnabled == true) {
            LOGGER.info("Stopping AbuseDetectionServiceFactory Service...");
        }
    }

    public static void AbuseDetectionInit() throws Exception {
        boolean schedulerEnabled = Boolean.parseBoolean(Constants.ABUSE_SCHEDULER_TIME_INTERVAL);
        if(schedulerEnabled == true) {
            LOGGER.info("Initializing AbuseDetectionServiceFactory Service...");

            //show message to know about the main thread
            LOGGER.info(" The name of the QuartzScheduler main thread is: " + Thread.currentThread().getName());

            //initialize scheduler instance from Quartz
            scheduler = new StdSchedulerFactory().getScheduler();

            //start scheduler
            scheduler.start();

            //create scheduler trigger based on the time interval
            Trigger triggerNew = createTrigger();

            //create scheduler trigger with a cron expression
            //Trigger triggerNew = createCronTrigger();

            //schedule trigger
            scheduleJob(triggerNew);
        } else {
            LOGGER.info("AbuseDetection Scheduler Disabled");
        }
    }

    //create scheduleJob() method to schedule a job
    private static void scheduleJob(Trigger triggerNew) throws Exception {

        //create an instance of the JoDetails to connect Quartz job to the CreateQuartzJob
        JobDetail jobInstance = JobBuilder.newJob(AbuseDetectionService.class).withIdentity(NAME_OF_JOB, NAME_OF_GROUP).build();

        //invoke scheduleJob method to connect the Quartz scheduler to the jobInstance and the triggerNew
        scheduler.scheduleJob(jobInstance, triggerNew);

    }

    //create createTrigger() method that returns a trigger based on the time interval
    /*private static Trigger createCronTrigger() {

        //create cron expression
        String CRON_EXPRESSION = "0 * * * * ?";

        //create a trigger to be returned from the method
        Trigger triggerNew = TriggerBuilder.newTrigger().withIdentity(NAME_OF_TRIGGER, NAME_OF_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION)).build();

        //return triggerNew to schedule it in main() method
        return triggerNew;
    }
    */

    //create createTrigger() method that returns a trigger based on the time interval
    private static Trigger createTrigger() {

        //initialize time interval
        int TIME_INTERVAL = 60;

        if(!Constants.ABUSE_SCHEDULER_TIME_INTERVAL.isEmpty()) {
            int val = Integer.parseInt(Constants.ABUSE_SCHEDULER_TIME_INTERVAL);
            if(val >= 60) {
                TIME_INTERVAL = val;
            }
        }

        //create a trigger to be returned from the method
        Trigger triggerNew = TriggerBuilder.newTrigger().withIdentity(NAME_OF_TRIGGER, NAME_OF_GROUP)
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(TIME_INTERVAL).repeatForever())
                .build();

        // triggerNew to schedule it in main() method
        return triggerNew;
    }
}
