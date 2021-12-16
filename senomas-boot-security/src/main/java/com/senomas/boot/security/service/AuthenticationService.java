package com.senomas.boot.security.service;

import javax.servlet.http.HttpServletRequest;

import com.senomas.boot.security.LoginRequest;
import com.senomas.boot.security.domain.AuthToken;
import com.senomas.boot.security.domain.SecurityUser;

public interface AuthenticationService {

	SecurityUser getUser();

	AuthToken login(HttpServletRequest request, LoginRequest login);
	
	AuthToken refresh(HttpServletRequest request, String login, String refreshToken);
	
	SecurityUser logout();

	SecurityUser logout(String login);
}
