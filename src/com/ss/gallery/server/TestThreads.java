package com.ss.gallery.server;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestThreads {

	private SortedMap<Integer, Integer> map = null;
	private ExecutorService pool;

	public static void main(String[] args) {
		new TestThreads();
	}

	public synchronized void changeMap() {
		long start = System.nanoTime();
		map.clear();
		for (int i = 0; i < 100000; i++) {
			map.put(i, i);
		}
		long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		System.out.println(Thread.currentThread().getName() + ": map changed within " + duration + " ms and contains " + map.size() + " elements");
	}
	
	public int getMapSize() {
		return map.size();
	}

	public TestThreads() {
		
		//map = new TreeMap<Integer, Integer>();
		map = Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>());

		map.put(1, 1);
		map.put(2, 1);
		map.put(3, 1);
		
		pool = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 100; i++) {
			if (i % 2 == 0) {
				pool.submit(new T2(this));
			} else {
				pool.submit(new T(this));
			}
		}

	}

	public static class T implements Runnable {

		private TestThreads th;

		public T(TestThreads th) {
			this.th = th;
		}

		@Override
		public void run() {
			th.changeMap();
		}
	}

	public static class T2 implements Runnable {

		private TestThreads th;

		public T2(TestThreads th) {
			this.th = th;
		}

		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName() + ": current map size: " + th.getMapSize());
		}
	}

}
