package org.o12stack.o12stack.testcenter.jobs;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.apache.tomcat.util.threads.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Executes the {@link Job}s using a threadpool of variable size.
 */
@Component
public class JobExecutor {

	// Logging
    final Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	
    // Executor stuff
    private ThreadPoolExecutor threadPoolExecutor;
    private TaskQueue taskQueue;
    private PoolSize poolSize = PoolSize.FOUR;
    
	// Metrics
	private Counter jobsSubmittedCounter;
	private Counter jobsCompletedCounter;
	private Counter jobsFailedCounter;
	private Timer jobDurationTimer;
	private Timer jobWaitTimer;
	// (gauges have no member variable)
	
	/**
	 * If 'the bomb' is dropped, the next executed job will fail.
	 */
	private AtomicBoolean bombDropped = new AtomicBoolean(false);
	
	private boolean shutdownRequested = false;

	public JobExecutor(MeterRegistry registry) {
		
		this.taskQueue = new TaskQueue(100_000);
		this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize.poolSize, this.poolSize.poolSize, 1L, TimeUnit.HOURS, this.taskQueue);
		
		// Gauges for jobs waiting / in progress
		registry.gauge("jobs_waiting", taskQueue, q -> { return q.size(); });
		registry.gauge("jobs_in_progress", threadPoolExecutor, e -> { return e.getActiveCount(); });
		
		// Counters for totals
		this.jobsSubmittedCounter = registry.counter("jobs_submitted");
		this.jobsCompletedCounter = registry.counter("jobs_completed");
		this.jobsFailedCounter = registry.counter("jobs_failed");
		
		// Timer for job duration
		// (Results in a 'summary' in Prometheus)
		this.jobDurationTimer = Timer.builder("job_duration_timer")
				   .publishPercentiles(0.5, 0.9, 0.95, 0.99) 
				   .register(registry);
		
		// Timer for job wait time
		// (Results in a 'summary' in Prometheus)
		this.jobWaitTimer = Timer.builder("job_wait_timer")
				   .publishPercentiles(0.5, 0.9, 0.95, 0.99) 
				   .register(registry);
	}
	
	/**
	 * Submits a job to be executed or queued beforehand, if necessary.
	 * 
	 * It wraps the actual job function in a lambda for metrics, logging and exception handling.
	 * 
	 * @param job
	 */
	public void submit(Job job) {
		
		// no new jobs are allowed to be submitted if shutdown is in progress
		if(shutdownRequested) {
			return;
		}

		// start wait
		Instant timeSubmitted = Instant.now();
		logger.info("Submitted job {}", job.getId());
		
		Runnable instrumentedJob  = () -> {
			
			// end wait = start exec
			Instant timeStarted = Instant.now();
			Duration waitTime = Duration.between(timeSubmitted, timeStarted);
			this.jobWaitTimer.record(waitTime);
			logger.info("Started job {} after waiting for {}", job.getId(), waitTime);
			
			// Are you gonna drop the bomb or not?
			if(this.bombDropped.getAndSet(false)) {
				logger.error(String.format("Error in job %s", job.getId()), new RuntimeException("Something went terribly wrong!"));
				this.jobsFailedCounter.increment();
				return;
			}
			
			// exec actual job
			String result = job.getFunction().get();
			
			// end exec
			Instant timeFinished = Instant.now();
			Duration runTime = Duration.between(timeStarted, timeFinished);
			this.jobDurationTimer.record(runTime);
			this.jobsCompletedCounter.increment();
			logger.info("Finished job {} after running for {}: '{}' => '{}'", job.getId(), runTime, job.getDescription(), result);
		};
		
		this.jobsSubmittedCounter.increment();
		this.threadPoolExecutor.execute(instrumentedJob);
	}
	
	public PoolSize getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(PoolSize poolSize) {
		if(this.poolSize.poolSize > poolSize.poolSize) {
			this.threadPoolExecutor.setCorePoolSize(poolSize.poolSize);
			this.threadPoolExecutor.setMaximumPoolSize(poolSize.poolSize);
		} else {
			this.threadPoolExecutor.setMaximumPoolSize(poolSize.poolSize);
			this.threadPoolExecutor.setCorePoolSize(poolSize.poolSize);
		}
		this.poolSize = poolSize;
		logger.info("Set pool size to {}.", this.poolSize);
	}
	
	public void dropTheBomb() {
		this.bombDropped.set(true);
	}

	@PreDestroy
	public void shutdown() {
		this.shutdownRequested = true;
		this.taskQueue.clear();
		this.threadPoolExecutor.shutdown();
	}
	
	
	public static enum PoolSize {
		ONE(1), TWO(2), FOUR(4), EIGHT(8), SIXTEEN(16);

		private int poolSize;

		private PoolSize(int poolSize) {
			this.poolSize = poolSize;
		}

		public int getPoolSize() {
			return poolSize;
		}
	}

}
