package com.tasksdemo.common;

import com.tasksdemo.common.service.ISyncService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("scheduled.task1.enabled")
public class Job1Scheduler {
    private static final Logger log = LoggerFactory.getLogger(Job1Scheduler.class);
    private final ISyncService syncService;
    private final ISyncJob syncJob;

    @Autowired
    public Job1Scheduler(@Qualifier("nonBlockingJob") final ISyncJob syncJob,
            @Qualifier("service1") final ISyncService syncService) {
        this.syncJob = syncJob;
        this.syncService = syncService;
    }

    @Scheduled(cron = "${scheduled.task1}")
    public void doSync() {
        log.info("Started sync. at {}", new DateTime());
        syncJob.doSync(syncService);
        log.info("Ended sync. at {}", new DateTime());
    }
}
