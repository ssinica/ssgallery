package com.ss.gallery.server;

import java.util.concurrent.TimeUnit;

public class Duration {

	private long start;

	public Duration() {
		start = System.nanoTime();
	}

	public long elapsedMilis() {
		long elapsed = System.nanoTime() - start;
		return TimeUnit.NANOSECONDS.toMillis(elapsed);
	}

	public long elapsedNanos() {
		return System.nanoTime() - start;
	}

}
