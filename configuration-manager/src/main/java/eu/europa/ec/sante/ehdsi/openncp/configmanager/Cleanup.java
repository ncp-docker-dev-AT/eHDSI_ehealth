package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cleanup {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cleanup.class);

    public void clean() {

    //    try {
            // JobDetail job = new JobDetail();
            // job.setName("dummyJobName");
            // job.setJobClass(HelloJob.class);

    //        JobDetail job = JobBuilder.newJob(PropertyMapJob.class).withIdentity("dummyJobName", "group1").build();

            // SimpleTrigger trigger = new SimpleTrigger();
            // trigger.setStartTime(new Date(System.currentTimeMillis() + 1000));
            // trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            // trigger.setRepeatInterval(30000);

            // Trigger the job to run on the next round minute
    //        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName", "group1")
    //                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever())
    //                .build();

            // schedule it
//            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
//            scheduler.start();
//            scheduler.scheduleJob(job, trigger);
//        } catch (SchedulerException e) {
//            LOGGER.info("SchedulerException: '{}'", e.getMessage(), e);
//        }



//        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
//
//        for (String groupName : scheduler.getJobGroupNames()) {
//
//            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
//
//                String jobName = jobKey.getName();
//                String jobGroup = jobKey.getGroup();
//
//                //get job's trigger
//                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
//                Date nextFireTime = triggers.get(0).getNextFireTime();
//
//                System.out.println("[jobName] : " + jobName + " [groupName] : "
//                        + jobGroup + " - " + nextFireTime);
//
//            }
//        }
    }
}
