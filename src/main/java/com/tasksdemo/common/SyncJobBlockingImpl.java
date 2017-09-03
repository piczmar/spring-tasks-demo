package com.tasksdemo.common;

import com.tasksdemo.common.service.ISyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * This SyncJob will wait for all tasks to complete before doSync method exits.
 * It uses the same ExecutorService for tasks and jobs (common thread pool).
 */
@Component("blockingJob")
public class SyncJobBlockingImpl implements ISyncJob {
    private static final Logger log = LoggerFactory.getLogger(SyncJobBlockingImpl.class);

    private final ExecutorService executor;// executor service

    @Autowired
    public SyncJobBlockingImpl(@Qualifier("jobExecutor") final ExecutorService executor) {
        this.executor = executor;
    }

    public void doSync(ISyncService syncService) {
        log.info("SyncJobBlockingImpl start for {}", syncService.getClass().getSimpleName());
        IntStream mls = IntStream.range(0, 10);
        List<Callable<Object>> todo = new ArrayList<Callable<Object>>();

        mls.forEach(j -> {
            SyncConfig config = new SyncConfig();
            config.setTaskNumber(j);
            Runnable worker = new SyncTask(config, syncService);
            todo.add(Executors.callable(worker));

        });

        try {
            List<Future<Object>> answers = executor.invokeAll(todo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("SyncJobBlockingImpl end for {}", syncService.getClass().getSimpleName());
    }

}