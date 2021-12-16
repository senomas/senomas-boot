package com.senomas.boot.security;

import org.springframework.security.core.Authentication;

public interface SenomasAuthentication extends Authentication {
	
	boolean hasAuthority(String authority);
	
	boolean hasRole(String role);

}
