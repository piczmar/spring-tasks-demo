package com.tasksdemo.common;

import com.tasksdemo.common.service.ISyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

/**
 * This SyncJob is similar to SyncJobNonBlockingImpl but it uses a separate task executor than the one used by scheduled jobs.
 * This way we can configure different thread pool settings for the executor (See method jobTaskExecutor in class JobsConfiguration)
 */
@Component("nonBlockingJobWithSeparateExecutor")
public class SyncJobNonBlockingWithSeparateTaskExecutorImpl implements ISyncJob {
    private static final Logger log = LoggerFactory.getLogger(SyncJobNonBlockingWithSeparateTaskExecutorImpl.class);

    private final TaskExecutor executor;

    @Autowired
    public SyncJobNonBlockingWithSeparateTaskExecutorImpl(@Qualifier("jobTaskExecutor") final TaskExecutor executor) {
        this.executor = executor;
    }

    public void doSync(ISyncService syncService) {

        log.info("SyncJobNonBlockingWithSeparateTaskExecutorImpl start for {}", syncService.getClass().getSimpleName());
        IntStream mls = IntStream.range(0, 10);
        mls.forEach(j -> {
            SyncConfig config = new SyncConfig();
            config.setTaskNumber(j);
            Runnable worker = new SyncTask(config, syncService);
            executor.execute(worker);
        });

        log.info("SyncJobNonBlockingWithSeparateTaskExecutorImpl end for {}", syncService.getClass().getSimpleName());
    }

}