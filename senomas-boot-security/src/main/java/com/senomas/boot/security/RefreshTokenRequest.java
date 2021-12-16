package com.senomas.boot.security;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class RefreshTokenRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Date timestamp;
	
	private String login;
	
	private String refreshTokenHash;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getRefreshTokenHash() {
		return refreshTokenHash;
	}

	public void setRefreshTokenHash(String refreshTokenHash) {
		this.refreshTokenHash = refreshTokenHash;
	}
}
