package org.o12stack.o12stack.testcenter.jobs;

import java.util.Random;

/**
 * Util to create jobs that just sleep.
 */
public class SleepJobs {
	
	private static Random random = new Random();

	public static Job gaussianAround(long sleepMillis) {
		return of(Math.abs(sleepMillis + Math.round(sleepMillis * random.nextGaussian() / 4)));
	}
	
	public static Job of(long sleepMillis) {
		return Job.of(String.format("Sleep for %d ms.", sleepMillis), () -> {
			try {
				Thread.sleep(sleepMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "DONE!";
		});
	}
}
