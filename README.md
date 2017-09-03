# spring-tasks-demo
Demo of Spring executors and scheduled jobs.
This application has two jobs running on its own pace scheduled in `Job1Scheduler` and `Job2Scheduler` based on properties in `application.properties`.

## Prerequisites
You need to install Maven 3 and JDK 8 and add binaries to classpath.

## Build and run
It is using Spring boot and Maven, use the following command to build:

```
mvn clean package
```

To run type in console:

```
mvn spring-boot:run
```

it should start application on Tomcat on port 8080.
It does not contain any web interface. The purpose of using Tomcat is to keep Spring context alive and block application from immediate exit.

## Demo cases

There are several demonstration purposes:
1. Running multiple scheduled jobs
2. Running a job which itself runs multiple tasks concurrently
3. Showing how different tasks executors affect runtime sequence when using different executor settings in `JobsConfiguration`

### Case 1
To demonstrate case 1 just build the project and run, then watch how jobs are running every 15 sec.
You would see logs similar to these below:

```
2017-08-31 14:00:00.399  INFO 6585 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-08-31 14:00:00.406  INFO 6585 --- [           main] com.tasksdemo.Application                : Started Application in 3.494 seconds (JVM running for 7.127)
2017-08-31 14:00:00.406  INFO 6585 --- [           main] com.tasksdemo.Application                : Started application
2017-08-31 14:00:15.037  INFO 6585 --- [pool-1-thread-1] com.tasksdemo.common.Job2Scheduler       : Started sync. at 2017-08-31T14:00:15.005+02:00
2017-08-31 14:00:15.037  INFO 6585 --- [pool-1-thread-2] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:00:15.005+02:00
2017-08-31 14:00:15.049  INFO 6585 --- [pool-1-thread-1] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking start
2017-08-31 14:00:15.049  INFO 6585 --- [pool-1-thread-2] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking start
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-4] c.tasksdemo.common.service.Service2Impl  : doSync running.. for task 0
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-2] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking end
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-1] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking end
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-4] c.tasksdemo.common.service.Service2Impl  : doSync running.. for task 1
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-3] c.tasksdemo.common.service.Service2Impl  : doSync running.. for task 2
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-5] c.tasksdemo.common.service.Service2Impl  : doSync running.. for task 3
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-2] com.tasksdemo.common.Job1Scheduler       : Ended sync. at 2017-08-31T14:00:15.054+02:00
2017-08-31 14:00:15.054  INFO 6585 --- [pool-1-thread-1] com.tasksdemo.common.Job2Scheduler       : Ended sync. at 2017-08-31T14:00:15.054+02:00
```

The most important for this case are sections `Started sync. at` and `Ended sync. at` which distinguish start and exit of each of the two jobs.
We also see that they run in the same thread pool in separate threats `pool-1-thread-1` and `pool-1-thread-2`

### Case 2
For better logs readability we can disable one of the jobs in `application.properties` by setting

```
scheduled.task2.enabled=false
```

Now only `Job1Scheduler` will run the job. The job itself creates 10 new tasks (`SyncTask`) and passes them to executor.

There are 3 kinds of jobs/tasks execution:
* `SyncJobBlockingImpl` - this wait for all tasks to complete before doSync method exits.
It uses the same ExecutorService for tasks and jobs (common thread pool).

* `SyncJobNonBlockingImpl` - this will NOT wait for all tasks to complete before doSync method exits.
It submits all tasks to executor and exits, then executor will process tasks in a pool.
It uses the same ExecutorService for tasks and jobs (common thread pool).

* `SyncJobNonBlockingWithSeparateTaskExecutorImpl` - this is similar to `SyncJobNonBlockingImpl` but it uses a separate task executor than the one used by scheduled jobs.
This way we can configure different thread pool settings for the executor (See method `jobTaskExecutor` in class `JobsConfiguration`).

To choose one of them change Job1Scheduler and Job2Scheduler constructor @Qualifier for ISyncJob
e.g. to use `SyncJobNonBlockingWithSeparateTaskExecutorImpl` set `nonBlockingJobWithSeparateExecutor`:

```
    @Autowired
    public Job1Scheduler(@Qualifier("nonBlockingJobWithSeparateExecutor") final ISyncJob syncJob,
            @Qualifier("service1") final ISyncService syncService) {
        this.syncJob = syncJob;
        this.syncService = syncService;
    }
```


### Case 3

When you start this project unchanged then `SyncJobNonBlockingImpl` task will run and you can notice that tasks are still running even when scheduled job has already completed (see logs below)

