package org.o12stack.o12stack.testcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class O12stackTestCenterApplication {

	private static boolean killed = false;

	public static void main(String[] args) {
		
		// Start SpringBoot application
		ApplicationContext ctx = SpringApplication.run(O12stackTestCenterApplication.class, args);

		// wait while kill signal is not sent
		while(!killed) {
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// once kill signal is sent ...

		// stop SpringBoot application (=all other threads)
		SpringApplication.exit(ctx);

		// end the main thread (last man standing) with an OOM
		Long[] tooLong = new Long[Integer.MAX_VALUE];
	}

	public static void kill() {
		killed = true;
	}

}
