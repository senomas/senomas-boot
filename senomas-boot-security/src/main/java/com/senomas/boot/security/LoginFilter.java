package com.senomas.boot.security;

import java.io.IOException;
import java.nio.file.ProviderNotFoundException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;

import com.senomas.boot.security.service.AuthUserService;
import com.senomas.boot.security.service.TokenService;

public class LoginFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);
	
	final TokenService tokenService;
	
	final AuthUserService userService;

	String localToken;
	
	public LoginFilter(Environment environment, TokenService tokenService, AuthUserService userService) {
    	log.debug("init");
    	this.tokenService = tokenService;
    	this.userService = userService;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String auth = req.getHeader("authorization");
			String token;
			if (auth != null && auth.startsWith("Bearer ") && (token = auth.substring(7).trim()).length() > 0) {
				SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(token));
			} else {
				SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication((String) null));
			}
		} else {
			throw new RuntimeException("Not supported.");
		}
		try {
			chain.doFilter(request, response);
		} catch (ProviderNotFoundException e) {
			log.warn(e.getMessage(), e);
			if (response instanceof HttpServletResponse) {
				HttpServletResponse res = (HttpServletResponse) response;
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			}
		}
	}

}
