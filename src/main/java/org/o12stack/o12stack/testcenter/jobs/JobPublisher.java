package org.o12stack.o12stack.testcenter.jobs;

import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * This bean is publishing jobs to the {@link JobExecutor}.
 */
@Component
public class JobPublisher {

	private JobExecutor jobExecutor;

	private boolean running = false;
	
	private boolean outliers = false;

	private Speed speed = Speed.MEDIUM;
	
	private Complexity complexity = Complexity.MEDIUM;
	
	private Random random = new Random();

	public JobPublisher(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	public void start() {
		this.running = true;
		new Thread(() -> {
			try {
				while (this.running) {
					Thread.sleep(this.speed.sleepBetweenSubmits);
					this.jobExecutor.submit(job());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private Job job() {
		int outlierFactor = 1;
		if(this.outliers) {
			if(random.nextInt(100)>=97) {
				outlierFactor = 3;
			}
		}
		// factor 3 is for 4 threads (75% utilization)
		return SleepJobs.gaussianAround(Math.round(Speed.MEDIUM.sleepBetweenSubmits * complexity.complexityFactor * 3 * outlierFactor));
	}
	
	public void stop() {
		this.running = false;
	}
	
	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}

	public Complexity getComplexity() {
		return complexity;
	}

	public void setComplexity(Complexity complexity) {
		this.complexity = complexity;
	}

	public boolean isRunning() {
		return running;
	}
	
	public boolean isOutliers() {
		return outliers;
	}

	public void setOutliers(boolean outliers) {
		this.outliers = outliers;
	}

	public static enum Speed {
		VERY_LOW(500), LOW(200), MEDIUM(100), HIGH(50), VERY_HIGH(20);

		private long sleepBetweenSubmits;

		private Speed(long sleepBetweenSubmits) {
			this.sleepBetweenSubmits = sleepBetweenSubmits;
		}
	}

	public static enum Complexity {
		VERY_LOW(0.5), LOW(0.8), MEDIUM(1.0), HIGH(1.2), VERY_HIGH(2);
		
		private double complexityFactor;

		private Complexity(double complexityFactor) {
			this.complexityFactor = complexityFactor;
		}
	}

}
