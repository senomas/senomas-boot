package com.senomas.boot.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.senomas.boot.security.service.TokenService;

public class TokenAuthenticationProvider implements AuthenticationProvider {
	private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationProvider.class);
	private final TokenService tokenService;

	public TokenAuthenticationProvider(TokenService tokenService) {
		this.tokenService = tokenService;
		log.debug("init");
	}
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		TokenAuthentication tokenAuth = (TokenAuthentication) authentication;
		if (tokenAuth.getToken() != null) {
			LoginUser user = tokenService.get(tokenAuth.getToken());
			if (log.isDebugEnabled()) log.debug("SET USER "+user);
			if (user == null) throw new BadCredentialsException("Invalid token");
			tokenAuth.setUser(user);
		}
		return tokenAuth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TokenAuthentication.class.isAssignableFrom(authentication);
	}

}
