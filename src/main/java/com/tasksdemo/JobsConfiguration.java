package com.tasksdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class JobsConfiguration implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(jobExecutor());
    }

    // this executor is used to run scheduled jobs and tasks in SyncJobBlockingImpl and SyncJobNonBlockingImpl
    @Bean(destroyMethod = "shutdown")
    public ExecutorService jobExecutor() {
        return Executors.newScheduledThreadPool(5);
    }

    // this executor is used to run tasks in SyncJobNonBlockingWithSeparateTaskExecutorImpl
    @Bean(destroyMethod = "shutdown")
    TaskExecutor jobTaskExecutor() {
        /**
         * CorePoolSize - number of threads running even when no tasks are in a queue to process, they can be terminated if
         *  AllowCoreThreadTimeOut == true, the idle time after which thread is killed can be set in KeepAliveSeconds
         * MaxPoolSize - max number of threads running in a pool, executor will start new threads when needed
         * QueueCapacity - max number of tasks in the queue, if queue is full no more tasks can be added and IllegalStateException is thrown (BlockingQueue is used underneath)
         */
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(10);
        t.setMaxPoolSize(100);
        t.setQueueCapacity(50);
        t.setAllowCoreThreadTimeOut(true);
        t.setKeepAliveSeconds(120);
        return t;
    }

}
