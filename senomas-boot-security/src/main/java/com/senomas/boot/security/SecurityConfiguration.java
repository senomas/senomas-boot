package com.senomas.boot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.senomas.security")
public class SecurityConfiguration {

	long tokenExpiry;
	
	public long getTokenExpiry() {
		return tokenExpiry;
	}
	
	public void setTokenExpiry(long tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}
}