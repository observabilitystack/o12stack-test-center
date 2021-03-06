# O12Stack Test Center Application

This is a small demo application that can be used for test or training purposes regarding observability.

## What does this demo do?

This demo processes 'jobs' (small dummy tasks) using a thread pool with workers.

Once you started the application (a local demo is included, see "Run demo" at the end of this document), you can control the job flow via this UI, which you access via http://localhost:8080:

![o12stack test center UI](dashboard.png)

You can do the following things with the UI:

* start/stop the submission of jobs into the pipeline
* (brain symbol) select the job complexity which influces the average job duration
* (snail symbol) if switched on, about 3% of the jobs are outliers, their processing takes considerably longer
* (processor symbol) number of executors to adjust parallel execution
* (bomb symbol) If you drop a bomb (one per click), the next scheduled job will fail with an exception right after being launched.
* (skull symbol) A click on this button causes the application/JVM to terminate with an `OutOfMemoryError`.
 
## Metrics

The application offers Prometheus metrics via the endpoint http://localhost:8080/actuator/prometheus.

### Counters

![counters in Grafana](grafana_counters.png)

This graph shows the total number of jobs over time. As it only shows counters, the values can only drop back to zero if the application is restarted.

|  metric/counter | description  | 
|---|---|
| `jobs_submitted_total`  | Total # of submitted jobs | 
| `jobs_completed_total`  | Total # of jobs that were completed successfully | 
| `jobs_failed_total`  | Total # of jobs that failed. | 

### Gauges

![gauges in Grafana](grafana_gauges.png)

This graph shows the current number of jobs waiting to be executed.

|  metric/counter | description  | 
|---|---|
| `jobs_waiting`  | Current # of jobs which are waiting for execution | 
| `jobs_in_progress`  | Current # of jobs in progress (not shown in graph above) | 

### Summaries

![summary for job duration in Grafana](grafana_summary_duration.png)

These graphs show a summary of the job durations. The values are calculated as quantiles for a sliding window of 1m.

The orange graph between `10:57:30` and `11:00:30` shows that there were some outliers in that period.

The metric (produced by a 'timer' in Micrometer) `job_duration_timer_seconds` produces a variety of time series as shown here:

```
# HELP job_duration_timer_seconds  
# TYPE job_duration_timer_seconds summary
job_duration_timer_seconds{quantile="0.5",} 0.3145728
job_duration_timer_seconds{quantile="0.9",} 0.415236096
job_duration_timer_seconds{quantile="0.95",} 0.448790528
job_duration_timer_seconds{quantile="0.99",} 0.7340032
job_duration_timer_seconds_count 338.0
job_duration_timer_seconds_sum 105.783639
# HELP job_duration_timer_seconds_max  
# TYPE job_duration_timer_seconds_max gauge
job_duration_timer_seconds_max 0.871385
```

|  metric/counter | description  | 
|---|---|
| `job_duration_timer_seconds{quantile=...}`  | duration at given quantile (sliding window) | 
| `job_duration_timer_seconds_count`  | (Counter) number of counted durations (total) | 
| `job_duration_timer_seconds_sum`  | (Counter) sum of all counted durations (total) | 
| `job_duration_timer_seconds_max`  | (Gauge) max duration (sliding window) | 

![summary for job waiting time in Grafana](grafana_summary_wait.png)

These graphs show a summary of the job wait times. The values are also calculated as quantiles for a sliding window of 1m. The metric name is `job_wait_timer_seconds`. 

Showing a various number of different quantile values does not make so much sense in this case, as the waiting time is not as distributed as the duration (see above).

Therefore, we only show the mean, the 90% quantile and the max value. The average value (perhaps the most interesting one) is calculated from `job_wait_timer_seconds_sum` and `job_duration_timer_seconds_count`. As the latter values are counters, the average is not really comparable to the other values that are calculated on 1m sliding windows.


## Logs

The application emits logs as JSON one-liners via stdout. You should be able to parse/analyze them with your favourite logging stack.

This is how the stream of logs looks like:

```
{"thread":"Thread-3","level":"INFO","loggerName":"org.o12stack.o12stack.testcenter.jobs.JobExecutor","message":"Submitted job 2a324725-f570-4c22-81d8-6e240b58315c","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1569318018,"nanoOfSecond":596993000},"threadId":33,"threadPriority":5}
{"thread":"pool-2-thread-2","level":"INFO","loggerName":"org.o12stack.o12stack.testcenter.jobs.JobExecutor","message":"Started job 2a324725-f570-4c22-81d8-6e240b58315c after waiting for PT0.001581S","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1569318018,"nanoOfSecond":598688000},"threadId":35,"threadPriority":5}
{"thread":"pool-2-thread-2","level":"INFO","loggerName":"org.o12stack.o12stack.testcenter.jobs.JobExecutor","message":"Finished job 2a324725-f570-4c22-81d8-6e240b58315c after running for PT0.587217S: 'Sleep for 586 ms.' => 'DONE!'","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1569318019,"nanoOfSecond":185966000},"threadId":35,"threadPriority":5}
```

