package org.o12stack.o12stack.testcenter.jobs;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.threads.TaskQueue;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Executes the {@link Job}s in 4 threads.
 */
@Component
public class JobExecutor {

	// Executor stuff
	private ThreadPoolExecutor threadPoolExecutor;
	private TaskQueue taskQueue;
	
	// Metrics
	private Counter jobsSubmittedCounter;
	private Counter jobsCompletedCounter;
	private Timer jobDurationTimer;
	private Timer jobWaitTimer;

	public JobExecutor(MeterRegistry registry) {
		
		this.taskQueue = new TaskQueue(100_000);
		this.threadPoolExecutor = new ThreadPoolExecutor(4, 4, 1000L, TimeUnit.MILLISECONDS, this.taskQueue);
		
		// Gauges for Jobs waiting or in progress
		registry.gauge("jobs_waiting", taskQueue, q -> { return q.size(); });
		registry.gauge("jobs_in_progress", threadPoolExecutor, e -> { return e.getActiveCount(); });
		
		// Counters for totals
		this.jobsSubmittedCounter = registry.counter("jobs_submitted");
		this.jobsCompletedCounter = registry.counter("jobs_completed");
		
		// Timer for job duration
		// (Results in a summary in Prometheus)
		this.jobDurationTimer = Timer.builder("job_duration_timer")
				   .publishPercentiles(0.5, 0.9, 0.95, 0.99) 
				   .register(registry);
		
		// Timer for job wait time
		// (Results in a summary in Prometheus)
		this.jobWaitTimer = Timer.builder("job_wait_timer")
				   .publishPercentiles(0.5, 0.9, 0.95, 0.99) 
				   .register(registry);

	}
	
	/**
	 * Submits a job to be executed or queued beforehand, if necessary.
	 * @param job
	 */
	public void submit(Job job) {
		
		Instant timeSubmitted = Instant.now();
		
		// wrapped Runnable w/ metrics and logging
		Runnable instrumentedJob  = () -> {
			Instant timeStarted = Instant.now();
			this.jobWaitTimer.record(Duration.between(timeSubmitted, timeStarted));
			String result = job.getFunction().get();
			Instant timeFinished = Instant.now();
			this.jobDurationTimer.record(Duration.between(timeStarted, timeFinished));
			this.jobsCompletedCounter.increment();
			System.out.println(String.format("Job %s: %s -> %s", job.getId(), job.getDescription(), result));
		};
		
		synchronized(this.threadPoolExecutor) {
			this.jobsSubmittedCounter.increment();
			this.threadPoolExecutor.execute(instrumentedJob);
		}
	}
	
}
