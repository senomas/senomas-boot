package com.senomas.boot.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.senomas.boot.security.LoginUser;
import com.senomas.boot.security.domain.SecurityUser;

public class TokenAuthentication implements SenomasAuthentication, Authentication {
	private static final long serialVersionUID = 1L;
	private boolean authenticated = false;
	private final String token;
	private LoginUser user;
	private Set<GrantedAuthority> authorities = new HashSet<>();

	public TokenAuthentication(String token) {
		this.token = token;
	}

	public TokenAuthentication(LoginUser loginUser) {
		this.token = loginUser.getToken();
		setUser(loginUser);
	}

	public String getToken() {
		return token;
	}

	public SecurityUser getUser() {
		return user != null ? user.getUser() : null;
	}

	public void setUser(LoginUser user) {
		if (user != null) {
			if (!user.getToken().equals(token))
				throw new RuntimeException("Invalid token " + user.getToken() + " -- ORI " + token);
			this.user = user;
			authenticated = true;
			authorities.clear();
			authorities.addAll(user.getAuthorities());
		} else {
			this.user = null;
			authenticated = false;
			authorities.clear();
		}
	}
	
	@Override
	public String getName() {
		return user != null ? user.getUser().getLogin() : "nobody";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getDetails() {
		return user;
	}

	@Override
	public Object getPrincipal() {
		return user;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
		this.authenticated = authenticated;
	}
	
	public boolean hasAuthority(String authority) {
		for (GrantedAuthority a : authorities) {
			if (a.getAuthority().equals(authority)) return true;
		}
		return false;
	}
	
	public boolean hasRole(String role) {
		for (GrantedAuthority a : authorities) {
			if (a.getAuthority().equals(role)) return true;
		}
		return false;
	}

}