```
2017-08-31 14:42:53.362  INFO 8946 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-08-31 14:42:53.369  INFO 8946 --- [           main] com.tasksdemo.Application                : Started Application in 3.041 seconds (JVM running for 7.24)
2017-08-31 14:42:53.370  INFO 8946 --- [           main] com.tasksdemo.Application                : Started application
2017-08-31 14:43:00.041  INFO 8946 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:43:00.012+02:00
2017-08-31 14:43:00.053  INFO 8946 --- [pool-1-thread-1] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking start
2017-08-31 14:43:00.056  INFO 8946 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:43:00.056  INFO 8946 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 2
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 3
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 4
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-1] c.t.common.SyncJobNonBlockingImpl        : SyncJobNonBlocking end
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 5
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 6
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 7
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 8
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Ended sync. at 2017-08-31T14:43:00.057+02:00
2017-08-31 14:43:00.057  INFO 8946 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 9
```

This may be very confusing when scheduled jobs are running very often but tasks take longer to complete then the scheduled job.
Soon it will be hard to distinguish which job has started the task.
Of course in some circumstances such behavior may bring positive effect in terms of performance, e.g. lets imagine situation when each scheduled job can start number of tasks which vary from execution to execution.
In this case when first execution started great number of tasks while the second started zero tasks, still we can schedule next execution and tasks are processed concurrently.
In other words we do not block scheduled jobs to run.

However if we needed to block them, there is a solution used in `SyncJobBlockingImpl` (logs below)

```
2017-08-31 14:50:45.041  INFO 9347 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:50:45.010+02:00
2017-08-31 14:50:45.052  INFO 9347 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl start for Service1Impl
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 2
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 4
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 3
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 5
2017-08-31 14:50:45.056  INFO 9347 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 6
2017-08-31 14:50:45.057  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 7
2017-08-31 14:50:45.057  INFO 9347 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 8
2017-08-31 14:50:45.057  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 9
2017-08-31 14:50:45.057  INFO 9347 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl end for Service1Impl
2017-08-31 14:50:45.058  INFO 9347 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Ended sync. at 2017-08-31T14:50:45.058+02:00
2017-08-31 14:51:00.000  INFO 9347 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:51:00.000+02:00
2017-08-31 14:51:00.000  INFO 9347 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl start for Service1Impl
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 2
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 3
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 4
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 5
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 6
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 7
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 8
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 9
2017-08-31 14:51:00.001  INFO 9347 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl end for Service1Impl
2017-08-31 14:51:00.002  INFO 9347 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Ended sync. at 2017-08-31T14:51:00.002+02:00
```

and the line of code which does it

```
List<Future<Object>> answers = executor.invokeAll(todo);
```

By assigning results from `invokeAll` we determine that the thread which runs this code will wait until the `answers` is available.
It would stop any subsequent jobs to run according to schedule. We can check it by adding `sleep` block in `Service1Impl` like this:

```
   @Override
    public void doSync(SyncConfig config) {
        log.info("doSync running.. for task {} ", config.getTaskNumber());

        try {
            Thread.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

It will take 30s for each task to run now and you can notice that next scheduled job will not run until all the tasks from last job are done:

```
2017-08-31 14:57:00.044  INFO 9728 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:57:00.012+02:00
2017-08-31 14:57:00.055  INFO 9728 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl start for Service1Impl
2017-08-31 14:57:00.059  INFO 9728 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:57:00.059  INFO 9728 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:57:00.059  INFO 9728 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 2
2017-08-31 14:57:00.059  INFO 9728 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 3
2017-08-31 14:57:30.059  INFO 9728 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 4
2017-08-31 14:57:30.059  INFO 9728 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 5
2017-08-31 14:57:30.059  INFO 9728 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 7
2017-08-31 14:57:30.059  INFO 9728 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 6
2017-08-31 14:58:00.060  INFO 9728 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 8
2017-08-31 14:58:00.060  INFO 9728 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 9
2017-08-31 14:58:30.061  INFO 9728 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl end for Service1Impl
2017-08-31 14:58:30.061  INFO 9728 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Ended sync. at 2017-08-31T14:58:30.061+02:00
2017-08-31 14:58:45.001  INFO 9728 --- [pool-1-thread-1] com.tasksdemo.common.Job1Scheduler       : Started sync. at 2017-08-31T14:58:45.001+02:00
2017-08-31 14:58:45.001  INFO 9728 --- [pool-1-thread-1] c.tasksdemo.common.SyncJobBlockingImpl   : SyncJobBlockingImpl start for Service1Impl
2017-08-31 14:58:45.002  INFO 9728 --- [pool-1-thread-5] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 0
2017-08-31 14:58:45.002  INFO 9728 --- [pool-1-thread-2] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 1
2017-08-31 14:58:45.002  INFO 9728 --- [pool-1-thread-4] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 2
2017-08-31 14:58:45.002  INFO 9728 --- [pool-1-thread-3] c.tasksdemo.common.service.Service1Impl  : doSync running.. for task 3

```

You can also see here that thread pool is always 5 and if you have processor with more cores we could actually speed it up by adding more threads in a pool.
We can change `JobsConfiguration` and add more thread in fragment:

```
    public ExecutorService jobExecutor() {
        return Executors.newScheduledThreadPool(5);
    }
```



## Release
Release this project using Maven Release Plugin by doing the following steps:
1. mvn release:clean
2. mvn release:prepare
3. mvn release:perform