Let's look into the details of these log messages.

The following message is written if a job is created and submitted into the system. Each job has a UUID which can be found as part of the `message` string:

```json
{
    "thread": "Thread-3",
    "level": "INFO",
    "loggerName": "org.o12stack.o12stack.testcenter.jobs.JobExecutor",
    "message": "Submitted job 2a324725-f570-4c22-81d8-6e240b58315c",
    "endOfBatch": false,
    "loggerFqcn": "org.apache.logging.slf4j.Log4jLogger",
    "instant": {
        "epochSecond": 1569318018,
        "nanoOfSecond": 596993000
    },
    "threadId": 33,
    "threadPriority": 5
}
```

After a job's execution has started, a message is written containing the waiting time of this job:

```json
{
    "thread": "pool-2-thread-2",
    "level": "INFO",
    "loggerName": "org.o12stack.o12stack.testcenter.jobs.JobExecutor",
    "message": "Started job 2a324725-f570-4c22-81d8-6e240b58315c after waiting for PT0.001581S",
    "endOfBatch": false,
    "loggerFqcn": "org.apache.logging.slf4j.Log4jLogger",
    "instant": {
        "epochSecond": 1569318018,
        "nanoOfSecond": 598688000
    },
    "threadId": 35,
    "threadPriority": 5
}
```

After the job execution is done, you'll find a message like this one:

```json
{
    "thread": "pool-2-thread-2",
    "level": "INFO",
    "loggerName": "org.o12stack.o12stack.testcenter.jobs.JobExecutor",
    "message": "Finished job 2a324725-f570-4c22-81d8-6e240b58315c after running for PT0.587217S: 'Sleep for 586 ms.' => 'DONE!'",
    "endOfBatch": false,
    "loggerFqcn": "org.apache.logging.slf4j.Log4jLogger",
    "instant": {
        "epochSecond": 1569318019,
        "nanoOfSecond": 185966000
    },
    "threadId": 35,
    "threadPriority": 5
}
```

If a job's execution failed, the JSON is a little more complex and holds Java stack trace data:

```json
{
    "thread": "pool-2-thread-13",
    "level": "ERROR",
    "loggerName": "org.o12stack.o12stack.testcenter.jobs.JobExecutor",
    "message": "Error in job 9ed58643-95e8-4612-9367-ffabf572d2ba",
    "thrown": {
        "commonElementCount": 0,
        "localizedMessage": "Something went terribly wrong!",
        "message": "Something went terribly wrong!",
        "name": "java.lang.RuntimeException",
        "extendedStackTrace": "java.lang.RuntimeException: Something went terribly wrong!\n\tat org.o12stack.o12stack.testcenter.jobs.JobExecutor.lambda$submit$2(JobExecutor.java:95) ~[classes!/:2]\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128) [?:?]\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628) [?:?]\n\tat java.lang.Thread.run(Thread.java:835) [?:?]\n"
    },
    "endOfBatch": false,
    "loggerFqcn": "org.apache.logging.slf4j.Log4jLogger",
    "instant": {
        "epochSecond": 1569318014,
        "nanoOfSecond": 940076000
    },
    "threadId": 46,
    "threadPriority": 5
}
```

Beside these job-related log messages, the application emits some more technical messages at startup and shutdown. They come from the Spring(Boot) framework.

## Usage / Demo

In our workshops, this application will be run inside a Kubernetes cluster. 
Within this repo, we provide a local demo based on `docker-compose`. The usage in Kubernetes can be easily (well, kind of ...) derived from this demo.

### Build application

Build the application including the Docker image:

    mvn clean package

### Run demo

The resources for this demo can be found in `./local-demo/`:

Spin up the stack with:

```
docker-compose up
```

The stack consists of the demo app, Prometheus and Grafana.

The UI of the demo app can be accessed via http://localhost:8080.

**Prometheus** is configured by mounting the `prometheus.yml` in which the scraping of our demo application is set up. You can access Prometheus via http://localhost:9090.

**Grafana** can be accessed via http://localhost:3000. The initial username/pwd is `admin`/`admin`, you are requested to change it, but that step can be skipped. 

The demo configures the datasource for Prometheus as well as an example dashboard (named **o12stack-test-center**) which displays some metrics of the test app.

Go play around with the stuff ...


[rules-of-a-threadpoolexecutor-pool-size]: http://www.bigsoft.co.uk/blog/2009/11/27/rules-of-a-threadpoolexecutor-pool-size
