package org.o12stack.o12stack.testcenter.jobs;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This bean is publishing jobs to the {@link JobExecutor}.
 */
@Component
public class JobPublisher {

	private static final long WAIT_TIME_BETWEEN_JOBS_MILLIS = 100l;

	// Logging
    final Logger logger = LoggerFactory.getLogger(JobPublisher.class);

    @Autowired
    private JobExecutor jobExecutor;

    /**
     * Is the publisher running?
     */
	private boolean running = false;
	
	/**
	 * Does the publisher publish outliers (=long running jobs)?
	 */
	private boolean outliers = false;

	/**
	 * The complexity of the jobs published.
	 */
	private Complexity complexity = Complexity.MEDIUM;
	
	/**
	 * Random number generator
	 */
	private Random random = new Random();

	/**
	 * Start publishing jobs.
	 */
	public void start() {
		if(this.running) {
			return;
		}
		this.running = true;
		new Thread(() -> {
			try {
				while (this.running) {
					Thread.sleep(WAIT_TIME_BETWEEN_JOBS_MILLIS);
					this.jobExecutor.submit(job());
				}
			} catch (InterruptedException e) {
				logger.error("Publisher thread was interrupted", e);
			}
		}).start();
		logger.info("Started publishing of jobs.");
	}

	/**
	 * Stop publishing jobs.
	 */
	public void stop() {
		this.running = false;
		logger.info("Stopped publishing of jobs.");
	}
	
	/**
	 * Create random Job.
	 * If {@link #outliers} is set, approx. 3 percent of the jobs take 3 times longer.
	 * @return random Job
	 */
	private Job job() {
		int outlierFactor = 1;
		if(this.outliers) {
			if(random.nextInt(100)>=97) {
				outlierFactor = 3;
			}
		}
		return SleepJobs.gaussianAround(Math.round(complexity.meanJobDuration * outlierFactor));
	}
	
	public Complexity getComplexity() {
		return complexity;
	}

	public void setComplexity(Complexity complexity) {
		this.complexity = complexity;
		logger.info("Set job complexity to {}.", this.complexity);
	}

	public boolean isRunning() {
		return running;
	}
	
	public boolean isOutliers() {
		return outliers;
	}

	public void setOutliers(boolean outliers) {
		this.outliers = outliers;
		logger.info("Set outlier publishing to {}.", this.outliers);
	}

	/**
	 * Complexity of a Job. 
	 */
	public static enum Complexity {
		
		VERY_LOW(75l, "very low"), LOW(150l, "low"), MEDIUM(300l, "medium"), HIGH(600l, "high"), VERY_HIGH(900l, "very high");
		
		/**
		 * mean job duration
		 */
		private long meanJobDuration;
		
		/**
		 * caption of the value (for UI)
		 */
		private String caption;

		private Complexity(long meanJobDuration, String caption) {
			this.meanJobDuration = meanJobDuration;
			this.caption = caption;
		}

		public long getMeanJobDuration() {
			return meanJobDuration;
		}

		public String getCaption() {
			return caption;
		}
	}

}
