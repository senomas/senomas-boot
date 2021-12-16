package com.senomas.boot.security.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.senomas.boot.security.domain.SecurityUser;

@JsonInclude(Include.NON_NULL)
public class AuthToken {
	private final String token;
	
	private final String refreshToken;
	
	private final SecurityUser user;
	
	public AuthToken(String token, String refreshToken, SecurityUser user) {
		this.token = token;
		this.refreshToken = refreshToken;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public SecurityUser getUser() {
		return user;
	}
	
}
