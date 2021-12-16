package com.senomas.test;

import java.util.concurrent.atomic.AtomicInteger;

public class Test {

	public static void main(String[] args) {
		AtomicInteger counter = new AtomicInteger(Integer.MAX_VALUE - 100);
		for (int i=0; i<100; i++) {
			i = counter.addAndGet(3) % 100;
			if (i < 0) i += 100;
			System.out.println(i);
		}
	}
}
