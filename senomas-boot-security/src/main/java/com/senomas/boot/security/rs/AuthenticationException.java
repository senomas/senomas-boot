package com.senomas.boot.security.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Forbidden")
public class AuthenticationException extends RuntimeException {
	private static final long serialVersionUID = 1676087038885165338L;
	
	public AuthenticationException(String msg) {
		super(msg);
	}
}
