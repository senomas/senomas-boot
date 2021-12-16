package com.senomas.common;

public interface RateLimiterService {

	boolean acquire(String url);
	
	boolean acquire(String url, String id);
	
}
