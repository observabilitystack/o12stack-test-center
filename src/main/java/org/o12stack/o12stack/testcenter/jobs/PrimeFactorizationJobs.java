package org.o12stack.o12stack.testcenter.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Jobs performing prime factorization.
 * 
 * Not the best algorithm(s), they are supposed to create some load!
 */
public class PrimeFactorizationJobs {

	private static Random random = new Random();

	public static Job random(int low, int high) {
		int number = random.nextInt(high + 1 - low) + low;
		return of(number);
	}

	public static Job of(long number) {
		return Job.of(String.format("Calculate prime factors of %d", number), () -> {
			return primeFactors(number).toString();
		});
	}

	private static List<Long> primeFactors(long number) {
		List<Long> primeFactors = new ArrayList<>();
		long prime = 2;
		long remainder = number;
		while (remainder > 1) {
			while (remainder % prime == 0) {
				remainder = remainder / prime;
				primeFactors.add(prime);
			}
			prime = nextPrime(prime);
		}
		return primeFactors;
	}

	private static long nextPrime(long prime) {
		long nextPossiblePrime = prime + 1;
		while (!isPrime(nextPossiblePrime)) {
			nextPossiblePrime++;
		}
		return nextPossiblePrime;
	}

	private static boolean isPrime(long n) {
		if (n <= 1) {
			return false;
		}
		for (int i = 2; i < n; i++)
			if (n % i == 0) {
				return false;
			}
		return true;
	}

}
