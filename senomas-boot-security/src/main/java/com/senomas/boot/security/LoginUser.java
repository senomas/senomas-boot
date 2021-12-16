package com.senomas.boot.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.senomas.boot.security.domain.SecurityUser;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements Serializable {
	private static final long serialVersionUID = 1L;

	final String token;
	final SecurityUser user;
	final Collection<GrantedAuthority> authorities;
	
	@JsonCreator
	public LoginUser(@JsonProperty("token") String token, @JsonProperty("user") SecurityUser user) {
		this.token = token;
		this.user = user;
		this.authorities = new LinkedList<>(user.getAuthorities());
	}
	
	public String getToken() {
		return token;
	}
	
	public SecurityUser getUser() {
		return user;
	}

	public void addAuthority(GrantedAuthority authority) {
		this.authorities.add(authority);
	}

	public void addAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities.addAll(authorities);
	}
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
}
