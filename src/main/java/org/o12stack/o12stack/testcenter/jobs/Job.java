package org.o12stack.o12stack.testcenter.jobs;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Job that can be processed by the {@link JobExecutor}.
 */
public class Job {

	/**
	 * (random) UUID of the job.
	 */
	private UUID id;
	
	/**
	 * Describes the purpose of the job.
	 */
	private String description;
	
	/**
	 * Function that is executed when the job is run.
	 * The job results in a {@link String}.
	 */
	private Supplier<String> function;

	private Job(String description, Supplier<String> function) {
		this.id = UUID.randomUUID();
		this.description = description;
		this.function = function;
	}

	public static Job of(String description, Supplier<String> function) {
		return new Job(description, function);
	}

	public UUID getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Supplier<String> getFunction() {
		return function;
	}
}
