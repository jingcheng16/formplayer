package org.commcare.formplayer.services;

import com.timgroup.statsd.StatsDClient;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commcare.formplayer.util.Constants;
import org.commcare.formplayer.utils.CheckedFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class ScheduledTasks {

    private final Log log = LogFactory.getLog(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private StatsDClient datadogStatsDClient;

    @Autowired
    private FormSessionService formSessionService;

    @Autowired
    private VirtualDataInstanceService virtualDataInstanceService;

    // the default "0 0 0 * * *" schedule means midnight each night
    @Scheduled(cron = "${commcare.formplayer.scheduledTasks.purge.cron:0 0 0 * * *}")
    @SchedulerLock(name = "purge",
            lockAtMostFor = "${commcare.formplayer.scheduledTasks.purge.lockAtMostFor:5h}",
            lockAtLeastFor = "${commcare.formplayer.scheduledTasks.purge.lockAtLeastFor:1h}")
    public void purge() {
        log.info("Starting purge scheduled task.");
        doTimedPurge("formSession", formSessionService::purge);
        doTimedPurge("virtualDataInstance", virtualDataInstanceService::purge);
        datadogStatsDClient.increment(
                String.format("%s.%s", Constants.SCHEDULED_TASKS_PURGE, "timesRun")
        );
    }

    private void doTimedPurge(String tag, CheckedFunction<Instant, Integer, RuntimeException> purgeable) {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        log.info("Beginning purge for " + tag);
        long start = System.currentTimeMillis();
        int deletedRows = purgeable.apply(cutoff);
        long elapsed = System.currentTimeMillis() - start;
        log.info(String.format("Purged %d records in %d ms for %s", deletedRows, elapsed, tag));
        datadogStatsDClient.count(
                String.format("%s.%s.%s", Constants.SCHEDULED_TASKS_PURGE, "deletedRows", tag),
                deletedRows
        );
        datadogStatsDClient.time(
                String.format("%s.%s.%s", Constants.SCHEDULED_TASKS_PURGE, "timeInMillis", tag),
                elapsed
        );
    }
}
